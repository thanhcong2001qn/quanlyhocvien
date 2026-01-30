package com.dacs.quanlyhocvien.Utils;

import com.dacs.quanlyhocvien.models.ResolvedAlias;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.LoaderOptions;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class YamlContextResolver {
    private static final String YAML_PATH = "/alias/alias_context.yaml";
    private static final Map<String, ResolvedAlias> aliasMap = new HashMap<>();

    static {
        try (InputStream rawInput = YamlContextResolver.class.getResourceAsStream(YAML_PATH)) {
            if (rawInput == null) {
                throw new RuntimeException("❌ Không tìm thấy file " + YAML_PATH);
            }

            try (InputStreamReader reader = new InputStreamReader(rawInput)) {
                Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
                Map<String, Map<String, String>> data = yaml.load(reader);

                if (data == null) {
                    throw new RuntimeException("❌ File YAML trống hoặc sai định dạng: " + YAML_PATH);
                }

                for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
                    String alias = entry.getKey();
                    Map<String, String> val = entry.getValue();
                    aliasMap.put(alias.toLowerCase(),
                            new ResolvedAlias(alias, val.get("canonical"), val.get("description")));
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Không thể load alias_context.yaml", e);
        }
    }

    public static boolean contains(String alias) {
        boolean found = aliasMap.containsKey(alias.toLowerCase());
        if (found) {
            System.out.println("📒 YAML contains: " + alias);
        }
        return found;
    }


    public static ResolvedAlias resolve(String alias) {
        return aliasMap.get(alias.toLowerCase());
    }

    public static Map<String, ResolvedAlias> getAll() {
        return aliasMap;
    }
}
