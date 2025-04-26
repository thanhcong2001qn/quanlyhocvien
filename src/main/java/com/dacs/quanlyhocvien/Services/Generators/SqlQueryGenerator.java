package com.dacs.quanlyhocvien.Services.Generators;

import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import com.dacs.quanlyhocvien.Utils.DatabaseMapping;
import com.dacs.quanlyhocvien.Utils.DatabaseSchemaExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class SqlQueryGenerator {
    private static final Logger logger = Logger.getLogger(SqlQueryGenerator.class.getName());

    @Autowired
    private GeminiApiClient geminiApiClient;

    @Autowired
    private DatabaseSchemaExtractor schemaExtractor;

    private String cachedSchema = null;

    /**
     * Sinh câu truy vấn SQL dựa trên ý định và câu hỏi
     * @param intent Ý định đã được phân tích
     * @param question Câu hỏi gốc của người dùng
     * @return Câu truy vấn SQL
     */
    public String generate(String intent, String question) {
        try {
            if (cachedSchema == null) {
                cachedSchema = schemaExtractor.getCompleteSchema();
                logger.info("Đã cache schema database");
            }

            String prompt = buildSqlGenerationPrompt(intent, question);
            String generatedSql = geminiApiClient.getResponse(prompt);

            // Làm sạch SQL được sinh ra
            String cleanedSql = cleanSqlQuery(generatedSql);

            // Validate SQL cơ bản
            if (!isValidSql(cleanedSql)) {
                throw new RuntimeException("SQL được sinh ra không hợp lệ: " + cleanedSql);
            }

            logger.info("SQL được sinh ra: " + cleanedSql);
            return cleanedSql;

        } catch (Exception e) {
            logger.severe("Lỗi khi sinh SQL: " + e.getMessage());
            throw new RuntimeException("Không thể tạo truy vấn SQL", e);
        }
    }

    /**
     * Xây dựng prompt cho việc sinh SQL
     */
    private String buildSqlGenerationPrompt(String intent, String question) {
        return String.format("""
            You are a SQL expert. Generate a valid SQL query based on the following:
            
            DATABASE SCHEMA:
            %s
            
            VIETNAMESE TO ENGLISH MAPPING:
            %s
            
            QUESTION: %s
            INTENT: %s
            
            Requirements:
            1. Use only tables and columns from the schema
            2. Return only the SQL query without any explanation or markdown
            3. Use proper JOIN conditions and WHERE clauses
            4. Consider performance and optimization
            5. Use proper aggregation functions when needed
            6. Handle null values appropriately
            
            Generate SQL query:
            """,
                cachedSchema,
                DatabaseMapping.getMappingInfo(),
                question,
                intent
        );
    }

    /**
     * Làm sạch câu SQL được sinh ra
     */
    private String cleanSqlQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new RuntimeException("SQL được sinh ra trống hoặc null");
        }

        // Loại bỏ markdown nếu có
        sql = sql.replaceAll("```sql\\s*", "")
                .replaceAll("```", "")
                .trim();

        // Chuẩn hóa khoảng trắng và xuống dòng
        sql = sql.replaceAll("\\s+", " ")
                .replaceAll("\\s*,\\s*", ", ")
                .replaceAll("\\s*=\\s*", " = ")
                .trim();

        return sql;
    }

    /**
     * Kiểm tra tính hợp lệ cơ bản của SQL
     */
    private boolean isValidSql(String sql) {
        sql = sql.toLowerCase();

        // Kiểm tra các thành phần cơ bản của câu SELECT
        if (!sql.contains("select")) {
            logger.warning("SQL thiếu mệnh đề SELECT");
            return false;
        }

        if (!sql.contains("from")) {
            logger.warning("SQL thiếu mệnh đề FROM");
            return false;
        }

        // Kiểm tra cân bằng dấu ngoặc
        long openParens = sql.chars().filter(ch -> ch == '(').count();
        long closeParens = sql.chars().filter(ch -> ch == ')').count();
        if (openParens != closeParens) {
            logger.warning("SQL có dấu ngoặc không cân bằng");
            return false;
        }

        // Kiểm tra các từ khóa cơ bản
        String[] basicKeywords = {"select", "from", "where", "group by", "having", "order by"};
        String[] parts = sql.split("\\s+");
        boolean hasValidKeywords = false;

        for (String keyword : basicKeywords) {
            if (sql.contains(keyword)) {
                hasValidKeywords = true;
                break;
            }
        }

        if (!hasValidKeywords) {
            logger.warning("SQL không chứa các từ khóa cơ bản");
            return false;
        }

        return true;
    }

    public String generateIntentBasedSql(String userQuestion) {
        try {
            if (cachedSchema == null) {
                cachedSchema = schemaExtractor.getCompleteSchema();
            }

            String prompt = buildAdvancedSqlPrompt(userQuestion);
            String generatedSql = geminiApiClient.getResponse(prompt);

            // Làm sạch SQL
            return cleanSqlQuery(generatedSql);

        } catch (Exception e) {
            throw new RuntimeException("Không thể sinh SQL từ câu hỏi", e);
        }
    }

    private String buildAdvancedSqlPrompt(String question) {
        return String.format("""
        Bạn là chuyên gia SQL.
        Dựa trên DATABASE SCHEMA dưới đây:
                         ➔ Lưu ý:
                            - Database sử dụng: **MySQL** (KHÔNG phải SQLite, PostgreSQL hoặc các hệ khác).
                            - Các lệnh như PRAGMA, sqlite_master, information_schema bị cấm.
                            - Chỉ được sử dụng các lệnh SELECT cơ bản trên các bảng đã cho trong Database Schema bên dưới.
                            - Không sinh ra bất kỳ lệnh metadata inspection nào.
                            - Nếu không tìm thấy dữ liệu trong schema, lịch sự từ chối trả lời rõ ràng!
        %s

        Ánh xạ Tiếng Việt sang bảng/cột:

        %s

        Người dùng hỏi:

        "%s"

        Hướng dẫn:
                        - Chỉ được sinh câu lệnh SELECT hợp lệ.
                        - Tuyệt đối KHÔNG sinh DELETE, UPDATE, INSERT, DROP, ALTER hoặc bất kỳ câu lệnh nào làm thay đổi dữ liệu hay cấu trúc hệ thống.
                        - Các truy vấn SELECT lấy thông tin như tên, danh sách, số lượng đều được phép và an toàn.
                        - Nếu câu hỏi yêu cầu nhiều thông tin (ví dụ: tổng số + danh sách tên), hãy sinh SELECT đầy đủ các trường liên quan.
                        - Lưu ý: Nếu truy vấn đếm số lượng (COUNT) thì không cần thêm LIMIT.
                        - Nếu truy vấn lấy danh sách (SELECT cột), thì cần thêm LIMIT 1000 để giới hạn kết quả.
                        - Nếu cần, sử dụng JOIN hợp lý giữa các bảng.
                        - Nếu câu hỏi không thể xử lý chỉ bằng SELECT hoặc gây rủi ro bảo mật, từ chối lịch sự với lý do phù hợp (ví dụ: "Xin lỗi, tôi không được phép thực hiện thao tác này để đảm bảo an toàn hệ thống.").
                        - Tuyệt đối KHÔNG thêm markdown, KHÔNG giải thích, chỉ trả về câu lệnh SQL.

        Dựa trên câu hỏi sau, sinh SQL phù hợp:
        """,
                cachedSchema,
                DatabaseMapping.getMappingInfo(),
                question
        );
    }


    /**
     * Thêm điều kiện giới hạn kết quả nếu cần
     */
    private String addLimitIfNeeded(String sql) {
        if (!sql.toLowerCase().contains("limit")) {
            return sql + " LIMIT 1000"; // Giới hạn mặc định
        }
        return sql;
    }
}