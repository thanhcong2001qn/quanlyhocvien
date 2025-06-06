package com.dacs.quanlyhocvien.Embedding.dto;

import java.util.List;

public class EmbeddingEntry {
    public String type;      // table | column
    public String name;      // table_name | table.column
    public String description;
    public List<Float> embedding;

    public EmbeddingEntry(String type, String name, String description, List<Float> embedding) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.embedding = embedding;
    }
}
