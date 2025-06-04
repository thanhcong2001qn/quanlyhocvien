package com.dacs.quanlyhocvien.models;
import com.dacs.quanlyhocvien.models.ColumnMapping;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Kết quả ánh xạ alias từ người dùng sang bảng và cột SQL, hỗ trợ nhiều bảng.
 */
public class AliasResolutionResult {

    private Set<String> tables; // tên các bảng liên quan
    private List<ColumnMapping> columns; // ánh xạ alias → canonical + bảng

    public AliasResolutionResult(Set<String> tables, List<ColumnMapping> columns) {
        this.tables = tables;
        this.columns = columns;
    }

    public Set<String> getTables() {
        return tables;
    }

    public List<ColumnMapping> getColumns() {
        return columns;
    }



    /**
     * Truy cập tên cột chuẩn (canonical) từ alias.
     */
    public Optional<String> getCanonicalByAlias(String alias) {
        return columns.stream()
                .filter(c -> c.alias().equalsIgnoreCase(alias))
                .map(ColumnMapping::canonical)
                .findFirst();
    }

    /**
     * Truy bảng chứa alias đó (nếu có).
     */
    public Optional<String> getTableOfAlias(String alias) {
        return columns.stream()
                .filter(c -> c.alias().equalsIgnoreCase(alias))
                .map(ColumnMapping::table)
                .findFirst();
    }

    /**
     * In log rõ ràng: danh sách bảng và ánh xạ alias → cột.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("📘 Tables: ").append(tables).append("\n");
        sb.append("📗 Aliases:\n");
        for (ColumnMapping c : columns) {
            sb.append("- ").append(c.alias())
                    .append(" → ").append(c.canonical());
            if (c.table() != null) {
                sb.append(" (").append(c.table()).append(")");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Dữ liệu ánh xạ alias → canonical + bảng
     */
    public record ColumnMapping(String alias, String canonical, String table) {
        public String fullName() {
            return (table != null ? table + "." : "") + canonical;
        }
    }
}
