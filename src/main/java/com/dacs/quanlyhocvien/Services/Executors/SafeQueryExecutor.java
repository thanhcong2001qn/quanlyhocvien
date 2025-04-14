package com.dacs.quanlyhocvien.Services.Executors;

import com.dacs.quanlyhocvien.Services.Validators.TAMCHUADUNG.SQLValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class SafeQueryExecutor {
    private static final Logger logger = Logger.getLogger(SafeQueryExecutor.class.getName());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SQLValidator sqlValidator;

    public List<Map<String, Object>> safeExecute(String sqlQuery) {
        try {
            if (!sqlValidator.isSafe(sqlQuery)) {
                throw new RuntimeException("Câu lệnh SQL không an toàn, không thực thi.");
            }

            sqlQuery = ensureLimit(sqlQuery);

            logger.info("Thực thi câu SQL an toàn: " + sqlQuery);

            return jdbcTemplate.queryForList(sqlQuery);

        } catch (Exception e) {
            logger.severe("Lỗi thực thi SQL an toàn: " + e.getMessage());
            return List.of(Map.of("error", "Lỗi SQL: " + e.getMessage()));
        }
    }

    private String ensureLimit(String sqlQuery) {
        String lowerSql = sqlQuery.trim().toLowerCase();

        // Nếu là câu SELECT bình thường, thêm LIMIT
        if (lowerSql.startsWith("select") && !lowerSql.contains("count(") && !lowerSql.contains("limit")) {
            return sqlQuery + " LIMIT 1000";
        }

        // Nếu là câu SELECT COUNT hoặc đã có LIMIT thì không thêm gì cả
        return sqlQuery;
    }

    public boolean isSafeSql(String sql) {
        sql = sql.toLowerCase();
        return sql.startsWith("select")
                && !sql.contains("insert")
                && !sql.contains("update")
                && !sql.contains("delete")
                && !sql.contains("drop")
                && !sql.contains("alter")
                && !sql.contains("truncate");
    }
}
