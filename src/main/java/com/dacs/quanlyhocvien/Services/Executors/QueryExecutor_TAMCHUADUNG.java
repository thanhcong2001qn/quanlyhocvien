package com.dacs.quanlyhocvien.Services.Executors;

import com.dacs.quanlyhocvien.Services.Processors.TAMCHUADUNG.Intent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class QueryExecutor_TAMCHUADUNG {
    private static final Logger logger = Logger.getLogger(QueryExecutor_TAMCHUADUNG.class.getName());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Thực thi truy vấn dựa trên Intent và câu hỏi của người dùng
     */
    public List<Map<String, Object>> execute(Intent intent, String userQuestion) {
        try {
            logQueryStart(intent, userQuestion);
            String sqlQuery = buildSqlQuery(intent);

            // Log SQL query
            logger.info("SQL Query: " + sqlQuery);

            // Thực thi query
            List<Map<String, Object>> results = executeQuery(sqlQuery);

            // Log kết quả
            logQueryResults(results);

            return results;

        } catch (Exception e) {
            logger.severe("Lỗi thực thi query: " + e.getMessage());
            return null;
        }
    }

    /**
     * Thực thi truy vấn SQL trực tiếp
     */
    public List<Map<String, Object>> executeRawSql(String sqlQuery) {
        try {
            logRawSqlExecution(sqlQuery);
            return executeQuery(sqlQuery);
        } catch (Exception e) {
            logger.severe("Lỗi thực thi raw SQL: " + e.getMessage());
            return null;
        }
    }

    /**
     * Xây dựng câu truy vấn SQL dựa trên Intent
     */
    public String buildSqlQuery(Intent intent) {
        return switch (intent) {
            // Các truy vấn COUNT
            case COUNT_TEACHER -> "SELECT COUNT(*) as total FROM teachers WHERE active = true";
            case COUNT_STUDENT -> "SELECT COUNT(*) as total FROM students WHERE active = true";
            case COUNT_COURSE -> "SELECT COUNT(*) as total FROM courses WHERE active = true";

            // Các truy vấn LIST
            case LIST_TEACHER -> """
                SELECT t.*, d.name as department_name 
                FROM teachers t 
                LEFT JOIN departments d ON t.department_id = d.id 
                WHERE t.active = true 
                ORDER BY t.id
                """;
            case LIST_STUDENT -> """
                SELECT s.*, c.name as class_name 
                FROM students s 
                LEFT JOIN classes c ON s.class_id = c.id 
                WHERE s.active = true 
                ORDER BY s.id
                """;
            case LIST_COURSE -> """
                SELECT c.*, t.name as teacher_name 
                FROM courses c 
                LEFT JOIN teachers t ON c.teacher_id = t.id 
                WHERE c.active = true 
                ORDER BY c.id
                """;

            // Các truy vấn SEARCH
            case SEARCH_TEACHER -> """
                SELECT t.*, d.name as department_name 
                FROM teachers t 
                LEFT JOIN departments d ON t.department_id = d.id 
                WHERE t.active = true AND t.name LIKE ?
                """;
            case SEARCH_STUDENT -> """
                SELECT s.*, c.name as class_name 
                FROM students s 
                LEFT JOIN classes c ON s.class_id = c.id 
                WHERE s.active = true AND s.name LIKE ?
                """;
            case SEARCH_COURSE -> """
                SELECT c.*, t.name as teacher_name 
                FROM courses c 
                LEFT JOIN teachers t ON c.teacher_id = t.id 
                WHERE c.active = true AND c.name LIKE ?
                """;

            // Các truy vấn DETAIL
            case DETAIL_TEACHER -> """
                SELECT t.*, d.name as department_name,
                       (SELECT COUNT(*) FROM courses WHERE teacher_id = t.id) as course_count
                FROM teachers t 
                LEFT JOIN departments d ON t.department_id = d.id 
                WHERE t.id = ?
                """;
            case DETAIL_STUDENT -> """
                SELECT s.*, c.name as class_name,
                       (SELECT COUNT(*) FROM enrollments WHERE student_id = s.id) as course_count
                FROM students s 
                LEFT JOIN classes c ON s.class_id = c.id 
                WHERE s.id = ?
                """;
            case DETAIL_COURSE -> """
                SELECT c.*, t.name as teacher_name,
                       (SELECT COUNT(*) FROM enrollments WHERE course_id = c.id) as student_count
                FROM courses c 
                LEFT JOIN teachers t ON c.teacher_id = t.id 
                WHERE c.id = ?
                """;

            case UNKNOWN -> throw new IllegalArgumentException("Intent không xác định");
        };
    }

    /**
     * Thực thi câu truy vấn và trả về kết quả
     */
    private List<Map<String, Object>> executeQuery(String sqlQuery) {
        return jdbcTemplate.queryForList(sqlQuery);
    }

    /**
     * Log thông tin bắt đầu thực thi query
     */
    private void logQueryStart(Intent intent, String userQuestion) {
        String currentTime = LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        logger.info(String.format("""
            === Thực thi Database Query ===
            Thời gian: %s
            Người dùng: VoThanhHa28
            Intent: %s
            Câu hỏi: %s
            """,
                currentTime,
                intent,
                userQuestion
        ));
    }

    /**
     * Log thông tin thực thi raw SQL
     */
    private void logRawSqlExecution(String sqlQuery) {
        String currentTime = LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        logger.info(String.format("""
            === Thực thi Raw SQL Query ===
            Thời gian: %s
            Người dùng: VoThanhHa28
            SQL: %s
            """,
                currentTime,
                sqlQuery
        ));
    }

    /**
     * Log kết quả query
     */
    private void logQueryResults(List<Map<String, Object>> results) {
        if (results == null) {
            logger.warning("Kết quả truy vấn là null");
            return;
        }

        logger.info(String.format("""
            Kết quả truy vấn:
            - Số lượng bản ghi: %d
            - Thời gian hoàn thành: %s
            """,
                results.size(),
                LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ));
    }
}