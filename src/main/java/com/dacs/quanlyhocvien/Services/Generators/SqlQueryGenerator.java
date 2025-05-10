package com.dacs.quanlyhocvien.Services.Generators;

import com.dacs.quanlyhocvien.Exceptions.ChatbotException;
import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import com.dacs.quanlyhocvien.Utils.DatabaseMapping;
import com.dacs.quanlyhocvien.Utils.DatabaseSchemaExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.logging.Logger;

@Component
public class SqlQueryGenerator {
    private static final Logger logger = Logger.getLogger(SqlQueryGenerator.class.getName());

    @Autowired
    private GeminiApiClient geminiApiClient;

    @Autowired
    private DatabaseSchemaExtractor schemaExtractor;

    private String cachedSchema = null;

    public String generate(String intent, String question) {
        try {
            if (cachedSchema == null) {
                cachedSchema = schemaExtractor.getCompleteSchema();
                logger.info("Đã cache schema database");
            }

            String prompt = buildSqlGenerationPrompt(intent, question);
            String generatedSql = geminiApiClient.getResponse(prompt);

            String cleanedSql = cleanSqlQuery(generatedSql);

            if (!isValidSql(cleanedSql)) {
                throw new ChatbotException("ERR_SQL_INVALID", "❌ Câu truy vấn được tạo không hợp lệ. Vui lòng kiểm tra lại câu hỏi.", "SQL không hợp lệ: " + cleanedSql);
            }

            logger.info("SQL được sinh ra: " + cleanedSql);
            return cleanedSql;

        } catch (ChatbotException ce) {
            throw ce; // Đã có thông tin đầy đủ
        } catch (Exception e) {
            logger.severe("Lỗi khi sinh SQL: " + e.getMessage());
            throw new ChatbotException("ERR_SQL_GENERATION", "⚠️ Mình gặp lỗi khi tạo câu truy vấn từ câu hỏi. Bạn thử lại sau nhé.", e.getMessage());
        }
    }

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

