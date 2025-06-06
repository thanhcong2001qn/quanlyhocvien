package com.dacs.quanlyhocvien.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PromptLoader {
    public static String loadPrompt(String path) {
        try {
            return Files.readString(Paths.get("src/main/resources/prompts/" + path));
        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc prompt: " + path, e);
        }
    }
}
