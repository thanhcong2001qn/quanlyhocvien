package com.dacs.quanlyhocvien.Utils;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

public class NormalizationLoader {

    public static Map<String, String> load() {
        try (InputStream in = NormalizationLoader.class.getClassLoader()
                .getResourceAsStream("normalization.yaml")) {
            if (in == null) return Collections.emptyMap();
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(in);
            return (Map<String, String>) root.getOrDefault("normalize", Collections.emptyMap());
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
