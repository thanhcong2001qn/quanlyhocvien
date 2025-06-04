package com.dacs.quanlyhocvien.Embedding;

import com.dacs.quanlyhocvien.models.ResolvedAlias;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.*;

public class FuzzyMatcher {

    private static final List<ResolvedAlias> aliasList = new ArrayList<>();
    private static final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();
    private static final double THRESHOLD = 0.80;

    static {
        try (InputStream input = FuzzyMatcher.class.getResourceAsStream("/alias/alias_data.csv");
             CSVReader reader = new CSVReader(new InputStreamReader(input))) {
            String[] line;
            reader.readNext(); // skip header
            while ((line = reader.readNext()) != null) {
                String name = line[1].toLowerCase();        // canonical
                String desc = line[2];
                aliasList.add(new ResolvedAlias(name, name, desc));
            }
        } catch (Exception e) {
            throw new RuntimeException("Không thể load alias_data.csv", e);
        }
    }

    public static ResolvedAlias findClosest(String alias) {
        System.out.println("🔎 FuzzyMatcher: tìm alias gần nhất với: " + alias);
        alias = alias.trim().toLowerCase();
        double maxScore = 0;
        ResolvedAlias bestMatch = null;

        for (ResolvedAlias ra : aliasList) {
            double score = similarity.apply(alias, ra.getCanonical().toLowerCase());
            if (score > maxScore && score >= THRESHOLD) {
                maxScore = score;
                bestMatch = ra;
            }
        }
        if (bestMatch != null) {
            System.out.println("✅ Fuzzy match: " + alias + " → " + bestMatch.getCanonical());
        } else {
            System.out.println("❌ Fuzzy không tìm được alias gần: " + alias);
        }
        return bestMatch;
    }
}
