package com.dacs.quanlyhocvien.Embedding;

import com.dacs.quanlyhocvien.models.ResolvedAlias;
import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import com.opencsv.CSVReader;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Component
public class SemanticMatcher {

    private static final double THRESHOLD = 0.80;

    private final GeminiApiClient geminiApiClient;
    private final List<ResolvedAlias> aliasList = new ArrayList<>();
    private final Map<String, double[]> embeddingMap = new HashMap<>();


    @Autowired
    public SemanticMatcher(GeminiApiClient geminiApiClient) {
        this.geminiApiClient = geminiApiClient;
    }
    @PostConstruct
    public void loadAliasAndEmbeddings() {
        try (InputStream input = SemanticMatcher.class.getResourceAsStream("/alias/alias_data.csv")) {

            if (input == null) {
                throw new RuntimeException("Không tìm thấy file /alias/alias_data.csv trong resources");
            }

            try (CSVReader reader = new CSVReader(new InputStreamReader(input))) {
                String[] line;
                reader.readNext(); // skip header
                while ((line = reader.readNext()) != null) {
                    String canonical = line[1].toLowerCase();
                    String desc = line[2];

                    ResolvedAlias resolved = new ResolvedAlias(canonical, canonical, desc);
                    aliasList.add(resolved);

                    List<Double> embedList = geminiApiClient.getEmbedding(canonical);
                    double[] embedArray = embedList.stream().mapToDouble(Double::doubleValue).toArray();
                    embeddingMap.put(canonical, embedArray);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi load alias và embedding", e);
        }
    }


    public ResolvedAlias match(String input) {
        System.out.println("🧠 SemanticMatcher: tính embedding và tìm alias cho: " + input);
        List<Double> inputEmbedList = geminiApiClient.getEmbedding(input);
        double[] inputVector = inputEmbedList.stream().mapToDouble(Double::doubleValue).toArray();

        double bestScore = 0.0;
        ResolvedAlias bestMatch = null;

        for (ResolvedAlias candidate : aliasList) {
            double[] candidateVector = embeddingMap.get(candidate.getCanonical());
            if (candidateVector == null) continue;

            double similarity = cosineSimilarity(inputVector, candidateVector);
            if (similarity > bestScore && similarity >= THRESHOLD) {
                bestScore = similarity;
                bestMatch = candidate;
            }
        }
        if (bestMatch != null) {
            System.out.println("✅ Embedding match: " + input + " → " + bestMatch.getCanonical() + " (score = " + bestScore + ")");
        } else {
            System.out.println("❌ Không tìm thấy embedding match cho: " + input);
        }
        return bestMatch;
    }

    private double cosineSimilarity(double[] a, double[] b) {
        RealVector v1 = new ArrayRealVector(a);
        RealVector v2 = new ArrayRealVector(b);
        return v1.dotProduct(v2) / (v1.getNorm() * v2.getNorm() + 1e-10);
    }
}
