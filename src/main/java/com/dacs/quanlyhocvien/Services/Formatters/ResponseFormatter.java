package com.dacs.quanlyhocvien.Services.Formatters;

import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

@Component
public class ResponseFormatter {
    private static final Logger logger = Logger.getLogger(ResponseFormatter.class.getName());

    private static final Map<String, String> COLUMN_NAMES = new HashMap<>();

    @Autowired
    private GeminiApiClient geminiApiClient;

    static {
        initializeColumnNames();
    }

    private static void initializeColumnNames() {
        COLUMN_NAMES.put("id", "Mã số");
        COLUMN_NAMES.put("name", "Tên");
        COLUMN_NAMES.put("email", "Email");
        COLUMN_NAMES.put("phone", "Số điện thoại");
        COLUMN_NAMES.put("address", "Địa chỉ");
        COLUMN_NAMES.put("total", "Tổng số");
        COLUMN_NAMES.put("created_at", "Ngày tạo");
        COLUMN_NAMES.put("updated_at", "Ngày cập nhật");
        COLUMN_NAMES.put("department_name", "Khoa/Bộ môn");
        COLUMN_NAMES.put("position", "Chức vụ");
        COLUMN_NAMES.put("specialization", "Chuyên môn");
        COLUMN_NAMES.put("class_name", "Lớp");
        COLUMN_NAMES.put("student_code", "Mã học viên");
        COLUMN_NAMES.put("birth_date", "Ngày sinh");
        COLUMN_NAMES.put("course_name", "Tên khóa học");
        COLUMN_NAMES.put("course_code", "Mã khóa học");
        COLUMN_NAMES.put("teacher_name", "Giáo viên phụ trách");
        COLUMN_NAMES.put("start_date", "Ngày bắt đầu");
        COLUMN_NAMES.put("end_date", "Ngày kết thúc");
    }

