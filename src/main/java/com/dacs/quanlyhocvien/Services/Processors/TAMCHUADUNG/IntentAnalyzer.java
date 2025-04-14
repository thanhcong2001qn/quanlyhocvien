package com.dacs.quanlyhocvien.Services.Processors.TAMCHUADUNG;

import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import com.dacs.quanlyhocvien.Utils.DatabaseMapping;
import com.dacs.quanlyhocvien.Utils.DatabaseSchemaExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class IntentAnalyzer {
    private static final Logger logger = Logger.getLogger(IntentAnalyzer.class.getName());

    @Autowired
    private DatabaseSchemaExtractor schemaExtractor;

    @Autowired
    private GeminiApiClient geminiApiClient;

    private String cachedSchema = null;

    // Map các pattern với intent
    private static final Map<String, Intent> INTENT_PATTERNS = new LinkedHashMap<>();

    static {
        // Khởi tạo các pattern matching với intent
        initializeIntentPatterns();
    }

    private static void initializeIntentPatterns() {
        // COUNT Intents
        INTENT_PATTERNS.put("(tổng số|số lượng|có bao nhiêu|đếm số).*(giáo viên|giảng viên)", Intent.COUNT_TEACHER);
        INTENT_PATTERNS.put("(tổng số|số lượng|có bao nhiêu|đếm số).*(học viên|sinh viên)", Intent.COUNT_STUDENT);
        INTENT_PATTERNS.put("(tổng số|số lượng|có bao nhiêu|đếm số).*(khóa học|lớp học)", Intent.COUNT_COURSE);

        // LIST Intents
        INTENT_PATTERNS.put("(danh sách|liệt kê).*(giáo viên|giảng viên)", Intent.LIST_TEACHER);
        INTENT_PATTERNS.put("(danh sách|liệt kê).*(học viên|sinh viên)", Intent.LIST_STUDENT);
        INTENT_PATTERNS.put("(danh sách|liệt kê).*(khóa học|lớp học)", Intent.LIST_COURSE);

        // SEARCH Intents
        INTENT_PATTERNS.put("(tìm|tìm kiếm).*(giáo viên|giảng viên)", Intent.SEARCH_TEACHER);
        INTENT_PATTERNS.put("(tìm|tìm kiếm).*(học viên|sinh viên)", Intent.SEARCH_STUDENT);
        INTENT_PATTERNS.put("(tìm|tìm kiếm).*(khóa học|lớp học)", Intent.SEARCH_COURSE);

        // DETAIL Intents
        INTENT_PATTERNS.put("(thông tin|chi tiết).*(giáo viên|giảng viên)", Intent.DETAIL_TEACHER);
        INTENT_PATTERNS.put("(thông tin|chi tiết).*(học viên|sinh viên)", Intent.DETAIL_STUDENT);
        INTENT_PATTERNS.put("(thông tin|chi tiết).*(khóa học|lớp học)", Intent.DETAIL_COURSE);
    }

    /**
     * Phân tích ý định của câu hỏi
     */
    public IntentAnalysisResult analyze(String question) {
        try {
            logAnalysisStart(question);

            // Kiểm tra pattern matching trước
            Intent patternIntent = findPatternIntent(question);
            if (patternIntent != null) {
                logger.info("Tìm thấy intent từ pattern: " + patternIntent);
                return new IntentAnalysisResult(patternIntent, true);
            }

            // Nếu không match pattern, dùng AI phân tích
            return analyzeWithAI(question);

        } catch (Exception e) {
            logger.severe("Lỗi khi phân tích intent: " + e.getMessage());
            return new IntentAnalysisResult(Intent.UNKNOWN, false);
        }
    }

    /**
     * Tìm intent dựa trên pattern matching
     */
    private Intent findPatternIntent(String question) {
        String normalizedQuestion = question.toLowerCase().trim();

        for (Map.Entry<String, Intent> entry : INTENT_PATTERNS.entrySet()) {
            if (normalizedQuestion.matches(".*" + entry.getKey() + ".*")) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Phân tích intent sử dụng AI
     */
    private IntentAnalysisResult analyzeWithAI(String question) {
        try {
            if (cachedSchema == null) {
                cachedSchema = schemaExtractor.getCompleteSchema();
                logger.info("Đã cache schema database");
            }

            String prompt = buildAIPrompt(question);
            logger.info("Gửi prompt tới Gemini AI");

            String response = geminiApiClient.getResponse(prompt);
            String translatedResponse = DatabaseMapping.translateQuery(response);

            Intent intent = Intent.fromString(translatedResponse);
            logger.info("Intent được xác định: " + intent);

            return new IntentAnalysisResult(intent, true);

        } catch (Exception e) {
            logger.severe("Lỗi khi phân tích với AI: " + e.getMessage());
            return new IntentAnalysisResult(Intent.UNKNOWN, false);
        }
    }

    /**
     * Xây dựng prompt cho AI
     */
    private String buildAIPrompt(String question) {
        return String.format("""
            Bạn là trợ lý AI phân tích ngôn ngữ tự nhiên. 
            Dựa vào schema database sau:
            %s

            Và ánh xạ Tiếng Việt - English:
            %s

            Hãy phân tích ý định của câu hỏi và trả về MỘT trong các intent sau:
            - COUNT_TEACHER: Đếm số giáo viên
            - COUNT_STUDENT: Đếm số học viên
            - COUNT_COURSE: Đếm số khóa học
            - LIST_TEACHER: Liệt kê giáo viên
            - LIST_STUDENT: Liệt kê học viên
            - LIST_COURSE: Liệt kê khóa học
            - SEARCH_TEACHER: Tìm kiếm giáo viên
            - SEARCH_STUDENT: Tìm kiếm học viên
            - SEARCH_COURSE: Tìm kiếm khóa học
            - DETAIL_TEACHER: Chi tiết giáo viên
            - DETAIL_STUDENT: Chi tiết học viên
            - DETAIL_COURSE: Chi tiết khóa học
            - UNKNOWN: Không xác định được

            Câu hỏi cần phân tích:
            %s
            
            Chỉ trả về tên intent, không giải thích.
            """,
                cachedSchema,
                DatabaseMapping.getMappingInfo(),
                question
        );
    }

    private void logAnalysisStart(String question) {
        String currentTime = LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logger.info(String.format("""
            === Bắt đầu phân tích intent ===
            Thời gian: %s
            Người dùng: VoThanhHa28
            Câu hỏi: %s
            """, currentTime, question));
    }
}