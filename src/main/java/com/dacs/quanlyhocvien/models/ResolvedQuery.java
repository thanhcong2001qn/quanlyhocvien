package com.dacs.quanlyhocvien.models;

import lombok.Data;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ResolvedQuery {
    private String intent;
    private String sql;
    private List<String> tables;
    private List<String> columns;
    private Map<String, String> alias_mapping;
    private String summary;
    private String answer; // ✅ Thêm dòng này
}
