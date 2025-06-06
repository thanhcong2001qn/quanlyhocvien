package com.dacs.quanlyhocvien.Utils;

import com.dacs.quanlyhocvien.Embedding.SemanticAliasResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class EntityCoverageAnalyzer {

    @Autowired
    private SmartEntityExtractor extractor;

    @Autowired
    private SemanticAliasResolver semanticAliasResolver;

    public Map<String, List<String>> analyze(String question, String sql, List<Map<String, Object>> result) {
        // 🔍 1. Trích các thực thể người dùng hỏi, ánh xạ alias
        Set<String> expectedValues = extractor.extractMeaningfulEntities(question, sql)
                .stream()
                .map(v -> semanticAliasResolver.resolve(v).orElse(v)) // dùng semantic ánh xạ alias
                .collect(Collectors.toSet());

        // 🔍 2. Tập hợp tất cả các giá trị trong kết quả DB (normalize mở rộng)
        Set<String> actualValues = result.stream()
                .flatMap(row -> row.values().stream())
                .filter(Objects::nonNull)
                .flatMap(val -> normalizeValueMulti(val.toString()).stream())
                .collect(Collectors.toSet());

        // 🔎 3. So sánh
        List<String> missing = expectedValues.stream()
                .filter(v -> !isCovered(v.toLowerCase(), actualValues))
                .toList();

        Map<String, List<String>> report = new HashMap<>();
        report.put("expected", new ArrayList<>(expectedValues));
        report.put("missing", missing);
        return report;
    }

    private boolean isCovered(String expected, Set<String> actualValues) {
        return actualValues.stream().anyMatch(a ->
                a.equalsIgnoreCase(expected) || a.contains(expected) || expected.contains(a)
        );
    }

    private Set<String> normalizeValueMulti(String raw) {
        Set<String> result = new LinkedHashSet<>();
        if (raw == null || raw.isBlank()) return result;

        raw = raw.trim().toLowerCase();
        result.add(raw);

        // ISO datetime → tách tháng, năm
        if (raw.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            try {
                int year = Integer.parseInt(raw.substring(0, 4));
                int month = Integer.parseInt(raw.substring(5, 7));
                result.add("tháng " + month);
                result.add(String.valueOf(year));
                result.add("tháng " + month + "|" + year);
            } catch (Exception ignored) {}
        }

        // dd/mm/yyyy
        if (raw.matches("\\d{1,2}/\\d{4}")) {
            String[] parts = raw.split("/");
            result.add("tháng " + Integer.parseInt(parts[0]));
            result.add(parts[1]);
            result.add("tháng " + parts[0] + "|" + parts[1]);
        }

        // plain year
        if (raw.matches("\\d{4}")) result.add(raw);

        // token từng từ
        String[] tokens = raw.split("\\s+");
        result.addAll(Arrays.asList(tokens));

        // split từ ghép: html-css → html, css
        if (raw.contains("-") || raw.contains("_")) {
            for (String part : raw.split("[-_]")) {
                result.add(part.trim());
            }
        }

        // tổ hợp 2 từ
        if (tokens.length >= 2) {
            for (int i = 0; i < tokens.length - 1; i++) {
                result.add(tokens[i] + " " + tokens[i + 1]);
            }
        }

        return result.stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
