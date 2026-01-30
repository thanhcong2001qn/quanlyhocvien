package com.dacs.quanlyhocvien.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class StructuredPromptResponse {

    @JsonProperty("tables")
    private List<String> tables;

    @JsonProperty("columns")
    private List<AliasResolutionResult.ColumnMapping> columns;

    @JsonProperty("sql")
    private String sql;

    @JsonProperty("summary")
    private String summary;

    public StructuredPromptResponse() {}

    public List<String> getTables() {
        return tables;
    }

    public List<AliasResolutionResult.ColumnMapping> getColumns() {
        return columns;
    }

    public String getSql() {
        return sql;
    }

    public String getSummary() {
        return summary;
    }

    @Override
    public String toString() {
        return "📘 Tables: " + tables + "\n"
                + "📗 Columns: " + columns + "\n"
                + "📄 SQL: " + sql + "\n"
                + "💬 Summary: " + summary;
    }
}
