package com.dacs.quanlyhocvien.Utils;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatabaseSchemaExtractor {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaExtractor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getCompleteSchema() {
        StringBuilder schema = new StringBuilder();

        // Lấy danh sách tất cả các bảng trong schema hiện tại
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE() ORDER BY table_name",
                String.class
        );

        for (String table : tables) {
            schema.append("-- Table: ").append(table).append("\n");
            schema.append("CREATE TABLE ").append(table).append(" (\n");

            // Lấy thông tin về các cột
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                    "SELECT column_name, data_type, column_key, is_nullable, column_default, extra " +
                            "FROM information_schema.columns " +
                            "WHERE table_schema = DATABASE() AND table_name = ? " +
                            "ORDER BY ordinal_position",
                    table
            );

            for (int i = 0; i < columns.size(); i++) {
                Map<String, Object> column = columns.get(i);
                schema.append("    ").append(column.get("column_name")).append(" ");
                schema.append(column.get("data_type"));

                // Thêm các ràng buộc
                if ("NO".equals(column.get("is_nullable"))) {
                    schema.append(" NOT NULL");
                }
                if (column.get("column_default") != null) {
                    schema.append(" DEFAULT ").append(column.get("column_default"));
                }
                if ("PRI".equals(column.get("column_key"))) {
                    schema.append(" PRIMARY KEY");
                }
                if (column.get("extra") != null && !column.get("extra").toString().isEmpty()) {
                    schema.append(" ").append(column.get("extra"));
                }

                if (i < columns.size() - 1) {
                    schema.append(",");
                }
                schema.append("\n");
            }

            // Thêm thông tin về khóa ngoại
            List<Map<String, Object>> foreignKeys = jdbcTemplate.queryForList(
                    "SELECT k.column_name, k.referenced_table_name, k.referenced_column_name " +
                            "FROM information_schema.key_column_usage k " +
                            "JOIN information_schema.table_constraints t " +
                            "ON k.constraint_name = t.constraint_name " +
                            "WHERE k.table_schema = DATABASE() " +
                            "AND k.table_name = ? " +
                            "AND t.constraint_type = 'FOREIGN KEY'",
                    table
            );

            if (!foreignKeys.isEmpty() && !columns.isEmpty()) {
                schema.append(",\n");
                for (int i = 0; i < foreignKeys.size(); i++) {
                    Map<String, Object> fk = foreignKeys.get(i);
                    schema.append("    FOREIGN KEY (").append(fk.get("column_name")).append(") ");
                    schema.append("REFERENCES ").append(fk.get("referenced_table_name")).append(" (");
                    schema.append(fk.get("referenced_column_name")).append(")");

                    if (i < foreignKeys.size() - 1) {
                        schema.append(",");
                    }
                    schema.append("\n");
                }
            }

            schema.append(");\n\n");
        }

        return schema.toString();
    }
}