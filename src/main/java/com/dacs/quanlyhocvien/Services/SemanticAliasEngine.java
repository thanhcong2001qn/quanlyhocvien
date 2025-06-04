package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.models.ResolvedAlias;
import com.dacs.quanlyhocvien.Utils.YamlContextResolver;
import com.dacs.quanlyhocvien.Embedding.FuzzyMatcher;
import com.dacs.quanlyhocvien.Embedding.SemanticMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SemanticAliasEngine {

    private final Map<String, ResolvedAlias> cache = new ConcurrentHashMap<>();

    @Autowired
    private SemanticMatcher semanticMatcher;

    public ResolvedAlias resolve(String rawAlias) {
        String alias = rawAlias.trim().toLowerCase();
        System.out.println("🔍 SemanticAliasEngine: Đang resolve alias = " + alias);

        // 1. YAML
        if (YamlContextResolver.contains(alias)) {
            ResolvedAlias yaml = YamlContextResolver.resolve(alias);
            System.out.println("✅ YAML matched: " + yaml);
            cache.put(alias, yaml);
            return yaml;
        }

        // 2. Cache
        if (cache.containsKey(alias)) {
            System.out.println("♻️ Cache hit: " + alias + " → " + cache.get(alias));
            return cache.get(alias);
        }

        // 3. Fuzzy
        ResolvedAlias fuzzy = FuzzyMatcher.findClosest(alias);
        if (fuzzy != null) {
            System.out.println("🔁 Fuzzy matched: " + alias + " → " + fuzzy);
            cache.put(alias, fuzzy);
            return fuzzy;
        }

        // 4. Semantic
        ResolvedAlias semantic = semanticMatcher.match(alias);
        if (semantic != null) {
            System.out.println("🧠 Embedding matched: " + alias + " → " + semantic);
            cache.put(alias, semantic);
            return semantic;
        }

        // 5. Không tìm thấy
        System.out.println("❌ Không tìm thấy alias: " + alias);
        return null;
    }


    public List<ResolvedAlias> resolveAll(Collection<String> aliases) {
        List<ResolvedAlias> result = new ArrayList<>();
        for (String alias : aliases) {
            ResolvedAlias resolved = resolve(alias);
            if (resolved != null) result.add(resolved);
        }
        return result;
    }

    public void clearCache() {
        cache.clear();
    }

    public Map<String, ResolvedAlias> getCache() {
        return cache;
    }

    public void cache(String alias, ResolvedAlias resolved) {
        cache.put(alias.trim().toLowerCase(), resolved);
    }
}
