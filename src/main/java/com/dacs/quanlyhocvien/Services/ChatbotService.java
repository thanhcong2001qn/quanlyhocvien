package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Utils.DatabaseMapping;
import com.dacs.quanlyhocvien.Utils.DatabaseSchemaExtractor;
import com.dacs.quanlyhocvien.config.GeminiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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

    // Cache schema để không phải truy vấn lại mỗi lần
    private String cachedSchema = null;

    public String processQuery(String userQuestion) {
        try {
            logger.info("Xử lý câu hỏi: " + userQuestion);

            // 1. Phân tích câu hỏi của người dùng bằng Gemini
            String intent = analyzeQuestionIntent(userQuestion);
            logger.info("Ý định được phân tích: " + intent);

            // 2. Tạo và thực thi truy vấn SQL dựa trên ý định
            String sqlQuery = generateSqlQuery(intent, userQuestion);
            logger.info("SQL được tạo: " + sqlQuery);

            // 3. Thực thi truy vấn
            List<Map<String, Object>> results = executeQuery(sqlQuery);
            logger.info("Kết quả thu được: " + (results != null ? results.size() : 0) + " bản ghi");

            // 4. Định dạng kết quả và tạo phản hồi
            return generateResponse(userQuestion, results);
        } catch (Exception e) {
            logger.severe("Lỗi khi xử lý câu hỏi: " + e.getMessage());
            e.printStackTrace();
            return "Xin lỗi, tôi gặp sự cố khi xử lý câu hỏi của bạn. Lỗi: " + e.getMessage();
        }
    }

    private String analyzeQuestionIntent(String question) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiConfig.getApiKey());

            // Đảm bảo schema được nạp
            if (cachedSchema == null) {
                cachedSchema = schemaExtractor.getCompleteSchema();
            }

            // Xây dựng ánh xạ thuật ngữ tiếng Việt sang tiếng Anh
            String mappingInfo = buildMappingInfo();

            // Xây dựng ví dụ few-shot
            String examples = buildExamples();

            Map<String, Object> part = new HashMap<>();
            part.put("text",
                    "Bạn là một trợ lý phân tích câu hỏi tiếng Việt và chuyển thành thông tin về bảng dữ liệu SQL.\n\n" +
                            "SCHEMA CƠ SỞ DỮ LIỆU:\n" + cachedSchema + "\n\n" +
                            "BẢNG ÁNH XẠ THUẬT NGỮ TIẾNG VIỆT SANG TÊN BẢNG VÀ CỘT:\n" + mappingInfo + "\n\n" +
                            "VÍ DỤ PHÂN TÍCH:\n" + examples + "\n\n" +
                            "Bây giờ hãy phân tích câu hỏi sau và cho tôi biết nó đang hỏi về những bảng, cột dữ liệu nào (sử dụng tên tiếng Anh trong schema):\n" +
                            question
            );

            Map<String, Object> content = new HashMap<>();
            content.put("role", "user");
            content.put("parts", List.of(part));

            Map<String, Object> request = new HashMap<>();
            request.put("contents", List.of(content));
            request.put("generationConfig", Map.of(
                    "temperature", 0.1,  // Giảm xuống để kết quả nhất quán hơn
                    "topK", 40,
                    "topP", 0.95,
                    "maxOutputTokens", 1024
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            logger.info("Gửi yêu cầu phân tích ý định đến Gemini API");
            Map response = restTemplate.postForObject(geminiConfig.getApiUrl(), entity, Map.class);
            logger.info("Nhận phản hồi từ Gemini API");

            String extractedIntent = extractTextFromResponse(response);

            // Ứng dụng ánh xạ từ ngữ để đảm bảo tên bảng/cột chính xác
            return DatabaseMapping.translateQuery(extractedIntent);
        } catch (Exception e) {
            logger.severe("Lỗi khi phân tích ý định: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể phân tích ý định câu hỏi", e);
        }
    }

    private String generateSqlQuery(String intent, String question) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiConfig.getApiKey());

            // Đảm bảo schema được nạp
            if (cachedSchema == null) {
                cachedSchema = schemaExtractor.getCompleteSchema();
            }

            // Xây dựng ví dụ SQL tương ứng với câu hỏi
            String sqlExamples = buildSqlExamples();

            Map<String, Object> part = new HashMap<>();
            part.put("text",
                    "Bạn là một chuyên gia SQL giỏi và nhiệm vụ của bạn là tạo truy vấn SQL chính xác dựa trên câu hỏi tiếng Việt.\n\n" +
                            "SCHEMA CƠ SỞ DỮ LIỆU:\n" + cachedSchema + "\n\n" +
                            "VÍ DỤ VỀ CÂU HỎI VÀ TRUY VẤN SQL TƯƠNG ỨNG:\n" + sqlExamples + "\n\n" +
                            "Câu hỏi: " + question + "\n" +
                            "Phân tích ý định: " + intent + "\n\n" +
                            "Tạo một truy vấn SQL chính xác và hiệu quả cho câu hỏi trên. Chỉ trả về truy vấn SQL không kèm giải thích."
            );

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

            logger.info("Gửi yêu cầu tạo SQL đến Gemini API");
            Map response = restTemplate.postForObject(geminiConfig.getApiUrl(), entity, Map.class);
            logger.info("Nhận phản hồi từ Gemini API");

            return extractTextFromResponse(response);
        } catch (Exception e) {
            logger.severe("Lỗi khi tạo truy vấn SQL: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể tạo truy vấn SQL", e);
        }
    }

    private List<Map<String, Object>> executeQuery(String sqlQuery) {
        try {
            // Clean the SQL query by removing Markdown formatting
            String cleanedQuery = sqlQuery
                    .replaceAll("```sql\\s*", "")
                    .replaceAll("```", "")
                    .trim();

            logger.info("Executing SQL query: " + cleanedQuery);
            return jdbcTemplate.queryForList(cleanedQuery);
        } catch (Exception e) {
            logger.info("Lỗi khi thực thi SQL: " + e.getMessage());
            return List.of(Map.of("error", "Lỗi khi thực thi truy vấn: " + e.getMessage()));
        }
    }
    private String generateResponse(String question, List<Map<String, Object>> results) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiConfig.getApiKey());

            Map<String, Object> part = new HashMap<>();
            part.put("text",
                    "Bạn là trợ lý AI chuyên nghiệp. Hãy tạo một phản hồi bằng tiếng Việt dễ hiểu cho câu hỏi sau:\n\n" +
                            "Câu hỏi: " + question + "\n\n" +
                            "Dữ liệu kết quả: " + results.toString() + "\n\n" +
                            "Hãy trình bày kết quả một cách rõ ràng, dễ hiểu và đầy đủ. Nếu có bảng dữ liệu, hãy định dạng nó cho dễ đọc. Trả lời bằng tiếng Việt, thân thiện và hữu ích."
            );

            Map<String, Object> content = new HashMap<>();
            content.put("role", "user");
            content.put("parts", List.of(part));

            Map<String, Object> request = new HashMap<>();
            request.put("contents", List.of(content));
            request.put("generationConfig", Map.of(
                    "temperature", 0.2,
                    "topK", 40,
                    "topP", 0.95,
                    "maxOutputTokens", 1024
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            logger.info("Gửi yêu cầu tạo phản hồi đến Gemini API");
            Map response = restTemplate.postForObject(geminiConfig.getApiUrl(), entity, Map.class);
            logger.info("Nhận phản hồi từ Gemini API");

            return extractTextFromResponse(response);
        } catch (Exception e) {
            logger.severe("Lỗi khi tạo phản hồi: " + e.getMessage());
            e.printStackTrace();
            return "Xin lỗi, tôi không thể tạo phản hồi cho câu hỏi của bạn. Lỗi: " + e.getMessage();
        }
    }

    private String extractTextFromResponse(Map response) {
        try {
            if (response == null) {
                logger.warning("Phản hồi null từ API");
                return "Không nhận được phản hồi từ API.";
            }

            // Ghi log cấu trúc phản hồi cho mục đích debug
            logger.fine("Cấu trúc phản hồi: " + response);

            if (response.containsKey("candidates")) {
                List<Map> candidates = (List<Map>) response.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map candidate = candidates.get(0);
                    if (candidate.containsKey("content")) {
                        Map content = (Map) candidate.get("content");
                        if (content != null && content.containsKey("parts")) {
                            List<Map> parts = (List<Map>) content.get("parts");
                            if (parts != null && !parts.isEmpty()) {
                                Object textObj = parts.get(0).get("text");
                                if (textObj != null) {
                                    return textObj.toString();
                                }
                            }
                        }
                    }
                }
            }

            return "Không thể trích xuất văn bản từ phản hồi: " + response;
        } catch (Exception e) {
            logger.severe("Lỗi khi trích xuất văn bản từ phản hồi: " + e.getMessage());
            e.printStackTrace();
            return "Lỗi xử lý phản hồi: " + e.getMessage();
        }
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

    private String buildExamples() {
        return
                "1. Câu hỏi: \"Có bao nhiêu học viên trong hệ thống?\"\n" +
                        "   Phân tích: Câu hỏi đang hỏi về số lượng bản ghi trong bảng student.\n\n" +

                        "2. Câu hỏi: \"Liệt kê tất cả khóa học có giá trên 1,000,000 đồng\"\n" +
                        "   Phân tích: Câu hỏi đang hỏi về bảng courses, với điều kiện lọc trên cột price.\n\n" +

                        "3. Câu hỏi: \"Thống kê số lượng học viên đã đăng ký theo từng khóa học\"\n" +
                        "   Phân tích: Câu hỏi liên quan đến các bảng courses và course_enrollments, cần join 2 bảng này và đếm theo course_id.\n\n" +

                        "4. Câu hỏi: \"Danh sách học viên có điểm trung bình cao nhất\"\n" +
                        "   Phân tích: Câu hỏi liên quan đến các bảng student, course_enrollments, cần tính điểm trung bình (grade) theo từng học viên.";
    }

    private String buildSqlExamples() {
        return
                "1. Câu hỏi: \"Có bao nhiêu học viên trong hệ thống?\"\n" +
                        "   SQL: SELECT COUNT(*) AS total_students FROM student;\n\n" +

                        "2. Câu hỏi: \"Liệt kê tất cả khóa học có giá trên 1,000,000 đồng\"\n" +
                        "   SQL: SELECT * FROM courses WHERE price > 1000000;\n\n" +

                        "3. Câu hỏi: \"Thống kê số lượng học viên đã đăng ký theo từng khóa học\"\n" +
                        "   SQL: SELECT c.course_id, c.course_name, COUNT(ce.student_id) AS student_count \n" +
                        "        FROM courses c \n" +
                        "        LEFT JOIN course_enrollments ce ON c.course_id = ce.course_id \n" +
                        "        GROUP BY c.course_id, c.course_name;\n\n" +

                        "4. Câu hỏi: \"Danh sách học viên có điểm trung bình cao nhất\"\n" +
                        "   SQL: SELECT s.student_id, a.full_name, AVG(ce.grade) AS average_grade \n" +
                        "        FROM student s \n" +
                        "        JOIN account a ON s.student_id = a.account_id \n" +
                        "        JOIN course_enrollments ce ON s.student_id = ce.student_id \n" +
                        "        WHERE ce.grade IS NOT NULL \n" +
                        "        GROUP BY s.student_id, a.full_name \n" +
                        "        ORDER BY average_grade DESC \n" +
                        "        LIMIT 10;\n\n" +

                        "5. Câu hỏi: \"Có bao nhiêu giáo viên đang dạy nhiều hơn 3 khóa học?\"\n" +
                        "   SQL: SELECT COUNT(*) AS teacher_count \n" +
                        "        FROM (SELECT t.teacher_id \n" +
                        "              FROM teacher t \n" +
                        "              JOIN courses c ON t.teacher_id = c.teacher_id \n" +
                        "              GROUP BY t.teacher_id \n" +
                        "              HAVING COUNT(c.course_id) > 3) AS subquery;";
    }
}