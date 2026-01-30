package com.dacs.quanlyhocvien.Embedding;

import com.dacs.quanlyhocvien.Services.Extractors.AliasExtractionService;
import com.dacs.quanlyhocvien.models.AliasResolutionResult;
import com.dacs.quanlyhocvien.models.AliasResolutionResult.ColumnMapping;
import com.dacs.quanlyhocvien.models.ResolvedAlias;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.logging.Logger;

@Component
public class SemanticAliasResolver {
    private static final Logger logger = Logger.getLogger(SemanticAliasResolver.class.getName());

    @Autowired
    private SemanticMatcher semanticMatcher;

    @Autowired
    private AliasExtractionService aliasExtractionService;

    /**
     * Resolve toàn bộ alias trong câu hỏi → trả về AliasResolutionResult chuẩn hóa
     */
    public AliasResolutionResult resolveAll(String userInput) {
        List<String> aliases = aliasExtractionService.extractAliases(userInput);
        Set<String> tables = new LinkedHashSet<>();
        List<ColumnMapping> columnMappings = new ArrayList<>();

        for (String alias : aliases) {
            Optional<ColumnMapping> mapping = resolveSingle(alias);
            if (mapping.isPresent()) {
                ColumnMapping cm = mapping.get();
                columnMappings.add(cm);
                if (cm.table() != null) tables.add(cm.table());
                logger.info("✅ Alias '" + alias + "' → " + cm.fullName());
            } else {
                // Fallback: giữ nguyên alias nếu không map được
                columnMappings.add(new ColumnMapping(alias, alias, null));
                logger.warning("⚠️ Không ánh xạ được alias: " + alias);
            }
        }

        return new AliasResolutionResult(tables, columnMappings);
    }

    /**
     * Resolve 1 alias duy nhất → ColumnMapping
     */
    public Optional<ColumnMapping> resolveSingle(String alias) {
        String cleaned = alias.trim().toLowerCase();

        // 1. Try embedding (SemanticMatcher)
        ResolvedAlias resolved = semanticMatcher.match(cleaned);
        if (resolved != null) {
            return Optional.of(new ColumnMapping(
                    resolved.getAlias(),
                    resolved.getCanonical(),
                    extractTableName(resolved.getCanonical())
            ));
        }

        // 2. Không tìm được
        return Optional.empty();
    }

    private String extractTableName(String canonical) {
        if (canonical.contains(".")) {
            return canonical.split("\\.")[0];
        } else if (canonical.contains("_")) {
            return canonical.split("_")[0];
        }
        return null;
    }

    public Optional<String> resolve(String alias) {
        return resolveSingle(alias).map(ColumnMapping::canonical);
    }
}