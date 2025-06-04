package com.dacs.quanlyhocvien.models;

public class ResolvedAlias {
    private String alias;         // Từ người dùng nhập
    private String canonical;     // Tên chuẩn (trong DB)
    private String description;   // Mô tả dùng cho AI

    public ResolvedAlias(String alias, String canonical, String description) {
        this.alias = alias;
        this.canonical = canonical;
        this.description = description;
    }

    public String getAlias() {
        return alias;
    }

    public String getCanonical() {
        return canonical;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ResolvedAlias{" +
                "alias='" + alias + '\'' +
                ", canonical='" + canonical + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
