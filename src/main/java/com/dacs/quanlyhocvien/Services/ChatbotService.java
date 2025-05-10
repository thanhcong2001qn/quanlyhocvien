package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Enums.ChatbotIntent;
import com.dacs.quanlyhocvien.Exceptions.ChatbotException;
import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import com.dacs.quanlyhocvien.Services.Executors.SafeQueryExecutor;
import com.dacs.quanlyhocvien.Services.Formatters.ResponseFormatter;
import com.dacs.quanlyhocvien.Services.Generators.SqlQueryGenerator;
import com.dacs.quanlyhocvien.Services.Handlers.GeneralQuestionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    @Autowired
    private GeneralQuestionHandler generalQuestionHandler;

    @Autowired
    private ChatbotRouterService chatbotRouterService;

    @Autowired
    private GeminiApiClient geminiApiClient;

    @Autowired
    private ResponseFormatter responseFormatter;

    @Autowired
    private SafeQueryExecutor safeQueryExecutor;

    @Autowired
    private SqlQueryGenerator sqlQueryGenerator;

    public String processQuery(String userQuestion) {
        try {
            ChatbotIntent intent = chatbotRouterService.detectIntent(userQuestion);

            return switch (intent) {
                case GENERAL_KNOWLEDGE -> {
                    String answer = generalQuestionHandler.matchGeneralQuestion(userQuestion);
                    yield formatAnswer(answer != null
                            ? answer
                            : "🤖 Xin lỗi, tôi không có câu trả lời cho câu hỏi này.");
                }

                case DATABASE_QUERY -> handleDatabaseQuery(userQuestion);

                case UNSAFE_COMMAND -> throw new ChatbotException(
                        "SQL_RESTRICTED_COMMAND",
                        "🛑 Xin lỗi, mình không được phép thực hiện thao tác này để đảm bảo an toàn hệ thống.",
                        "Phát hiện câu lệnh nguy hiểm từ người dùng: " + userQuestion
                );

                case ENGLISH -> throw new ChatbotException(
                        "ERR_ENGLISH_NOT_SUPPORTED",
                        "❗Hiện tại tôi chỉ hỗ trợ tiếng Việt, bạn vui lòng thử lại nhé!",
                        "English input detected: " + userQuestion
                );

                case GIBBERISH -> throw new ChatbotException(
                        "ERR_GIBBERISH",
                        "🤖 Tôi không hiểu bạn nói gì! Bạn có thể hỏi rõ ràng hơn không?",
                        "Không nhận diện được ý nghĩa câu hỏi: " + userQuestion
                );

                default -> formatAnswer("🤖 Xin lỗi, tôi không có câu trả lời cho câu hỏi này.");
            };

        } catch (ChatbotException ce) {
            return formatErrorResponse(ce.getUserMessage(), ce.getErrorCode());
        } catch (Exception e) {
            return formatErrorResponse("💥 Có lỗi không xác định xảy ra.", "ERR_UNKNOWN");
        }
    }

    private String handleDatabaseQuery(String question) {
        String sql = sqlQueryGenerator.generateIntentBasedSql(question);

        if (sql == null || sql.trim().isEmpty() || sql.trim().equalsIgnoreCase("undefined")) {
            throw new ChatbotException(
                    "ERR_SQL_UNDEFINED",
                    "⚠️ Mình không thể tạo truy vấn phù hợp với câu hỏi này.",
                    "Gemini trả về SQL không hợp lệ: " + sql
            );
        }

        List<Map<String, Object>> result = safeQueryExecutor.safeExecute(sql);

        if (result == null || result.isEmpty()) {
            throw new ChatbotException(
                    "ERR_NO_DATA",
                    "📋 Không có dữ liệu phù hợp với câu hỏi.",
                    "Kết quả truy vấn rỗng: " + sql
            );
        }

        if (result.size() == 1 && result.get(0).size() == 1) {
            Object value = result.get(0).values().iterator().next();
            if (value instanceof Number && ((Number) value).intValue() == 0) {
                throw new ChatbotException("ERR_NO_DATA", "📋 Không có dữ liệu phù hợp với câu hỏi.", "Kết quả COUNT(*) = 0");
            }
        }

        boolean hasUndefined = result.stream()
                .flatMap(map -> map.values().stream())
                .anyMatch(val -> {
                    if (val == null) return false;
                    String str = val.toString().trim().toLowerCase();
                    return str.equals("undefined") || str.equals("null");
                });

        if (hasUndefined) {
            throw new ChatbotException("ERR_SQL_UNDEFINED", "⚠️ Mình không tạo được truy vấn hợp lệ cho câu hỏi này.", sql);
        }

        return responseFormatter.format(question, result);
    }

    private String formatAnswer(String answer) {
        return answer;
    }

    private String formatErrorResponse(String message, String errorCode) {
        return String.format("🚨%s", message);
    }
}
