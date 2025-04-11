package com.dacs.quanlyhocvien.Utils;

import java.util.HashMap;
import java.util.Map;

public class DatabaseMapping {
    // Ánh xạ từ tiếng Việt sang tên bảng
    private static final Map<String, String> vietnameseToTableMap = new HashMap<>();
    // Ánh xạ từ tiếng Việt sang tên thuộc tính
    private static final Map<String, String> vietnameseToColumnMap = new HashMap<>();

    static {
        // Ánh xạ bảng
        vietnameseToTableMap.put("tài khoản", "account");
        vietnameseToTableMap.put("học viên", "student");
        vietnameseToTableMap.put("sinh viên", "student");
        vietnameseToTableMap.put("giáo viên", "teacher");
        vietnameseToTableMap.put("giảng viên", "teacher");
        vietnameseToTableMap.put("khóa học", "courses");
        vietnameseToTableMap.put("khoá học", "courses");
        vietnameseToTableMap.put("lớp học", "courses");
        vietnameseToTableMap.put("danh mục", "course_categories");
        vietnameseToTableMap.put("đăng ký", "course_enrollments");
        vietnameseToTableMap.put("ghi danh", "course_enrollments");
        vietnameseToTableMap.put("module", "course_modules");
        vietnameseToTableMap.put("bài học", "lessons");
        vietnameseToTableMap.put("đánh giá", "course_reviews");
        vietnameseToTableMap.put("bài tập", "assignments");
        vietnameseToTableMap.put("nộp bài", "assignment_submissions");
        vietnameseToTableMap.put("bài nộp", "assignment_submissions");

        // Ánh xạ thuộc tính
        vietnameseToColumnMap.put("tên đăng nhập", "username");
        vietnameseToColumnMap.put("mật khẩu", "password");
        vietnameseToColumnMap.put("email", "email");
        vietnameseToColumnMap.put("họ tên", "full_name");
        vietnameseToColumnMap.put("tên đầy đủ", "full_name");
        vietnameseToColumnMap.put("ngày sinh", "date_of_birth");
        vietnameseToColumnMap.put("số điện thoại", "phone_number");
        vietnameseToColumnMap.put("địa chỉ", "address");
        vietnameseToColumnMap.put("giới tính", "gender");
        vietnameseToColumnMap.put("tên khóa học", "course_name");
        vietnameseToColumnMap.put("mã khóa học", "course_code");
        vietnameseToColumnMap.put("mô tả", "description");
        vietnameseToColumnMap.put("giá", "price");
        vietnameseToColumnMap.put("học phí", "price");
        vietnameseToColumnMap.put("ngày bắt đầu", "start_date");
        vietnameseToColumnMap.put("ngày kết thúc", "end_date");
        vietnameseToColumnMap.put("trạng thái", "status");
        vietnameseToColumnMap.put("điểm", "grade");
        vietnameseToColumnMap.put("điểm số", "grade");
        vietnameseToColumnMap.put("đánh giá", "rating");
        vietnameseToColumnMap.put("ngày đăng ký", "enrollment_date");
        vietnameseToColumnMap.put("lớp", "class");
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
