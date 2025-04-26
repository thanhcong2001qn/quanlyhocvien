package com.dacs.quanlyhocvien.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DatabaseMapping {
    // Ánh xạ từ tiếng Việt sang tên bảng
    private static final Map<String, String> vietnameseToTableMap = new HashMap<>();
    // Ánh xạ từ tiếng Việt sang tên thuộc tính
    private static final Map<String, String> vietnameseToColumnMap = new HashMap<>();

    static {
        // Ánh xạ bảng
        // Ánh xạ bảng người dùng và xác thực
        vietnameseToTableMap.put("tài khoản", "account");
        vietnameseToTableMap.put("quản trị viên", "admin");
        vietnameseToTableMap.put("vai trò", "roles");
        vietnameseToTableMap.put("học viên", "student");
        vietnameseToTableMap.put("giáo viên", "teacher");
        vietnameseToTableMap.put("token xác thực", "verification_token");

        // Ánh xạ bảng khóa học
        vietnameseToTableMap.put("danh mục khóa học", "course_category");
        vietnameseToTableMap.put("khóa học", "course");
        vietnameseToTableMap.put("chương học", "module");
        vietnameseToTableMap.put("bài học", "lesson");
        vietnameseToTableMap.put("video", "video");
        vietnameseToTableMap.put("tài liệu đính kèm", "lesson_attachment");

        // Ánh xạ bảng đăng ký và tiến độ học tập
        vietnameseToTableMap.put("đăng ký khóa học", "enrollment");
        vietnameseToTableMap.put("tiến độ học tập", "progress");
        vietnameseToTableMap.put("đánh giá", "review");

        // Ánh xạ bảng kiểm tra và đánh giá
        vietnameseToTableMap.put("bài kiểm tra", "quiz");
        vietnameseToTableMap.put("câu hỏi", "quiz_question");
        vietnameseToTableMap.put("đáp án", "quiz_answer");
        vietnameseToTableMap.put("làm bài kiểm tra", "quiz_attempt");
        vietnameseToTableMap.put("câu trả lời học viên", "student_answer");
        vietnameseToTableMap.put("chứng chỉ", "certificate");
        vietnameseToTableMap.put("thông báo", "notification");

        // Ánh xạ cột chung
        vietnameseToColumnMap.put("tên đầy đủ", "full_name");
        vietnameseToColumnMap.put("email", "email");
        vietnameseToColumnMap.put("mật khẩu", "password");
        vietnameseToColumnMap.put("giới tính", "gender");
        vietnameseToColumnMap.put("ngày sinh", "date_of_birth");
        vietnameseToColumnMap.put("số điện thoại", "phone_number");
        vietnameseToColumnMap.put("địa chỉ", "address");
        vietnameseToColumnMap.put("tên tài khoản", "username");
        vietnameseToColumnMap.put("tên giáo viên", "full_name");
        vietnameseToColumnMap.put("môn chuyên ngành", "subject_specialization");
        vietnameseToColumnMap.put("trình độ", "qualification");
        vietnameseToColumnMap.put("ngày tạo", "created_at");
        vietnameseToColumnMap.put("ngày cập nhật", "updated_at");
        vietnameseToColumnMap.put("trạng thái hoạt động", "is_active");
        vietnameseToColumnMap.put("xác thực email", "is_email_verified");
        vietnameseToColumnMap.put("ảnh đại diện", "avatar_path");

        // Ánh xạ cột cho vai trò
        vietnameseToColumnMap.put("tên vai trò", "role_name");
        vietnameseToColumnMap.put("mô tả", "description");
        vietnameseToColumnMap.put("id vai trò", "role_id");

        // Ánh xạ cột cho giáo viên
        vietnameseToColumnMap.put("ngày tuyển dụng", "hire_date");

        // Ánh xạ cột cho học viên
        vietnameseToColumnMap.put("lớp", "class");

        // Ánh xạ cột cho token xác thực
        vietnameseToColumnMap.put("mã token", "token");
        vietnameseToColumnMap.put("ngày hết hạn", "expiry_date");

        // Ánh xạ cột cho danh mục khóa học
        vietnameseToColumnMap.put("tên danh mục", "category_name");
        vietnameseToColumnMap.put("đường dẫn biểu tượng", "icon_path");

        // Ánh xạ cột cho khóa học
        vietnameseToColumnMap.put("tiêu đề", "title");
        vietnameseToColumnMap.put("id danh mục", "category_id");
        vietnameseToColumnMap.put("ảnh thu nhỏ", "thumbnail_path");
        vietnameseToColumnMap.put("giá", "price");
        vietnameseToColumnMap.put("giá khuyến mãi", "discount_price");
        vietnameseToColumnMap.put("thời lượng", "duration");
        vietnameseToColumnMap.put("cấp độ", "level");
        vietnameseToColumnMap.put("đã xuất bản", "is_published");
        vietnameseToColumnMap.put("ngày xuất bản", "published_at");
        vietnameseToColumnMap.put("nổi bật", "is_featured");
        vietnameseToColumnMap.put("đánh giá", "rating");
        vietnameseToColumnMap.put("tổng số học viên", "total_students");
        vietnameseToColumnMap.put("tổng số đánh giá", "total_reviews");

        // Ánh xạ cột cho module và bài học
        vietnameseToColumnMap.put("id khóa học", "course_id");
        vietnameseToColumnMap.put("id module", "module_id");
        vietnameseToColumnMap.put("vị trí", "position");
        vietnameseToColumnMap.put("miễn phí", "is_free");

        // Ánh xạ cột cho video và tài liệu
        vietnameseToColumnMap.put("id bài học", "lesson_id");
        vietnameseToColumnMap.put("đường dẫn video", "video_url");
        vietnameseToColumnMap.put("cho phép tải xuống", "is_downloadable");
        vietnameseToColumnMap.put("đường dẫn tệp", "file_path");
        vietnameseToColumnMap.put("loại tệp", "file_type");
        vietnameseToColumnMap.put("kích thước tệp", "file_size");

        // Ánh xạ cột cho đăng ký khóa học
        vietnameseToColumnMap.put("id đăng ký", "enrollment_id");
        vietnameseToColumnMap.put("id học viên", "student_id");
        vietnameseToColumnMap.put("ngày đăng ký", "enrollment_date");
        vietnameseToColumnMap.put("trạng thái thanh toán", "payment_status");
        vietnameseToColumnMap.put("số tiền thanh toán", "payment_amount");
        vietnameseToColumnMap.put("phương thức thanh toán", "payment_method");
        vietnameseToColumnMap.put("mã giao dịch", "transaction_id");
        vietnameseToColumnMap.put("ngày thanh toán", "payment_date");

        // Ánh xạ cột cho tiến độ học tập
        vietnameseToColumnMap.put("vị trí video", "video_position");
        vietnameseToColumnMap.put("hoàn thành", "is_completed");
        vietnameseToColumnMap.put("lần truy cập cuối", "last_accessed_at");
        vietnameseToColumnMap.put("ngày hoàn thành", "completion_date");

        // Ánh xạ cột cho đánh giá
        vietnameseToColumnMap.put("đánh giá sao", "rating");
        vietnameseToColumnMap.put("bình luận", "comment");

        // Ánh xạ cột cho quiz và câu hỏi
        vietnameseToColumnMap.put("thời gian giới hạn", "time_limit");
        vietnameseToColumnMap.put("điểm đạt", "passing_score");
        vietnameseToColumnMap.put("số lần làm bài cho phép", "attempts_allowed");
        vietnameseToColumnMap.put("văn bản câu hỏi", "question_text");
        vietnameseToColumnMap.put("loại câu hỏi", "question_type");
        vietnameseToColumnMap.put("điểm", "points");
        vietnameseToColumnMap.put("văn bản đáp án", "answer_text");
        vietnameseToColumnMap.put("đáp án đúng", "is_correct");
        vietnameseToColumnMap.put("điểm đạt được", "points_earned");

        // Ánh xạ cột cho quiz attempt
        vietnameseToColumnMap.put("id bài kiểm tra", "quiz_id");
        vietnameseToColumnMap.put("điểm số", "score");
        vietnameseToColumnMap.put("thời gian bắt đầu", "start_time");
        vietnameseToColumnMap.put("thời gian nộp bài", "submit_time");
        vietnameseToColumnMap.put("thời gian làm bài", "time_spent");
        vietnameseToColumnMap.put("trạng thái", "status");

        // Ánh xạ cột cho chứng chỉ
        vietnameseToColumnMap.put("mã chứng chỉ", "certificate_code");
        vietnameseToColumnMap.put("ngày cấp", "issue_date");
        vietnameseToColumnMap.put("đường dẫn chứng chỉ", "certificate_path");

        // Ánh xạ cột cho thông báo
        vietnameseToColumnMap.put("id tài khoản", "account_id");
        vietnameseToColumnMap.put("đã đọc", "is_read");
        vietnameseToColumnMap.put("loại thông báo", "notification_type");
        vietnameseToColumnMap.put("id tham chiếu", "reference_id");
    }


    /**
     * Trả về thông tin ánh xạ dưới dạng chuỗi có định dạng
     * @return String chứa thông tin ánh xạ từ tiếng Việt sang tiếng Anh
     */
    public static String getMappingInfo() {
        StringBuilder mappingInfo = new StringBuilder();

        // Ánh xạ bảng
        mappingInfo.append("Tiếng Việt -> Tên bảng:\n");
        String tableMappings = vietnameseToTableMap.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue))
                .entrySet().stream()
                .map(e -> "- " + e.getValue().stream()
                        .map(Map.Entry::getKey)
                        .collect(Collectors.joining(", ")) +
                        " -> " + e.getKey())
                .collect(Collectors.joining("\n"));
        mappingInfo.append(tableMappings).append("\n\n");

        // Ánh xạ cột
        mappingInfo.append("Tiếng Việt -> Tên cột:\n");
        String columnMappings = vietnameseToColumnMap.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue))
                .entrySet().stream()
                .map(e -> "- " + e.getValue().stream()
                        .map(Map.Entry::getKey)
                        .collect(Collectors.joining(", ")) +
                        " -> " + e.getKey())
                .collect(Collectors.joining("\n"));
        mappingInfo.append(columnMappings);

        return mappingInfo.toString();
    }

    // Các phương thức hiện có giữ nguyên
    public static String getTableName(String vietnameseTerm) {
        return vietnameseToTableMap.getOrDefault(vietnameseTerm.toLowerCase(), vietnameseTerm);
    }

    public static String getColumnName(String vietnameseTerm) {
        return vietnameseToColumnMap.getOrDefault(vietnameseTerm.toLowerCase(), vietnameseTerm);
    }

    public static String translateQuery(String vietnameseQuery) {
        String translatedQuery = vietnameseQuery;
        for (Map.Entry<String, String> entry : vietnameseToTableMap.entrySet()) {
            translatedQuery = translatedQuery.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
        }
        for (Map.Entry<String, String> entry : vietnameseToColumnMap.entrySet()) {
            translatedQuery = translatedQuery.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
        }
        return translatedQuery;
    }
}