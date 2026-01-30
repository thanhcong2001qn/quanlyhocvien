package com.dacs.quanlyhocvien.Utils;

import com.dacs.quanlyhocvien.models.AliasResolutionResult;
import com.dacs.quanlyhocvien.models.AliasResolutionResult.ColumnMapping;
import com.dacs.quanlyhocvien.models.StructuredPromptResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class GeminiAliasJsonParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static AliasResolutionResult parse(String rawJson) {
        String cleanedJson = rawJson.trim();
        try {
            // ✅ Làm sạch nếu có markdown ```json
            if (cleanedJson.startsWith("```")) {
                cleanedJson = cleanedJson.replaceAll("(?s)```json\\s*", "")
                        .replaceAll("(?s)```", "")
                        .trim();
            }

            // ✅ Nếu còn dư JSON: phía đầu là "json:..." → xóa tiếp
            if (cleanedJson.toLowerCase().startsWith("json")) {
                cleanedJson = cleanedJson.substring(4).trim();
            }

            StructuredPromptResponse response = mapper.readValue(cleanedJson, StructuredPromptResponse.class);

            // ✅ Nếu không có bảng hoặc SQL quá rỗng mới coi là lỗi nghiêm trọng
            if (response.getTables() == null || response.getTables().isEmpty()
                    || response.getSql() == null || response.getSql().isBlank()) {
                return null;
            }


            Set<String> tables = new LinkedHashSet<>(response.getTables());
            List<ColumnMapping> columns = response.getColumns() != null ? response.getColumns() : List.of();
            return new AliasResolutionResult(tables, columns);
        } catch (Exception e) {
            System.err.println("❌ Lỗi parse JSON từ Gemini: " + e.getMessage());
            System.err.println("📥 JSON đầu vào:\n" + rawJson);
            System.err.println("📥 JSON đã làm sạch:\n" + cleanedJson);
            return null;
        }
    }
}
