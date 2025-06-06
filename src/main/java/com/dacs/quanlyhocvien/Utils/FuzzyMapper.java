package com.dacs.quanlyhocvien.Utils;

import org.apache.commons.text.similarity.LevenshteinDistance;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FuzzyMapper {

    private static final LevenshteinDistance distance = new LevenshteinDistance();

    public static String findBestMatch(String userInput, List<String> options) {
        return options.stream()
                .min(Comparator.comparingInt(option -> distance.apply(userInput.toLowerCase(), option.toLowerCase())))
                .orElse(null);
    }

    public static String findBestTable(String userInput, Map<String, List<String>> tableColumnMap) {
        return findBestMatch(userInput, new ArrayList<>(tableColumnMap.keySet()));
    }

    public static String findBestColumn(String userInput, List<String> columnList) {
        return findBestMatch(userInput, columnList);
    }
}
