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
        vietnameseToTableMap.put("tài khoản", "account");
        vietnameseToTableMap.put("quản trị viên", "admin");
        vietnameseToTableMap.put("vai trò", "roles");
        vietnameseToTableMap.put("học viên", "student");
        vietnameseToTableMap.put("giáo viên", "teacher");

        // Ánh xạ cột
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