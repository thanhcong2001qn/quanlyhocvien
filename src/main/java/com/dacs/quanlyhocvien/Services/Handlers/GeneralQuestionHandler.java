package com.dacs.quanlyhocvien.Services.Handlers;

import com.dacs.quanlyhocvien.Exceptions.ChatbotException;
import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

@Component
public class GeneralQuestionHandler {

    private static final Logger logger = Logger.getLogger(GeneralQuestionHandler.class.getName());

    @Autowired
    private GeminiApiClient geminiApiClient;

    private String knowledgeBase;

    public static final List<String> queryKeywords = List.of(
            "bao nhiêu", "có bao nhiêu", "liệt kê", "tìm", "tìm kiếm", "truy vấn",
            "danh sách", "đếm", "số lượng", "hiển thị", "thống kê", "xem", "có tất cả",
            "gồm những", "list", "show", "count", "display", "how many", "tổng số", "bao gồm",
            "số lượng account", "số lượng học viên", "dữ liệu", "record", "records",
            "tính toán", "có những", "những ai", "kể tên", "ai đang", "gồm", "những gì"
    );


    @PostConstruct
    public void initKnowledgeBase() {
        try {
            this.knowledgeBase = loadKnowledgeBase();
        } catch (IOException e) {
            logger.severe("Lỗi khi load knowledge base: " + e.getMessage());
            this.knowledgeBase = "Xin lỗi, tôi chưa có dữ liệu hỗ trợ.";
        }
    }

    private String loadKnowledgeBase() throws IOException {
        Path path = Paths.get("src/main/resources/knowledge_base.txt");
        if (!Files.exists(path)) {
            throw new ChatbotException(
                    "KNOWLEDGE_BASE_NOT_FOUND",
                    "⚠️ Hệ thống không tìm thấy tài liệu kiến thức cần thiết để trả lời.",
                    "File knowledge_base.txt không tồn tại tại: " + path.toAbsolutePath()
            );
        }

        return Files.readString(path, StandardCharsets.UTF_8);
    }

    /**
     * Nếu là câu hỏi tự nhiên (không liên quan DB), dùng AI trả lời dựa vào document
     */
    public String matchGeneralQuestion(String userQuestion) {
        try {
            // 0. Nếu không phải tiếng Việt ➜ từ chối
            if (!isVietnamese(userQuestion)) {
                throw new ChatbotException(
                        "ERR_LANG_NOT_SUPPORTED",
                        "Hiện tại mình chỉ hỗ trợ tiếng Việt thôi nha! 😅",
                        "Detected non-Vietnamese input: " + userQuestion
                );
            }

            String normalized = userQuestion.toLowerCase();

            // 1. Nếu chứa keyword truy vấn DB ➜ không xử lý tại đây
            for (String keyword : queryKeywords) {
                if (normalized.contains(keyword)) {
                    return null; // sẽ được xử lý bởi SQL generator
                }
            }

            // 2. Xử lý bằng Gemini với knowledge base
            String prompt = String.format("""
            Dựa trên nội dung kiến thức sau:

            %s

            Trả lời ngắn gọn, thân thiện cho câu hỏi:
            "%s"

            Nếu câu hỏi không liên quan đến kiến thức trên, trả lời "Mình không có câu trả lời cho câu hỏi này".
            Nếu không hiểu câu hỏi thì trả lời "Mình không hiểu câu hỏi của bạn".
            """, knowledgeBase, userQuestion);

            String answer = geminiApiClient.getResponse(prompt).trim();

            if (answer.trim().toLowerCase().startsWith("null")) {
                throw new ChatbotException(
                        "ERR_NO_KNOWLEDGE_DATA",
                        "📚 Mình chưa có đủ dữ liệu để trả lời câu hỏi này nha.",
                        "Câu hỏi không match được nội dung trong knowledge base"
                );
            }

            if (answer.toLowerCase().contains("không chắc") ||
                    answer.toLowerCase().contains("không rõ") ||
                    answer.toLowerCase().contains("tôi cần thêm") ||
                    answer.toLowerCase().contains("không biết") ||
                    answer.toLowerCase().contains("tôi không thể")) {
                throw new ChatbotException(
                        "ERR_CANNOT_UNDERSTAND",
                        "🤔 Xin lỗi, mình chưa hiểu câu hỏi của bạn. Bạn có thể hỏi cụ thể hơn không?",
                        "Câu hỏi bị đánh giá là không rõ ràng"
                );
            }


            return answer;

        } catch (ChatbotException e) {
            throw e; // giữ nguyên để lớp trên xử lý
        } catch (Exception e) {
            logger.severe("Lỗi xử lý câu hỏi tự nhiên: " + e.getMessage());
            throw new ChatbotException(
                    "ERR_GENERAL_HANDLER_FAILED",
                    "⚠️ Mình gặp lỗi khi xử lý câu hỏi. Bạn thử lại sau nha.",
                    e.getMessage()
            );
        }
    }

    private boolean isVietnamese(String input) {
        try {
            String prompt = String.format("""
            Detect the language of the following sentence.
            Return only "vi" if it's Vietnamese. Otherwise return language code (like "en", "fr", etc).
            Do not explain. Just return the code.

            Sentence: "%s"
        """, input);

            String langCode = geminiApiClient.getResponse(prompt).trim().toLowerCase();

            // Nếu trả về "und", "undefined", "null" hoặc độ dài quá ngắn → xem là không xác định
            if (langCode.equals("und") || langCode.equals("undefined") || langCode.equals("null") || langCode.length() > 5) {
                logger.warning("Ngôn ngữ không xác định hoặc không rõ: " + langCode);
                return false; // không xử lý
            }

            return langCode.equals("vi");
        } catch (Exception e) {
            logger.warning("Không xác định được ngôn ngữ, mặc định xử lý là tiếng Việt.");
            return true; // fallback cho an toàn
        }
    }

}
