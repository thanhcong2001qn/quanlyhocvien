package com.dacs.quanlyhocvien.Utils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class MultiEntityQueryAnalyzer {

    public static Set<String> extractNamedEntities(String question) {
        if (question == null || question.isBlank()) return Set.of();

        return Arrays.stream(question.split("(,| và | hoặc )"))
                .map(String::trim)
                .filter(word -> word.length() >= 2)
                .collect(Collectors.toSet());
    }

    public static boolean isMultiEntityQuery(String question) {
        return extractNamedEntities(question).size() >= 2;
    }
}
