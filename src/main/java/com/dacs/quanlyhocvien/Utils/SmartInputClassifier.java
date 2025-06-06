package com.dacs.quanlyhocvien.Utils;

import com.dacs.quanlyhocvien.Exceptions.ChatbotException;
import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import com.dacs.quanlyhocvien.Enums.ChatbotIntent;
import com.github.pemistahl.lingua.api.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class SmartInputClassifier {

    private final LanguageDetector detector = LanguageDetectorBuilder
            .fromLanguages(Language.VIETNAMESE, Language.ENGLISH)
            .build();

    @Autowired
    private GeminiApiClient geminiApiClient;

    public ChatbotIntent classify(String input) {
        if (input == null || input.isBlank()) return ChatbotIntent.UNKNOWN;

        String normalized = input.trim();
        String lower = normalized.toLowerCase(Locale.ROOT);
        int wordCount = lower.split("\\s+").length;

        // 🚫 Quá ngắn, không có nghĩa
        if (normalized.length() < 3) return ChatbotIntent.GIBBERISH;

        // ⛔ Một từ ngắn như “hi”, “ok” → kiểm tra meaningful
        if (normalized.matches("^[a-zA-Z]{1,4}$")) {
            return isMeaningfulEnglish(normalized) ? ChatbotIntent.ENGLISH : ChatbotIntent.GIBBERISH;
        }

        // ✅ Nếu có ≥ 3 từ → giả định là tiếng Việt hoặc viết tắt → cho Gemini xử lý
        if (wordCount >= 3) return ChatbotIntent.VIETNAMESE;

        // 🧠 Nếu không rõ, dùng Lingua
        Language lang = detector.detectLanguageOf(normalized);
        if (lang == Language.VIETNAMESE) return ChatbotIntent.VIETNAMESE;
        if (lang == Language.ENGLISH) {
            return isMeaningfulEnglish(normalized) ? ChatbotIntent.ENGLISH : ChatbotIntent.GIBBERISH;
        }

        // ❓ Không xác định
        return ChatbotIntent.GIBBERISH;
    }

    private boolean isMeaningfulEnglish(String text) {
        try {
            String prompt = """
            Is the following English sentence meaningful (yes/no)?
            Sentence: "%s"
            """.formatted(text);
            String response = geminiApiClient.getResponse(prompt).trim().toLowerCase();
            return response.contains("yes");
        } catch (ChatbotException ce) {
            if ("ERR_GEMINI_UNAVAILABLE".equals(ce.getErrorCode())) {
                System.err.println("❗ Gemini không phản hồi, fallback GIBBERISH");
                return false;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static final Pattern UNSAFE_PATTERN = Pattern.compile(
            "\\b(xo[aá]|xoa|xóa|xóa hết|xóa toàn bộ|bỏ|bo|thay đổi|thay doi|cập nhật|cap nhat|gỡ|go|" +
                    "drop|truncate|alter|delete|insert|update|remove|reset|clear|overwrite)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public boolean isUnsafe(String input) {
        if (input == null || input.isBlank()) return false;

        String normalized = input
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ");

        return UNSAFE_PATTERN.matcher(normalized).find();
    }
}
