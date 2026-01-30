package com.dacs.quanlyhocvien.Services.Executors;


import com.dacs.quanlyhocvien.Services.Validators.TAMCHUADUNG.SQLValidatorold;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.BadSqlGrammarException;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class SafeQueryExecutor {
    private static final Logger logger = Logger.getLogger(SafeQueryExecutor.class.getName());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SQLValidatorold sqlValidator;

    public List<Map<String, Object>> safeExecute(String sqlQuery) {
        // ✅ Kiểm tra NULL, rỗng, undefined
        if (sqlQuery == null || sqlQuery.trim().isEmpty()
                || sqlQuery.toLowerCase().contains("undefined")
                || sqlQuery.toLowerCase().contains("select undefined")
                || sqlQuery.toLowerCase().matches(".*where\\s+.*=\\s*'undefined'.*")) {

            logger.warning("⚠️ SQL có chứa 'undefined' hoặc vô nghĩa: " + sqlQuery);
            throw new ChatbotException(
                    "SQL_UNDEFINED_OR_INVALID",
                    "⚠️ Mình không tạo được truy vấn hợp lệ cho câu hỏi này.",
                    "SQL có chứa undefined hoặc không rõ ràng: " + sqlQuery
            );
        }

        // ✅ Kiểm tra độ an toàn chung
        if (!isSafeSql(sqlQuery)) {
            logger.warning("⚠️ SQL bị đánh giá không an toàn: " + sqlQuery);
            throw new ChatbotException(
                    "SQL_INVALID",
                    "❌ Mình nhận thấy truy vấn này không an toàn nên không thể thực hiện nhé!",
                    "SQL bị đánh dấu không an toàn: " + sqlQuery
            );
        }

        sqlQuery = ensureLimit(sqlQuery);
        logger.info("▶️ Thực thi câu SQL an toàn: " + sqlQuery);

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sqlQuery);

            // 💡 Logging chi tiết
            if (results == null) {
                logger.warning("❗️ Kết quả trả về NULL.");
            } else if (results.isEmpty()) {
                logger.warning("📭 Không có bản ghi nào được trả về (empty list).");
            } else {
                logger.info("✅ Có " + results.size() + " dòng dữ liệu được trả về.");
                logger.info("📦 Dòng đầu tiên: " + results.get(0).toString());

                // Nếu có 1 giá trị duy nhất (1 cột), in riêng:
                if (results.get(0).size() == 1) {
                    Object value = results.get(0).values().iterator().next();
                    logger.info("🔎 Giá trị đầu tiên: " + value + " (class: " + (value != null ? value.getClass().getSimpleName() : "null") + ")");
                }
            }

            return results;

        } catch (BadSqlGrammarException e) {
            throw new ChatbotException(
                    "SQL_TABLE_OR_COLUMN_ERROR",
                    "⚠️ Có vẻ như một bảng hoặc cột nào đó không tồn tại trong cơ sở dữ liệu.",
                    "Chi tiết lỗi cú pháp SQL: " + e.getMessage()
            );
        } catch (Exception e) {
            logger.severe("💥 Lỗi khi thực thi SQL: " + e.getMessage());
            throw new ChatbotException(
                    "SQL_EXECUTION_ERROR",
                    "⚠️ Có lỗi xảy ra khi truy vấn dữ liệu. Mình sẽ khắc phục sớm nhất có thể!",
                    "Chi tiết lỗi JDBC: " + e.getMessage()
            );
        }
    }

    private String ensureLimit(String sqlQuery) {
        String lowerSql = sqlQuery.trim().toLowerCase();

        // Nếu là câu SELECT bình thường, thêm LIMIT
        if (lowerSql.startsWith("select") && !lowerSql.contains("count(") && !lowerSql.contains("limit")) {
            return sqlQuery + " LIMIT 100";
        }

        return sqlQuery;
    }

    public boolean isSafeSql(String sql) {
        if (sql == null) return false;

        sql = sql.replaceAll("--.*", "") // xóa comment
                .replaceAll("[\\r\\n]+", " ") // bỏ xuống dòng
                .trim()
                .toLowerCase();

        // Bắt buộc có select + from
        boolean hasSelect = sql.startsWith("select");
        boolean hasFrom = sql.contains(" from ");

        boolean hasForbidden = sql.contains("insert")
                || sql.contains("update")
                || sql.contains("delete")
                || sql.contains("drop")
                || sql.contains("alter")
                || sql.contains("truncate");

        logger.info("🔎 Kiểm tra SQL - SELECT: " + hasSelect + ", FROM: " + hasFrom + ", Forbidden: " + hasForbidden);
        return hasSelect && hasFrom && !hasForbidden;
    }
}
