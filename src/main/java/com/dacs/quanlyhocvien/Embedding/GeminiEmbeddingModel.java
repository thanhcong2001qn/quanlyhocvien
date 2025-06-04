package com.dacs.quanlyhocvien.Embedding;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.http.*;
import java.util.*;
import com.fasterxml.jackson.databind.*;

@Component
public class GeminiEmbeddingModel implements EmbeddingModel {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/embedding-001:embedContent";

    @Override
    public List<Float> getEmbedding(String text) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> requestBody = Map.of(
                "content", Map.of(
                        "parts", List.of(
                                Map.of("text", text)
                        )
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("❌ Gemini error: " + response.body());
        }

        String responseBody = response.body();
        System.out.println("📥 Gemini raw embedding response: " + responseBody);

        Map<String, Object> result = mapper.readValue(responseBody, Map.class);

        Map<String, Object> embedding = (Map<String, Object>) result.get("embedding");
        List<Double> values = (List<Double>) embedding.get("values");

        return values.stream().map(Double::floatValue).toList();
    }
}