    private String cleanSqlQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new ChatbotException("ERR_SQL_EMPTY", "⚠️ Không tạo được câu truy vấn nào từ câu hỏi của bạn.", "SQL được sinh ra là null hoặc rỗng");
        }

        sql = sql.replaceAll("```sql\\s*", "")
                .replaceAll("```", "")
                .replaceAll("[\\r\\n]+", " ") // 👈 Thêm dòng này
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*,\\s*", ", ")
                .replaceAll("\\s*=\\s*", " = ")
                .trim();

        return sql;
    }


    private boolean isValidSql(String sql) {
        sql = sql.toLowerCase();

        if (!sql.contains("select")) {
            logger.warning("SQL thiếu mệnh đề SELECT");
            return false;
        }

        if (!sql.contains("from")) {
            logger.warning("SQL thiếu mệnh đề FROM");
            return false;
        }

        long openParens = sql.chars().filter(ch -> ch == '(').count();
        long closeParens = sql.chars().filter(ch -> ch == ')').count();
        if (openParens != closeParens) {
            logger.warning("SQL có dấu ngoặc không cân bằng");
            return false;
        }

        String[] basicKeywords = {"select", "from", "where", "group by", "having", "order by"};
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

            logger.info("🔥 SQL sinh ra từ Gemini: " + generatedSql);

            // ✅ B1: Kiểm tra nếu Gemini trả về undefined/null/chuỗi rác
            if (generatedSql == null || generatedSql.trim().isEmpty() || generatedSql.trim().equalsIgnoreCase("undefined")) {
                throw new ChatbotException(
                        "ERR_SQL_UNDEFINED",
                        "⚠️ Không thể tạo câu truy vấn từ câu hỏi bạn vừa nhập.",
                        "Gemini trả về SQL không hợp lệ: " + generatedSql
                );
            }

            // ✅ B2: Làm sạch truy vấn
            String cleanedSql = cleanSqlQuery(generatedSql);

            // ✅ B3: Kiểm tra bảng có tồn tại không
            if (!containsValidTable(cleanedSql, cachedSchema)) {
                throw new ChatbotException(
                        "ERR_INVALID_TABLE",
                        "🧾 Có vẻ như bảng bạn yêu cầu không tồn tại trong hệ thống. Vui lòng kiểm tra lại tên bảng nhé!",
                        "SQL truy cập bảng không có trong schema: " + cleanedSql
                );
            }

            return cleanedSql;

        } catch (ChatbotException ce) {
            throw ce;
        } catch (Exception e) {
            throw new ChatbotException(
                    "ERR_SQL_GENERATION",
                    "⚠️ Không thể tạo câu truy vấn từ câu hỏi bạn vừa nhập.",
                    e.getMessage()
            );
        }
    }


    private boolean containsValidTable(String sql, String schema) {
        sql = sql.toLowerCase();

        // Tìm tất cả tên bảng trong schema
        List<String> tableNames = extractTableNamesFromSchema(schema);

        // Lấy tên bảng sau FROM trong SQL
        int fromIndex = sql.indexOf("from ");
        if (fromIndex == -1) return false;

        String[] tokens = sql.substring(fromIndex + 5).split(" ");
        String possibleTable = tokens[0].replaceAll("[^a-zA-Z0-9_]", ""); // bỏ dấu câu

        // So sánh với các bảng đã khai báo
        return tableNames.contains(possibleTable);
    }

    private List<String> extractTableNamesFromSchema(String schema) {
        List<String> tables = new ArrayList<>();
        Pattern pattern = Pattern.compile("create table (\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(schema);
        while (matcher.find()) {
            tables.add(matcher.group(1).toLowerCase());
        }
        return tables;
    }

    private String buildAdvancedSqlPrompt(String question) {
        return String.format("""
        Bạn là chuyên gia SQL.

        Dựa trên DATABASE SCHEMA thực tế dưới đây (các lệnh CREATE TABLE chi tiết):
   
        %s

        Ánh xạ Tiếng Việt sang bảng/cột:
        
        %s

        Lưu ý nghiêm ngặt:
        - Database: **MySQL** (KHÔNG phải SQLite, PostgreSQL...).
        - KHÔNG được dùng PRAGMA, INFORMATION_SCHEMA, sqlite_master, system table.
        - Chỉ sử dụng SELECT cơ bản, JOIN giữa các bảng đã cho nếu cần.
        - Bắt buộc chọn đúng tên bảng và cột như trong SCHEMA. Tuyệt đối không bịa thêm cột hoặc bảng mới.
        - Nếu bảng hoặc dữ liệu không có trả lời "Tôi không có dữ liệu này"
        - Nếu câu hỏi cần đếm (COUNT) → chỉ cần SELECT COUNT(*).
        - Nếu câu hỏi lấy danh sách → SELECT các cột cần thiết + LIMIT 100.
        - KHÔNG sinh các lệnh INSERT, UPDATE, DELETE, ALTER, DROP.
        - Nếu câu hỏi không phù hợp hoặc rủi ro, hãy từ chối lịch sự (ví dụ: "Xin lỗi, tôi không được phép thực hiện thao tác này để đảm bảo an toàn hệ thống.").
        - Chỉ trả về đúng CÂU LỆNH SQL, KHÔNG giải thích, KHÔNG thêm markdown.

        Câu hỏi từ người dùng:

        "%s"

        👉 Dựa vào câu hỏi trên và schema, sinh ra câu lệnh SQL chuẩn xác nhất:
        """,
                cachedSchema,
                DatabaseMapping.getMappingInfo(),
                question
        );
    }

    private String addLimitIfNeeded(String sql) {
        if (!sql.toLowerCase().contains("limit")) {
            return sql + " LIMIT 1000";
        }
        return sql;
    }
}
