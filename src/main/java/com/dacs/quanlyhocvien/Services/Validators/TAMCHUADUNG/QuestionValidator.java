package com.dacs.quanlyhocvien.Services.Validators.TAMCHUADUNG;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class QuestionValidator {
    private static final Logger logger = Logger.getLogger(QuestionValidator.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Phân loại từ khóa theo nhóm
    private static final Map<QuestionType, Set<String>> KEYWORD_GROUPS = new EnumMap<>(QuestionType.class);
    private static final Set<String> QUESTION_INDICATORS = new HashSet<>();
    private static final Set<String> CONFIGURED_QUERIES = new HashSet<>();

    // Các hằng số cấu hình
    private static final int MIN_QUESTION_LENGTH = 3;
    private static final int MAX_QUESTION_LENGTH = 500;

    static {
        // Khởi tạo các nhóm từ khóa và các chỉ báo
        initializeKeywordGroups();
        initializeQuestionIndicators();
        initializeConfiguredQueries();
    }

    private static void initializeKeywordGroups() {
        // Từ khóa liên quan đến đối tượng USER
        KEYWORD_GROUPS.put(QuestionType.USER, new HashSet<>(Arrays.asList(
                "học viên", "sinh viên", "giáo viên", "giảng viên"
        )));

        // Từ khóa liên quan đến COURSE
        KEYWORD_GROUPS.put(QuestionType.COURSE, new HashSet<>(Arrays.asList(
                "khóa học", "khoá học", "lớp học", "bài học"
        )));

        // Từ khóa liên quan đến ASSIGNMENT
        KEYWORD_GROUPS.put(QuestionType.ASSIGNMENT, new HashSet<>(Arrays.asList(
                "bài tập", "nộp bài", "điểm số", "đánh giá"
        )));

        // Từ khóa liên quan đến ACTION
        KEYWORD_GROUPS.put(QuestionType.ACTION, new HashSet<>(Arrays.asList(
                "đăng ký", "ghi danh", "tham gia", "hủy"
        )));

        // Từ khóa yêu cầu truy vấn DATABASE
        KEYWORD_GROUPS.put(QuestionType.DATABASE_QUERY, new HashSet<>(Arrays.asList(
                "danh sách", "tìm kiếm", "thông tin của", "số lượng", "tổng số"
        )));
    }

    private static void initializeQuestionIndicators() {
        QUESTION_INDICATORS.addAll(Arrays.asList(
                "?", "cho biết", "hãy", "liệt kê", "tìm",
                "hiển thị", "đếm", "tính", "thống kê"
        ));
    }

    private static void initializeConfiguredQueries() {
        CONFIGURED_QUERIES.addAll(Arrays.asList(
                // COUNT queries
                "tổng số giáo viên",
                "tổng số học viên",
                "số lượng giáo viên",
                "số lượng học viên",
                "đếm số giáo viên",
                "đếm số học viên",

                // LIST queries
                "danh sách giáo viên",
                "danh sách học viên",
                "liệt kê giáo viên",
                "liệt kê học viên",

                // SEARCH queries
                "tìm kiếm giáo viên",
                "tìm kiếm học viên",

                // Detail queries
                "thông tin khóa học",
                "chi tiết khóa học",
                "điểm của học viên",
                "bài tập đã nộp"
        ));
    }

    /**
     * Kiểm tra tính hợp lệ tổng thể của câu hỏi
     */
    public ValidationResult validate(String question) {
        logValidationStart(question);

        if (!isBasicValid(question)) {
            return new ValidationResult(false, getInvalidMessage(), QuestionType.INVALID);
        }

        String normalizedQuestion = question.toLowerCase().trim();
        QuestionType type = determineQuestionType(normalizedQuestion);

        // Kiểm tra các điều kiện khác
        boolean isValid = hasValidLength(question) && hasValidStructure(question);

        logValidationResult(question, isValid, type);

        return new ValidationResult(isValid, isValid ? "" : getInvalidMessage(), type);
    }

    /**
     * Kiểm tra xem câu hỏi có yêu cầu truy vấn database không
     */
    public boolean isDatabaseQuery(String question) {
        String normalizedQuestion = question.toLowerCase().trim();

        // Kiểm tra các từ khóa database query
        Set<String> databaseKeywords = KEYWORD_GROUPS.get(QuestionType.DATABASE_QUERY);
        boolean containsDatabaseKeyword = databaseKeywords.stream()
                .anyMatch(normalizedQuestion::contains);

        // Kiểm tra kết hợp với các đối tượng
        boolean containsEntity = KEYWORD_GROUPS.get(QuestionType.USER).stream()
                .anyMatch(normalizedQuestion::contains) ||
                KEYWORD_GROUPS.get(QuestionType.COURSE).stream()
                        .anyMatch(normalizedQuestion::contains) ||
                KEYWORD_GROUPS.get(QuestionType.ASSIGNMENT).stream()
                        .anyMatch(normalizedQuestion::contains);

        boolean isDatabaseQuery = containsDatabaseKeyword && containsEntity;

        logger.info(String.format("""
            Kiểm tra Database Query:
            - Câu hỏi: %s
            - Có từ khóa database: %s
            - Có đối tượng: %s
            - Kết quả: %s
            """,
                question, containsDatabaseKeyword, containsEntity, isDatabaseQuery));

        return isDatabaseQuery;
    }

    /**
     * Kiểm tra xem câu hỏi đã được cấu hình chưa
     */
    public boolean isConfigured(String question) {
        String normalizedQuestion = question.toLowerCase().trim();

        boolean isConfigured = CONFIGURED_QUERIES.stream()
                .anyMatch(normalizedQuestion::contains);

        logger.info(String.format("""
            Kiểm tra Configured Query:
            - Câu hỏi: %s
            - Đã cấu hình: %s
            """,
                question, isConfigured));

        return isConfigured;
    }

    /**
     * Kiểm tra xem có phải câu hỏi đơn giản không cần truy vấn
     */
    public boolean isSimpleQuestion(String question) {
        String normalizedQuestion = question.toLowerCase().trim();

        // Câu hỏi đơn giản thường là các câu hỏi chung, không cần truy vấn dữ liệu
        boolean isSimple = !isDatabaseQuery(question) &&
                hasValidStructure(question) &&
                !normalizedQuestion.contains("của") &&
                !normalizedQuestion.contains("cụ thể") &&
                !normalizedQuestion.contains("chi tiết");

        logger.info(String.format("""
            Kiểm tra Simple Question:
            - Câu hỏi: %s
            - Là câu đơn giản: %s
            """,
                question, isSimple));

        return isSimple;
    }

    /**
     * Kiểm tra tính hợp lệ cơ bản của câu hỏi
     */
    private boolean isBasicValid(String question) {
        if (question == null || question.trim().isEmpty()) {
            logger.warning("Câu hỏi trống hoặc null");
            return false;
        }
        return true;
    }

    /**
     * Xác định loại câu hỏi dựa trên từ khóa
     */
    public QuestionType determineQuestionType(String normalizedQuestion) {
        for (Map.Entry<QuestionType, Set<String>> entry : KEYWORD_GROUPS.entrySet()) {
            if (entry.getValue().stream().anyMatch(normalizedQuestion::contains)) {
                return entry.getKey();
            }
        }
        return QuestionType.GENERAL;
    }

    /**
     * Kiểm tra độ dài câu hỏi
     */
    private boolean hasValidLength(String question) {
        int length = question.trim().length();
        boolean isValid = length >= MIN_QUESTION_LENGTH && length <= MAX_QUESTION_LENGTH;

        if (!isValid) {
            logger.warning(String.format("Độ dài câu hỏi không hợp lệ: %d ký tự", length));
        }

        return isValid;
    }

    /**
     * Kiểm tra cấu trúc câu hỏi
     */
    private boolean hasValidStructure(String question) {
        String normalized = question.trim().toLowerCase();
        return QUESTION_INDICATORS.stream().anyMatch(normalized::contains);
    }

    /**
     * Lấy thông báo khi câu hỏi không hợp lệ
     */
    public String getInvalidMessage() {
        return """
            Xin lỗi, tôi chỉ có thể trả lời các câu hỏi liên quan đến:
            - Thông tin học viên/sinh viên
            - Thông tin giáo viên/giảng viên
            - Thông tin khóa học/lớp học
            - Đăng ký/ghi danh khóa học
            - Bài tập và bài nộp
            - Đánh giá và điểm số
            
            Vui lòng đặt câu hỏi rõ ràng và ngắn gọn.""";
    }

    /**
     * Lấy thông báo khi câu hỏi chưa được cấu hình
     */
    public String getNotConfiguredMessage() {
        return """
            Xin lỗi, hiện tại tôi chưa được cấu hình để trả lời chính xác câu hỏi này.
            Các loại câu hỏi tôi có thể trả lời:
            - Đếm số lượng (giáo viên, học viên)
            - Danh sách cơ bản
            - Tìm kiếm đơn giản
            
            Vui lòng thử các câu hỏi đơn giản hơn hoặc liên hệ admin để được hỗ trợ.""";
    }

    private void logValidationStart(String question) {
        String currentTime = LocalDateTime.now(ZoneOffset.UTC)
                .format(DATE_FORMATTER);
        logger.info(String.format("""
            === Bắt đầu kiểm tra câu hỏi ===
            Thời gian: %s
            Người dùng: VoThanhHa28
            Câu hỏi: %s
            """, currentTime, question));
    }

    private void logValidationResult(String question, boolean isValid, QuestionType type) {
        logger.info(String.format("""
            Kết quả kiểm tra:
            - Câu hỏi: %s
            - Hợp lệ: %s
            - Loại: %s
            """, question, isValid, type));
    }
}