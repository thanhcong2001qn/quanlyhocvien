package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Enums.ChatbotIntent;
import com.dacs.quanlyhocvien.Services.Handlers.GeneralQuestionHandler;
import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import org.springframework.stereotype.Service;

@Service
public class ChatbotRouterService {

    private final LanguageDetector detector = LanguageDetectorBuilder
            .fromLanguages(Language.VIETNAMESE, Language.ENGLISH)
            .build();

    public ChatbotIntent detectIntent(String input) {
        if (input == null || input.isBlank()) {
            return ChatbotIntent.UNKNOWN;
        }

        String normalized = input.trim();

        // 🧠 B1: Nếu quá ngắn → có thể là chuỗi loạn → coi là GIBBERISH
        if (normalized.length() <= 2 || normalized.matches("[a-zA-Z]+") && normalized.length() <= 4) {
            return ChatbotIntent.GIBBERISH;
        }

        // 🧠 B2: Phân tích ngôn ngữ
        Language lang = detector.detectLanguageOf(normalized);

        if (lang != Language.VIETNAMESE) {
            if (lang == Language.ENGLISH) {
                return ChatbotIntent.ENGLISH;
            } else {
                return ChatbotIntent.GIBBERISH;
            }
        }

        // 🧠 B3: Nếu là tiếng Việt → tiếp tục kiểm tra intent
        String lower = normalized.toLowerCase();

        // ⚠️ Hành động nguy hiểm
        String[] unsafeKeywords = {"xóa", "bỏ", "thay đổi", "delete", "drop", "truncate", "alter"};
        for (String keyword : unsafeKeywords) {
            if (lower.contains(keyword)) {
                return ChatbotIntent.UNSAFE_COMMAND;
            }
        }

        // 📊 Truy vấn dữ liệu
        for (String keyword : GeneralQuestionHandler.queryKeywords) {
            if (lower.contains(keyword)) {
                return ChatbotIntent.DATABASE_QUERY;
            }
        }

        // 📚 Kiến thức chung
        return ChatbotIntent.GENERAL_KNOWLEDGE;
    }
}
