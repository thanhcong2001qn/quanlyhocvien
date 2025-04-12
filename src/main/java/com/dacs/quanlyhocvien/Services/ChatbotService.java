package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Utils.DatabaseMapping;
import com.dacs.quanlyhocvien.Utils.DatabaseSchemaExtractor;
import com.dacs.quanlyhocvien.config.GeminiConfig;
import com.dacs.quanlyhocvien.models.Example;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class ChatbotService {
    private static final Logger logger = Logger.getLogger(ChatbotService.class.getName());

    @Autowired
    private GeminiConfig geminiConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DatabaseSchemaExtractor schemaExtractor;

    private String cachedSchema = null;

    public String processQuery(String userQuestion) {
        try {
            logger.info("Xử lý câu hỏi: " + userQuestion);

            if (!isValidQuestion(userQuestion)) {
                return "Xin lỗi, tôi chỉ hỗ trợ các câu hỏi liên quan đến truy vấn dữ liệu học viên, giáo viên hoặc khóa học.";
            }

            String intent = analyzeQuestionIntent(userQuestion);
            logger.info("Ý định được phân tích: " + intent);

            String sqlQuery = generateSqlQuery(intent, userQuestion);
            if (!isValidSql(sqlQuery)) {
                throw new RuntimeException("SQL không hợp lệ. Bot tạo sai cú pháp.");
            }
            logger.info("SQL được tạo: " + sqlQuery);

            List<Map<String, Object>> results = executeQuery(sqlQuery);
            logger.info("Kết quả thu được: " + (results != null ? results.size() : 0) + " bản ghi");

            return generateResponse(userQuestion, results);
        } catch (Exception e) {
            logger.severe("Lỗi khi xử lý câu hỏi: " + e.getMessage());
            e.printStackTrace();
            return "Xin lỗi, tôi gặp sự cố khi xử lý câu hỏi của bạn. Lỗi: " + e.getMessage();
        }
    }

    private boolean isValidQuestion(String question) {
        String q = question.toLowerCase();
        return q.contains("học viên") || q.contains("giáo viên") || q.contains("khóa học") || q.contains("lớp học");
    }

    private boolean isValidSql(String sql) {
        sql = sql.toLowerCase();
        return sql.contains("select") && sql.contains("from");
    }

    private String analyzeQuestionIntent(String question) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiConfig.getApiKey());

            if (cachedSchema == null) {
                cachedSchema = schemaExtractor.getCompleteSchema();
            }

            String mappingInfo = buildMappingInfo();
            List<Example> examplesList = loadExamples();
            String examples = buildPromptFromExamples(examplesList);

            Map<String, Object> part = new HashMap<>();
            part.put("text", "Bạn là trợ lý AI. Dựa vào SCHEMA dưới đây, hãy phân tích ý định câu hỏi.\n\n" +
                    "SCHEMA:\n" + cachedSchema + "\n\nÁNH XẠ TIẾNG VIỆT SANG ENGLISH:\n" + mappingInfo +
                    "\n\nVí dụ:\n" + examples +
                    "\n\nCâu hỏi: " + question);

            Map<String, Object> content = new HashMap<>();
            content.put("role", "user");
            content.put("parts", List.of(part));

            Map<String, Object> request = new HashMap<>();
            request.put("contents", List.of(content));
            request.put("generationConfig", Map.of(
                    "temperature", 0.1,
                    "topK", 40,
                    "topP", 0.95,
                    "maxOutputTokens", 1024
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            Map response = restTemplate.postForObject(geminiConfig.getApiUrl(), entity, Map.class);

            String extractedIntent = extractTextFromResponse(response);
            return DatabaseMapping.translateQuery(extractedIntent);
        } catch (Exception e) {
            throw new RuntimeException("Không thể phân tích ý định câu hỏi", e);
        }
    }

    private String generateSqlQuery(String intent, String question) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiConfig.getApiKey());

            if (cachedSchema == null) {
                cachedSchema = schemaExtractor.getCompleteSchema();
            }

            List<Example> examplesList = loadExamples();
            String sqlExamples = buildPromptFromExamples(examplesList);

            Map<String, Object> part = new HashMap<>();
            part.put("text", "Strictly output pure valid SQL without explanations or Markdown.\n\nSCHEMA:\n" + cachedSchema +
                    "\n\nCâu hỏi: " + question + "\n\nÝ định: " + intent + "\n\nVí dụ SQL:\n" + sqlExamples);

            Map<String, Object> content = new HashMap<>();
            content.put("role", "user");
            content.put("parts", List.of(part));

            Map<String, Object> request = new HashMap<>();
            request.put("contents", List.of(content));
            request.put("generationConfig", Map.of(
                    "temperature", 0.1,
                    "topK", 40,
                    "topP", 0.95,
                    "maxOutputTokens", 1024
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            Map response = restTemplate.postForObject(geminiConfig.getApiUrl(), entity, Map.class);

            return extractTextFromResponse(response);
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo truy vấn SQL", e);
        }
    }

    private List<Map<String, Object>> executeQuery(String sqlQuery) {
        try {
            String cleanedQuery = sqlQuery.replaceAll("```sql\\s*", "").replaceAll("```", "").trim();
            return jdbcTemplate.queryForList(cleanedQuery);
        } catch (Exception e) {
            return List.of(Map.of("error", "Lỗi SQL: " + e.getMessage()));
        }
    }

    private String generateResponse(String question, List<Map<String, Object>> results) {
        if (results == null || results.isEmpty()) {
            return "Không có dữ liệu phù hợp với yêu cầu của bạn.";
        }

        StringBuilder table = new StringBuilder();
        table.append("<table border='1' style='border-collapse:collapse;'>");

        Map<String, Object> firstRow = results.get(0);
        table.append("<thead><tr>");
        for (String key : firstRow.keySet()) {
            table.append("<th style='padding:8px;'>").append(key).append("</th>");
        }
        table.append("</tr></thead><tbody>");

        for (Map<String, Object> row : results) {
            table.append("<tr>");
            for (Object value : row.values()) {
                table.append("<td style='padding:8px;'>").append(value != null ? value.toString() : "").append("</td>");
            }
            table.append("</tr>");
        }

        table.append("</tbody></table>");

        return "Kết quả cho câu hỏi: <b>" + question + "</b><br/>" + table.toString();
    }

    private String extractTextFromResponse(Map response) {
        if (response == null) return "Không nhận được phản hồi từ API.";
        try {
            if (response.containsKey("candidates")) {
                List<Map> candidates = (List<Map>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map candidate = candidates.get(0);
                    Map content = (Map) candidate.get("content");
                    List<Map> parts = (List<Map>) content.get("parts");
                    if (!parts.isEmpty()) {
                        return parts.get(0).get("text").toString();
                    }
                }
            }
        } catch (Exception ignored) {}
        return "Không thể trích xuất văn bản từ phản hồi.";
    }

    // Helper methods

    private String buildMappingInfo() {
        return
                "Tiếng Việt -> Tên bảng:\n" +
                        "- tài khoản -> account\n" +
                        "- học viên, sinh viên -> student\n" +
                        "- giáo viên, giảng viên -> teacher\n" +
                        "- khóa học, khoá học, lớp học -> courses\n" +
                        "- danh mục -> course_categories\n" +
                        "- đăng ký, ghi danh -> course_enrollments\n" +
                        "- module -> course_modules\n" +
                        "- bài học -> lessons\n" +
                        "- đánh giá -> course_reviews\n" +
                        "- bài tập -> assignments\n" +
                        "- nộp bài, bài nộp -> assignment_submissions\n\n" +

                        "Tiếng Việt -> Tên cột:\n" +
                        "- tên đăng nhập -> username\n" +
                        "- mật khẩu -> password\n" +
                        "- email -> email\n" +
                        "- họ tên, tên đầy đủ -> full_name\n" +
                        "- ngày sinh -> date_of_birth\n" +
                        "- số điện thoại -> phone_number\n" +
                        "- địa chỉ -> address\n" +
                        "- giới tính -> gender\n" +
                        "- tên khóa học -> course_name\n" +
                        "- mã khóa học -> course_code\n" +
                        "- mô tả -> description\n" +
                        "- giá, học phí -> price\n" +
                        "- ngày bắt đầu -> start_date\n" +
                        "- ngày kết thúc -> end_date\n" +
                        "- trạng thái -> status\n" +
                        "- điểm, điểm số -> grade\n" +
                        "- đánh giá -> rating\n" +
                        "- ngày đăng ký -> enrollment_date\n" +
                        "- lớp -> class";
    }

    private List<Example> loadExamples() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream inputStream = getClass().getResourceAsStream("/examples.json")) {
            return mapper.readValue(inputStream, new TypeReference<List<Example>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc file examples.json", e);
        }
    }

    private String buildPromptFromExamples(List<Example> examples) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < examples.size(); i++) {
            Example ex = examples.get(i);
            sb.append(i + 1).append(". Câu hỏi: \"").append(ex.getQuestion()).append("\"\n");
            sb.append("   Phân tích: ").append(ex.getAnalysis()).append("\n");
            sb.append("   SQL: ").append(ex.getSql()).append("\n\n");
        }
        return sb.toString();
    }

    private String fillPlaceholders(String text, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            text = text.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return text;
    }
}