package com.dacs.quanlyhocvien.Services.Formatters;

import com.dacs.quanlyhocvien.Exceptions.ChatbotException;
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
        // Ánh xạ cột cơ bản
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

        // Ánh xạ cột người dùng và tài khoản
        COLUMN_NAMES.put("username", "Tên tài khoản");
        COLUMN_NAMES.put("password", "Mật khẩu");
        COLUMN_NAMES.put("full_name", "Tên đầy đủ");
        COLUMN_NAMES.put("gender", "Giới tính");
        COLUMN_NAMES.put("date_of_birth", "Ngày sinh");
        COLUMN_NAMES.put("phone_number", "Số điện thoại");
        COLUMN_NAMES.put("avatar_path", "Ảnh đại diện");
        COLUMN_NAMES.put("role_id", "Mã vai trò");
        COLUMN_NAMES.put("is_active", "Trạng thái hoạt động");
        COLUMN_NAMES.put("is_email_verified", "Xác thực email");

        // Ánh xạ cột vai trò
        COLUMN_NAMES.put("role_name", "Tên vai trò");
        COLUMN_NAMES.put("description", "Mô tả");

        // Ánh xạ cột giáo viên
        COLUMN_NAMES.put("subject_specialization", "Môn chuyên ngành");
        COLUMN_NAMES.put("qualification", "Trình độ");
        COLUMN_NAMES.put("hire_date", "Ngày tuyển dụng");

        // Ánh xạ cột học viên
        COLUMN_NAMES.put("class", "Lớp");

        // Ánh xạ cột token xác thực
        COLUMN_NAMES.put("token", "Mã token");
        COLUMN_NAMES.put("expiry_date", "Ngày hết hạn");

        // Ánh xạ cột danh mục khóa học
        COLUMN_NAMES.put("category_name", "Tên danh mục");
        COLUMN_NAMES.put("icon_path", "Đường dẫn biểu tượng");

        // Ánh xạ cột khóa học
        COLUMN_NAMES.put("title", "Tiêu đề");
        COLUMN_NAMES.put("category_id", "Mã danh mục");
        COLUMN_NAMES.put("thumbnail_path", "Ảnh thu nhỏ");
        COLUMN_NAMES.put("price", "Giá");
        COLUMN_NAMES.put("discount_price", "Giá khuyến mãi");
        COLUMN_NAMES.put("duration", "Thời lượng");
        COLUMN_NAMES.put("level", "Cấp độ");
        COLUMN_NAMES.put("is_published", "Đã xuất bản");
        COLUMN_NAMES.put("published_at", "Ngày xuất bản");
        COLUMN_NAMES.put("is_featured", "Nổi bật");
        COLUMN_NAMES.put("rating", "Đánh giá");
        COLUMN_NAMES.put("total_students", "Tổng số học viên");
        COLUMN_NAMES.put("total_reviews", "Tổng số đánh giá");

        // Ánh xạ cột module và bài học
        COLUMN_NAMES.put("course_id", "Mã khóa học");
        COLUMN_NAMES.put("module_id", "Mã chương học");
        COLUMN_NAMES.put("position", "Vị trí");
        COLUMN_NAMES.put("is_free", "Miễn phí");

        // Ánh xạ cột video và tài liệu
        COLUMN_NAMES.put("lesson_id", "Mã bài học");
        COLUMN_NAMES.put("video_url", "Đường dẫn video");
        COLUMN_NAMES.put("is_downloadable", "Cho phép tải xuống");
        COLUMN_NAMES.put("file_path", "Đường dẫn tệp");
        COLUMN_NAMES.put("file_type", "Loại tệp");
        COLUMN_NAMES.put("file_size", "Kích thước tệp");

        // Ánh xạ cột đăng ký khóa học
        COLUMN_NAMES.put("enrollment_id", "Mã đăng ký");
        COLUMN_NAMES.put("student_id", "Mã học viên");
        COLUMN_NAMES.put("enrollment_date", "Ngày đăng ký");
        COLUMN_NAMES.put("payment_status", "Trạng thái thanh toán");
        COLUMN_NAMES.put("payment_amount", "Số tiền thanh toán");
        COLUMN_NAMES.put("payment_method", "Phương thức thanh toán");
        COLUMN_NAMES.put("transaction_id", "Mã giao dịch");
        COLUMN_NAMES.put("payment_date", "Ngày thanh toán");

        // Ánh xạ cột tiến độ học tập
        COLUMN_NAMES.put("video_position", "Vị trí video");
        COLUMN_NAMES.put("is_completed", "Hoàn thành");
        COLUMN_NAMES.put("last_accessed_at", "Lần truy cập cuối");
        COLUMN_NAMES.put("completion_date", "Ngày hoàn thành");

        // Ánh xạ cột đánh giá
        COLUMN_NAMES.put("comment", "Bình luận");

        // Ánh xạ cột quiz và câu hỏi
        COLUMN_NAMES.put("quiz_id", "Mã bài kiểm tra");
        COLUMN_NAMES.put("time_limit", "Thời gian giới hạn");
        COLUMN_NAMES.put("passing_score", "Điểm đạt");
        COLUMN_NAMES.put("attempts_allowed", "Số lần làm bài cho phép");
        COLUMN_NAMES.put("question_text", "Văn bản câu hỏi");
        COLUMN_NAMES.put("question_type", "Loại câu hỏi");
        COLUMN_NAMES.put("points", "Điểm");
        COLUMN_NAMES.put("answer_text", "Văn bản đáp án");
        COLUMN_NAMES.put("is_correct", "Đáp án đúng");
        COLUMN_NAMES.put("points_earned", "Điểm đạt được");

        // Ánh xạ cột làm bài kiểm tra
        COLUMN_NAMES.put("score", "Điểm số");
        COLUMN_NAMES.put("start_time", "Thời gian bắt đầu");
        COLUMN_NAMES.put("submit_time", "Thời gian nộp bài");
        COLUMN_NAMES.put("time_spent", "Thời gian làm bài");
        COLUMN_NAMES.put("status", "Trạng thái");

        // Ánh xạ cột chứng chỉ
        COLUMN_NAMES.put("certificate_code", "Mã chứng chỉ");
        COLUMN_NAMES.put("issue_date", "Ngày cấp");
        COLUMN_NAMES.put("certificate_path", "Đường dẫn chứng chỉ");

        // Ánh xạ cột thông báo
        COLUMN_NAMES.put("account_id", "Mã tài khoản");
        COLUMN_NAMES.put("is_read", "Đã đọc");
        COLUMN_NAMES.put("notification_type", "Loại thông báo");
        COLUMN_NAMES.put("reference_id", "Mã tham chiếu");
    }

    public String format(String question, List<Map<String, Object>> results, String table, List<String> keywords) {
        try {
            if (results == null || results.isEmpty()) {
                return "<p>📋 Không có dữ liệu phù hợp với yêu cầu của bạn.</p>";
            }

            if (results.size() == 1 && results.get(0).size() == 1) {
                Map.Entry<String, Object> onlyEntry = results.get(0).entrySet().iterator().next();
                Object value = onlyEntry.getValue();
                String safeValue = formatValue(value);

                if (safeValue.equals("không xác định")) {
                    return "<p>📋 Không có dữ liệu phù hợp để hiển thị.</p>";
                }

                return buildFriendlyAnswer(question, value);
            }

            if (results.size() <= 10) {
                return buildHtmlTable(question, results, table, keywords);
            }

            String htmlTable = buildHtmlTable(question, subListSafe(results, 10), table, keywords);
            String excelLink = generateExcelLink(results);

            return htmlTable + """
        <p style='margin-top: 12px;'>📦 Bạn có thể tải toàn bộ kết quả tại đây:</p>
        <p>
            <a href='%s' target='_blank'
               style='color: #007bff; text-decoration: underline; font-weight: 500;'>
               Tải Excel kết quả
            </a>
        </p>
        """.formatted(excelLink);

        } catch (ChatbotException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Lỗi khi format dữ liệu: " + e.getMessage());
            throw new ChatbotException(
                    "RESPONSE_FORMAT_ERROR",
                    "⚠️ Mình gặp lỗi khi hiển thị kết quả. Bạn thử lại sau nha!",
                    e.getMessage()
            );
        }
    }

    private List<Map<String, Object>> subListSafe(List<Map<String, Object>> list, int limit) {
        return list.size() <= limit ? list : list.subList(0, limit);
    }

    private String buildFriendlyAnswer(String question, Object value) {
        try {
            String safeValue = formatValue(value);

            if (safeValue.equals("không xác định") ||
                    safeValue.equals("undefined") ||
                    safeValue.equals("null")) {
                return "<p>📋 Không có dữ liệu phù hợp để hiển thị.</p>";
            }

            String prompt = String.format("""
            Bạn là trợ lý AI nghiêm túc.
            Hãy viết một câu trả lời ngắn gọn, chỉnh chu, nghiêm túc nhưng k quá trang trọng và tự nhiên cho:

            Câu hỏi: "%s"
            Kết quả: "%s"

            Yêu cầu:
            - Viết 1-2 câu ngắn gọn.
            - Thân thiện, có thể thêm emoji nhẹ.
            - Không lặp lại nguyên văn câu hỏi.
            """, question, safeValue);

            String friendlyAnswer = geminiApiClient.getResponse(prompt);

            if (friendlyAnswer == null || friendlyAnswer.trim().isEmpty()) {
                friendlyAnswer = "Đây là kết quả bạn hỏi: " + safeValue;
            }

            return "<div style='font-family: Arial, sans-serif; color: white;'><p>" +
                    friendlyAnswer + "</p></div>";

        } catch (Exception e) {
            logger.warning("Không thể tạo phản hồi thân thiện: " + e.getMessage());
            return "<div style='font-family: Arial, sans-serif; color: white;'>" +
                    "<p><b>" + question + "</b></p><p>" + formatValue(value) + "</p></div>";
        }
    }

    public String buildHtmlTable(String question, List<Map<String, Object>> results, String table, List<String> keywords) {
        StringBuilder response = new StringBuilder();

        response.append("<div style='font-family: Arial, sans-serif; color: white;'>");
        response.append("<h3 style='color: white;'>").append(generateFriendlyLeadingText(question, table, keywords)).append("</h3>");

        response.append("""
        <div style="width: 100%; overflow-x: auto;">
          <table style="border-collapse: collapse; min-width: 1200px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); color: black;">
        """);

        response.append("<thead style='background-color: #4CAF50; color: white;'><tr>");
        results.get(0).keySet().stream()
                .filter(this::shouldShowColumn)
                .forEach(column -> response.append("<th style='padding: 12px 15px; border-bottom: 1px solid #ddd;'>")
                        .append(getColumnDisplayName(column))
                        .append("</th>"));
        response.append("</tr></thead><tbody>");

        for (Map<String, Object> row : results) {
            response.append("<tr style='background-color: #f9f9f9;'>");
            row.entrySet().stream()
                    .filter(entry -> shouldShowColumn(entry.getKey()))
                    .forEach(entry -> response.append("<td style='padding: 12px 15px; border-bottom: 1px solid #ddd;'>")
                            .append(formatValue(entry.getValue()))
                            .append("</td>"));
            response.append("</tr>");
        }

        response.append("</tbody></table></div></div>");
        response.append("<p style='margin-top: 10px;'>📊 <i>Tổng số kết quả:</i> <strong>")
                .append(results.size())
                .append("</strong></p>");
        response.append("</div>");
        return response.toString();
    }

    private String generateExcelLink(List<Map<String, Object>> data) {
        try {
            String backendUrl = "http://localhost:8080/download/excel";

            org.springframework.web.client.RestTemplate rest = new org.springframework.web.client.RestTemplate();
            org.springframework.http.HttpEntity<List<Map<String, Object>>> request = new org.springframework.http.HttpEntity<>(data);

            org.springframework.http.ResponseEntity<String> response =
                    rest.postForEntity(backendUrl, request, String.class);

            return response.getBody();
        } catch (Exception e) {
            logger.warning("Không thể tạo link tải Excel: " + e.getMessage());
            return "#";
        }
    }

    private String formatValue(Object value) {
        if (value == null) return "không xác định";
        if (value instanceof Date) {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(value);
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? "Có" : "Không";
        }

        String str = String.valueOf(value).trim();
        if (str.isBlank() || str.equalsIgnoreCase("undefined") || str.equalsIgnoreCase("null")) {
            return "không xác định";
        }

        return str;
    }

    private boolean shouldShowColumn(String columnName) {
        return !List.of("active", "password", "deleted_at").contains(columnName);
    }

    private String getColumnDisplayName(String columnName) {
        return COLUMN_NAMES.getOrDefault(columnName, columnName);
    }

    private String generateFriendlyLeadingText(String userQuestion, String table, List<String> keywords) {
        try {
            String keywordInfo = String.join(", ", keywords); // ví dụ: "học sinh, giáo viên"
            String prompt = String.format("""
            Bạn là một trợ lý AI nghiêm túc.

            Người dùng vừa hỏi: "%s"
            Ý định liên quan đến: %s
            Câu truy vấn truy xuất từ bảng: "%s"

            Hãy viết một lời dẫn ngắn gọn, tự nhiên, thân thiện trước khi hiển thị bảng dữ liệu.

            Yêu cầu:
            - Viết 1 câu dẫn cụ thể, không chung chung.
            - Ưu tiên mô tả tên bảng hoặc từ khoá thành ngôn ngữ tự nhiên nếu có thể.
            - Không viết lại nguyên câu hỏi, không chào hỏi.
        """, userQuestion, keywordInfo, table);

            String result = geminiApiClient.getResponse(prompt);
            return result == null || result.isEmpty()
                    ? "Dưới đây là kết quả bạn yêu cầu:"
                    : result;
        } catch (Exception e) {
            logger.warning("Không thể sinh lời dẫn: " + e.getMessage());
            return "Dưới đây là kết quả bạn yêu cầu:";
        }
    }
}
