package com.dacs.quanlyhocvien.Utils;

import com.dacs.quanlyhocvien.Embedding.SemanticMatcher;
import com.dacs.quanlyhocvien.models.AliasResolutionResult;
import com.dacs.quanlyhocvien.models.AliasResolutionResult.ColumnMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AliasResolutionService {

    @Autowired
    private SemanticMatcher semanticMatcher;

    public AliasResolutionResult resolveAlias(String userInput) {
        String normalized = NaturalLanguageNormalizer.normalize(userInput);

        // ✅ Gọi semantic matcher
        var resolved = semanticMatcher.match(normalized);

        if (resolved != null) {
            System.out.println("🔎 Alias input = " + userInput);
            System.out.println("➡️ Resolved via SemanticMatcher: " + resolved.getCanonical());

            String table = extractTableFromCanonical(resolved.getCanonical());

            ColumnMapping column = new ColumnMapping(
                    resolved.getAlias(),
                    resolved.getCanonical(),
                    table
            );

            return new AliasResolutionResult(
                    table != null ? Set.of(table) : Set.of(),
                    List.of(column)
            );
        }

        // ❌ Không match được
        System.out.println("❌ Không thể ánh xạ alias: " + userInput);
        return new AliasResolutionResult(Collections.emptySet(), Collections.emptyList());
    }

    private String extractTableFromCanonical(String canonical) {
        if (canonical.contains(".")) {
            return canonical.split("\\.")[0];
        } else if (canonical.contains("_")) {
            return canonical.split("_")[0];
        }
        return null;
    }
}
