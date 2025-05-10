package com.dacs.quanlyhocvien.Services.Validators;

import org.springframework.stereotype.Component;

@Component
public class SQLValidator {

    public boolean isSafe(String sql) {
        if (sql == null) return false;
        String lowerSql = sql.trim().toLowerCase();

        // Chỉ cho phép SELECT
        if (!lowerSql.startsWith("select")) {
            return false;
        }

        // Không cho phép các lệnh nguy hiểm
        String[] forbidden = {"insert", "update", "delete", "drop", "alter", "truncate"};
        for (String keyword : forbidden) {
            if (lowerSql.contains(keyword)) {
                return false;
            }
        }

        return true;
    }
}