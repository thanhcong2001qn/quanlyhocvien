package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Enums.ChatbotIntent;
import com.dacs.quanlyhocvien.Exceptions.ChatbotException;
import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import com.dacs.quanlyhocvien.Services.Executors.SafeQueryExecutor;
import com.dacs.quanlyhocvien.Services.Formatters.ResponseFormatter;
import com.dacs.quanlyhocvien.Utils.DatabaseSchemaExtractor;
import com.dacs.quanlyhocvien.Utils.SmartEntityExtractor;
import com.dacs.quanlyhocvien.Utils.SmartUnsafeCommandDetector;
import com.dacs.quanlyhocvien.models.ResolvedQuery;
import com.dacs.quanlyhocvien.models.ResolvedAlias;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

@Service
public class ChatbotService {

    private static final Logger logger = Logger.getLogger(ChatbotService.class.getName());

    @Autowired private ChatbotIntentDetector intentDetector;
    @Autowired private GeminiApiClient geminiApiClient;
    @Autowired private SafeQueryExecutor queryExecutor;
    @Autowired private ResponseFormatter responseFormatter;
    @Autowired private SmartEntityExtractor smartEntityExtractor;
    @Autowired private SemanticAliasEngine semanticAliasEngine;
    @Autowired private GeminiQueryResolver geminiQueryResolver;
    @Autowired private DatabaseSchemaExtractor schemaExtractor;

    public String processQuery(String userQuestion) {
        logger.info("📩 Nhận câu hỏi: " + userQuestion);

        try {
            ChatbotIntent intent = intentDetector.detect(userQuestion);
            logger.info("🧠 Intent được phát hiện: " + intent);

            return switch (intent) {
                case DATABASE_QUERY -> handleDatabaseQuery(userQuestion);
                case GENERAL_KNOWLEDGE -> handleGeneralKnowledge(userQuestion);
                case UNSAFE_COMMAND -> throw new ChatbotException("UNSAFE", "⚠️ Câu hỏi có thể gây nguy hiểm hệ thống.", userQuestion);
                case ENGLISH -> throw new ChatbotException("ENGLISH", "❌ Hiện tại chỉ hỗ trợ tiếng Việt.", userQuestion);
                case GIBBERISH -> throw new ChatbotException("GIBBERISH", "🤖 Tôi không hiểu bạn nói gì.", userQuestion);
                default -> throw new ChatbotException("UNKNOWN", "🤖 Tôi không hiểu câu hỏi của bạn.", userQuestion);
            };

        } catch (ChatbotException e) {
            return "🚨 " + e.getUserMessage();
        } catch (Exception e) {
            logger.severe("Lỗi không xác định: " + e.getMessage());
            return "💥 Đã có lỗi xảy ra: " + e.getMessage();
        }
    }

    private String handleGeneralKnowledge(String question) {
        logger.info("💬 Xử lý câu hỏi kiến thức hệ thống: " + question);

        String schema = schemaExtractor.getCompleteSchema();

        ResolvedQuery resolved = geminiQueryResolver.resolve(question, schema);


        System.out.println("📄 SQL sinh ra: " + resolved.getSql());
        System.out.println("📦 Alias mapping: " + resolved.getAlias_mapping());
        System.out.println("🧠 Intent: " + resolved.getIntent());

        String answer = resolved.getAnswer();
        if (answer == null || answer.isBlank()) {
            return "🤖 Xin lỗi, tôi không có câu trả lời phù hợp.";
        }

        return "📘 " + answer + "<br><br><code style='color:gray; font-size:90%'>" + resolved.getSummary() + "</code>";
    }

    private String handleDatabaseQuery(String question) {
        logger.info("🔍 Đang xử lý truy vấn DB cho: " + question);

        String schema = schemaExtractor.getCompleteSchema();

        // ✅ Dùng Gemini
        ResolvedQuery resolved = geminiQueryResolver.resolve(question, schema);

        if (resolved.getSql() == null || resolved.getSql().isBlank() || resolved.getSql().toLowerCase().contains("undefined")) {
            throw new ChatbotException("SQL_INVALID", "⚠️ Mình không thể tạo truy vấn phù hợp với câu hỏi này.", resolved.getSql());
        }

        resolved.getAlias_mapping().forEach((alias, canonical) -> {
            semanticAliasEngine.cache(alias, new ResolvedAlias(alias, canonical, ""));
        });

        List<Map<String, Object>> result = queryExecutor.safeExecute(resolved.getSql());
        if (result.isEmpty()) {
            return "📭 Không có dữ liệu nào phù hợp với yêu cầu của bạn.";
        }

        return responseFormatter.buildHtmlTable(
                question,
                result,
                resolved.getTables().isEmpty() ? "dữ liệu" : resolved.getTables().get(0),
                new ArrayList<>(resolved.getAlias_mapping().keySet())
        );
    }

    private String extractSql(String rawJson) {
        try {
            int start = rawJson.indexOf("\"sql\"");
            if (start == -1) return null;

            int colon = rawJson.indexOf(":", start);
            int quote1 = rawJson.indexOf("\"", colon + 1);
            int quote2 = rawJson.indexOf("\"", quote1 + 1);
            return rawJson.substring(quote1 + 1, quote2).trim();
        } catch (Exception e) {
            return null;
        }
    }
}
