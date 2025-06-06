package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Enums.ChatbotIntent;
import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import com.dacs.quanlyhocvien.Utils.SmartInputClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatbotIntentDetector {

    @Autowired
    private GeminiApiClient geminiApiClient;

    @Autowired
    private SmartInputClassifier classifier;

    public ChatbotIntent detect(String userInput) {
        if (userInput == null || userInput.isBlank()) {
            return ChatbotIntent.UNKNOWN;
        }

        // ✅ 1. Dùng local classifier trước
        ChatbotIntent local = classifier.classify(userInput);
        if (local == ChatbotIntent.GIBBERISH || local == ChatbotIntent.ENGLISH) {
            return local;
        }

        // ✅ 2. Nếu là tiếng Việt → gọi Gemini để phân loại (KHÔNG BAO GIỜ chấp nhận GIBBERISH từ Gemini)
        try {
            String prompt = """
            Bạn là một AI chuyên phân loại ý định câu hỏi người dùng. Hãy đọc câu hỏi và phân loại thành một trong các intent sau:
            
            IMPORTANT:
            - DATABASE_QUERY: khi người dùng hỏi thông tin có thể truy vấn từ hệ thống (cơ sở dữ liệu) như học viên, khóa học, giảng viên, điểm, lịch học, số lượng, v.v.
            - GENERAL_KNOWLEDGE: khi người dùng hỏi về bạn, lời chào, cách dùng hệ thống, thông tin không cần truy vấn DB.
            
            🎯 QUAN TRỌNG: Chỉ trả lời đúng 1 từ: DATABASE_QUERY hoặc GENERAL_KNOWLEDGE. Không viết thêm bất cứ từ nào khác.
            
            ✍️ Lưu ý: Người dùng có thể viết tắt hoặc không dấu. Ví dụ:
            - "hv" = "học viên"
            - "dk" = "đăng ký"
            - "kh" = "khoá học"
            - "tt" = "thông tin"
            - "cn" = "chuyên ngành"
            - "gv" = "giáo viên"
            
            Câu hỏi: "%s"
            """.formatted(userInput.trim());


            String response = geminiApiClient.getResponse(prompt).trim().toUpperCase();
            System.out.println("➡️ Gemini intent response: " + response);

            return switch (response) {
                case "DATABASE_QUERY" -> ChatbotIntent.DATABASE_QUERY;
                case "GENERAL_KNOWLEDGE" -> ChatbotIntent.GENERAL_KNOWLEDGE;
                case "UNSAFE_COMMAND" -> {
                    // ✅ Kiểm tra lại nội bộ trước khi chấp nhận UNSAFE
                    if (classifier.isUnsafe(userInput)) {
                        yield ChatbotIntent.UNSAFE_COMMAND;
                    } else {
                        System.out.println("❗ Gemini gán UNSAFE sai – override thành UNKNOWN");
                        yield ChatbotIntent.UNKNOWN;
                    }
                }
                default -> {
                    if (local == ChatbotIntent.UNSAFE_COMMAND) yield ChatbotIntent.UNSAFE_COMMAND;
                    yield ChatbotIntent.UNKNOWN;
                }
            };


        } catch (Exception e) {
            return ChatbotIntent.UNKNOWN;
        }
    }
}
