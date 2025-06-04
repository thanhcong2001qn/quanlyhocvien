package com.dacs.quanlyhocvien.Utils;

import com.dacs.quanlyhocvien.Exceptions.ChatbotException;
import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import com.dacs.quanlyhocvien.Enums.ChatbotIntent;
import com.github.pemistahl.lingua.api.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
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

        // Nếu chuỗi quá ngắn → GIBBERISH
        if (normalized.length() < 3) {
            return ChatbotIntent.GIBBERISH;
        }

        // Nếu chỉ là 1 từ ngắn như “hi”, “ok” → có thể là ENGLISH nếu meaningful
        if (normalized.matches("^[a-zA-Z]{1,4}$")) {
            if (!isMeaningfulEnglish(normalized)) {
                return ChatbotIntent.GIBBERISH;
            }
            return ChatbotIntent.ENGLISH;
        }

        // Dùng Lingua detect ngôn ngữ (cả có dấu hoặc không dấu)
        Language lang = detector.detectLanguageOf(normalized);

        if (lang == Language.VIETNAMESE) {
            return ChatbotIntent.VIETNAMESE;
        }

        if (lang == Language.ENGLISH) {
            if (!isMeaningfulEnglish(normalized)) {
                return ChatbotIntent.GIBBERISH;
            }
            return ChatbotIntent.ENGLISH;
        }

        // Nếu không phải tiếng Việt hay tiếng Anh → GIBBERISH
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
            // Nếu lỗi là mất kết nối AI → fallback là GIBBERISH, KHÔNG được ENGLISH
            if (ce.getErrorCode().equals("ERR_GEMINI_UNAVAILABLE")) {
                System.err.println("❗ Gemini không phản hồi, fallback GIBBERISH");
                return false;
            }
            // Nếu lỗi khác → log và vẫn giả định là không meaningful
            System.err.println("❌ Lỗi khi đánh giá meaningful English: " + ce.getTechnicalMessage());
            return false;
        } catch (Exception e) {
            System.err.println("❌ Exception trong isMeaningfulEnglish: " + e.getMessage());
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
                .replaceAll("[^\\p{L}\\p{N}]+", " "); // chuẩn hóa: xoá_, xoá.hv, xoá-hv → xoa hv

        return UNSAFE_PATTERN.matcher(normalized).find();
    }

}
