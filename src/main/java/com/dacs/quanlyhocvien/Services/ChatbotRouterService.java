package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Enums.ChatbotIntent;
import com.dacs.quanlyhocvien.Services.Handlers.GeneralQuestionHandler;
import com.dacs.quanlyhocvien.Utils.SmartInputClassifier;
import com.dacs.quanlyhocvien.Utils.SmartUnsafeCommandDetector;
import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

@Service
public class ChatbotRouterService {

    private static final Logger logger = Logger.getLogger(ChatbotRouterService.class.getName());

    private final LanguageDetector detector = LanguageDetectorBuilder
            .fromLanguages(Language.VIETNAMESE, Language.ENGLISH)
            .build();

    @Autowired
    private GeneralQuestionHandler generalQuestionHandler;

    @Autowired
    private SmartInputClassifier smartInputClassifier;

    private static final List<String> UNSAFE_KEYWORDS = List.of(
            "xóa", "bỏ", "thay đổi", "insert", "delete", "drop", "truncate", "alter"
    );

    public ChatbotIntent detectIntent(String input) {
        if (input == null || input.isBlank()) {
            logger.warning("❗ Input is null or blank, returning UNKNOWN.");
            return ChatbotIntent.UNKNOWN;
        }

        ChatbotIntent type = smartInputClassifier.classify(input);

        switch (type) {
            case GIBBERISH -> {
                logger.info("⚠️ Detected GIBBERISH input: " + input);
                return ChatbotIntent.GIBBERISH;
            }

            case ENGLISH -> {
                logger.info("🌐 Detected ENGLISH input: " + input);
                return ChatbotIntent.ENGLISH;
            }

            case VIETNAMESE -> {
                String lower = input.toLowerCase(Locale.ROOT).trim();

                // ⚠️ Kiểm tra từ khóa nguy hiểm
                if (SmartUnsafeCommandDetector.isUnsafe(input)) {
                    logger.warning("🚫 Phát hiện câu lệnh nguy hiểm: " + input);
                    return ChatbotIntent.UNSAFE_COMMAND;
                }

                // 📊 Truy vấn DB
                if (GeneralQuestionHandler.queryKeywords.stream().anyMatch(lower::contains)) {
                    logger.info("📊 Detected DATABASE_QUERY from: " + input);
                    return ChatbotIntent.DATABASE_QUERY;
                }

                // 📚 Câu hỏi kiến thức chung
                logger.info("📚 Defaulting to GENERAL_KNOWLEDGE for input: " + input);
                return ChatbotIntent.GENERAL_KNOWLEDGE;
            }

            default -> {
                logger.warning("❓ Không xác định loại input, trả về UNKNOWN.");
                return ChatbotIntent.UNKNOWN;
            }
        }
    }
}