    public String format(String question, List<Map<String, Object>> results) {
        try {
            if (results == null || results.isEmpty()) {
                return "<p>Không có dữ liệu phù hợp với yêu cầu của bạn.</p>";
            }

            // Nếu chỉ có 1 dòng và 1 cột bất kỳ thì tự động trả về dạng text
            if (results.size() == 1 && results.get(0).size() == 1) {
                Map.Entry<String, Object> onlyEntry = results.get(0).entrySet().iterator().next();
                String key = onlyEntry.getKey();
                Object value = onlyEntry.getValue();

                if ("error".equalsIgnoreCase(key)) {
                    // Nếu là lỗi ➔ trả về lỗi đẹp
                    return "<div style='font-family: Arial, sans-serif; color: #ff4d4d;'>" +
                            "<p><strong>Oops!</strong> " + formatValue(value) + "</p>" +
                            "</div>";
                } else if (value instanceof String || value instanceof Number) {
                    // Nếu là kết quả String / Number bình thường ➔ dùng AI để tạo câu trả lời tự nhiên

                    try {
                        String prompt = String.format("""
                Bạn là trợ lý AI thân thiện.
                Hãy viết một câu trả lời ngắn gọn, vui vẻ và tự nhiên cho:

                Câu hỏi: "%s"
                Kết quả: "%s"

                Yêu cầu:
                - Viết 1-2 câu ngắn gọn.
                - Thân thiện, vui vẻ, có thể thêm 1 emoji nhẹ.
                - Không nhắc lại nguyên văn câu hỏi.
                """, question, formatValue(value));

                        // Gửi prompt lên Gemini
                        String friendlyAnswer = geminiApiClient.getResponse(prompt);

                        // Nếu AI trả về null hoặc lỗi thì fallback
                        if (friendlyAnswer == null || friendlyAnswer.trim().isEmpty()) {
                            friendlyAnswer = "Đây là kết quả bạn hỏi: " + formatValue(value);
                        }

                        return "<div style='font-family: Arial, sans-serif; color: white;'>" +
                                "<p>" + friendlyAnswer + "</p>" +
                                "</div>";

                    } catch (Exception e) {
                        e.printStackTrace();
                        // Nếu lỗi khi gọi AI, fallback về cũ
                        return "<div style='font-family: Arial, sans-serif; color: white;'>" +
                                "<p><b>" + question + "</b></p>" +
                                "<p>" + formatValue(value) + "</p>" +
                                "</div>";
                    }
                }
            }

            StringBuilder response = new StringBuilder();
            response.append("<div style='font-family: Arial, sans-serif; color: white;'>");  // color: white ở div lớn
            // Gửi prompt yêu cầu AI viết lại lời dẫn tự nhiên
            String leadingText = generateFriendlyLeadingText(question);

            // In lời dẫn tự nhiên
            response.append("<h3 style='color: white;'>").append(leadingText).append("</h3>");


            response.append("<div style='overflow-x:auto;'>");
            response.append("<table style='border-collapse: collapse; width: 100%; min-width: 400px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); color: black;'>");

            // Header
            response.append("<thead style='background-color: #4CAF50; color: white;'>");
            response.append("<tr>");
            results.get(0).keySet().stream()
                    .filter(this::shouldShowColumn)
                    .forEach(column -> response.append("<th style='padding: 12px 15px; text-align: left; border-bottom: 1px solid #ddd;'>")
                            .append(getColumnDisplayName(column))
                            .append("</th>"));
            response.append("</tr>");
            response.append("</thead>");

// Body
            response.append("<tbody>");
            for (Map<String, Object> row : results) {
                response.append("<tr style='background-color: #f9f9f9;'>");
                row.entrySet().stream()
                        .filter(entry -> shouldShowColumn(entry.getKey()))
                        .forEach(entry -> response.append("<td style='padding: 12px 15px; border-bottom: 1px solid #ddd;'>")
                                .append(formatValue(entry.getValue()))
                                .append("</td>"));
                response.append("</tr>");
            }
            response.append("</tbody>");

            response.append("</table>");
            response.append("</div>");

            response.append("<p style='color: white; margin-top: 10px;'><i>Tổng số kết quả:</i> <strong>").append(results.size()).append("</strong></p>");
            response.append("</div>"); // đóng div lớn

            return response.toString();

        } catch (Exception e) {
            logger.severe("Lỗi format kết quả: " + e.getMessage());
            return "Có lỗi xảy ra khi hiển thị kết quả.";
        }
    }

    private String generateFriendlyLeadingText(String userQuestion) {
        String prompt = String.format("""
        Bạn là một trợ lý thân thiện.
        Người dùng vừa hỏi: "%s"
        Hãy viết lại một lời dẫn ngắn gọn, tự nhiên, dễ hiểu trước khi hiển thị bảng dữ liệu.
        Ví dụ:
        - Nếu họ hỏi "Liệt kê tên giáo viên", bạn có thể viết lại: "Dưới đây là danh sách các giáo viên hiện có:"
        - Nếu họ hỏi "Cho biết số học viên", bạn có thể viết lại: "Số lượng học viên trong hệ thống là:"
        
        Viết ngắn gọn và tự nhiên.
        """, userQuestion);

        return geminiApiClient.getResponse(prompt);
    }

    private String formatCountResult(Map<String, Object> result, String userQuestion) {
        Long total = ((Number) result.get("total")).longValue();
        String prompt = String.format("""
        Người dùng vừa hỏi: "%s"
        Kết quả tìm được: %d
        Hãy trả lời ngắn gọn, thân thiện, như một trợ lý học viên.
        """, userQuestion, total);

        return geminiApiClient.getResponse(prompt);
    }

    private String formatValue(Object value) {
        if (value == null) return "-";
        if (value instanceof Date) {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(value);
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? "Có" : "Không";
        }
        return String.valueOf(value);
    }

    private boolean shouldShowColumn(String columnName) {
        return !List.of("active", "password", "deleted_at").contains(columnName);
    }

    private String getColumnDisplayName(String columnName) {
        return COLUMN_NAMES.getOrDefault(columnName, columnName);
    }
}
