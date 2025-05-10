package com.dacs.quanlyhocvien.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DatabaseMapping {

    private static final Map<String, String> vietnameseToTableMap = new HashMap<>();
    private static final Map<String, String> vietnameseToColumnMap = new HashMap<>();

    static {
        // Ánh xạ tên bảng
        vietnameseToTableMap.put("tài khoản", "account");
        vietnameseToTableMap.put("quản trị viên", "admin");
        vietnameseToTableMap.put("vai trò", "roles");
        vietnameseToTableMap.put("học viên", "student");
        vietnameseToTableMap.put("giáo viên", "teacher");
        vietnameseToTableMap.put("khóa học", "course");
        vietnameseToTableMap.put("danh mục khóa học", "course_category");
        vietnameseToTableMap.put("module", "module");
        vietnameseToTableMap.put("bài học", "lesson");
        vietnameseToTableMap.put("video", "video");
        vietnameseToTableMap.put("tài liệu bài học", "lesson_attachment");
        vietnameseToTableMap.put("đăng ký khóa học", "enrollment");
        vietnameseToTableMap.put("tiến độ học", "progress");
        vietnameseToTableMap.put("bài kiểm tra", "quiz");
        vietnameseToTableMap.put("câu hỏi kiểm tra", "quiz_question");
        vietnameseToTableMap.put("đáp án câu hỏi", "quiz_answer");
        vietnameseToTableMap.put("lần làm bài kiểm tra", "quiz_attempt");
        vietnameseToTableMap.put("câu trả lời học viên", "student_answer");
        vietnameseToTableMap.put("chứng chỉ", "certificate");
        vietnameseToTableMap.put("thông báo", "notification");
        vietnameseToTableMap.put("khóa học học viên mua", "student_course");
        vietnameseToTableMap.put("giỏ hàng", "cart_item");

        // Ánh xạ tên cột
        vietnameseToColumnMap.put("tên đầy đủ", "full_name");
        vietnameseToColumnMap.put("email", "email");
        vietnameseToColumnMap.put("mật khẩu", "password");
        vietnameseToColumnMap.put("giới tính", "gender");
        vietnameseToColumnMap.put("ngày sinh", "date_of_birth");
        vietnameseToColumnMap.put("số điện thoại", "phone_number");
        vietnameseToColumnMap.put("địa chỉ", "address");
        vietnameseToColumnMap.put("tên tài khoản", "username");
        vietnameseToColumnMap.put("môn chuyên ngành", "subject_specialization");
        vietnameseToColumnMap.put("trình độ", "qualification");
        vietnameseToColumnMap.put("mã lớp", "class_name");
        vietnameseToColumnMap.put("mã khóa học", "course_id");
        vietnameseToColumnMap.put("giá", "price");
        vietnameseToColumnMap.put("thời lượng", "duration");
        vietnameseToColumnMap.put("trạng thái đăng ký", "payment_status");
    }

    /**
     * Trả về thông tin ánh xạ tiếng Việt -> bảng/cột tiếng Anh dạng gộp đẹp
     */
    public static String getMappingInfo() {
        StringBuilder mappingInfo = new StringBuilder();

        // Bảng
        mappingInfo.append("Tiếng Việt -> Tên bảng:\n");
        String tableMappings = vietnameseToTableMap.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue))
                .entrySet().stream()
                .map(e -> "- " + e.getValue().stream()
                        .map(Map.Entry::getKey)
                        .collect(Collectors.joining(", ")) + " -> " + e.getKey())
                .collect(Collectors.joining("\n"));
        mappingInfo.append(tableMappings).append("\n\n");

        // Cột
        mappingInfo.append("Tiếng Việt -> Tên cột:\n");
        String columnMappings = vietnameseToColumnMap.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue))
                .entrySet().stream()
                .map(e -> "- " + e.getValue().stream()
                        .map(Map.Entry::getKey)
                        .collect(Collectors.joining(", ")) + " -> " + e.getKey())
                .collect(Collectors.joining("\n"));
        mappingInfo.append(columnMappings);

        return mappingInfo.toString();
    }

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
