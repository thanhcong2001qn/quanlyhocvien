package com.dacs.quanlyhocvien.Services.Clients;

import com.dacs.quanlyhocvien.Exceptions.ChatbotException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class OpenChatApiClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "Bearer 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"; // 🔐 Đổi thành key của bạn

    public String getResponse(String prompt, String schemaJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", API_KEY);
        headers.set("HTTP-Referer", "http://localhost:8080");
        headers.set("X-Title", "HocVien4.0 Assistant");

        String fullPrompt = String.format("""
        You are an assistant that answers natural language questions by generating SQL queries based on the database schema.
        
        Database Schema:
        %s
        
        Question:
        %s
        
        Please return a valid JSON object with the following fields:
        - intent: always "DATABASE_QUERY"
        - sql: the SQL query
        - tables: list of table names used
        - columns: list of column names used
        - alias_mapping: map of original aliases to actual column/table names
        - summary: short Vietnamese description of the query
        """, schemaJson, prompt);

        Map<String, Object> body = Map.of(
                "model", "mistralai/mistral-7b-instruct:free",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful SQL assistant."),
                        Map.of("role", "user", "content", fullPrompt)
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);
            System.out.println("🧾 Response OpenChat:\n" + response.getBody());
            return response.getBody();
        } catch (Exception e) {
            throw new ChatbotException("ERR_OPENCHAT", "❌ Lỗi gọi OpenChat: " + e.getMessage(), e.getMessage());
        }
    }
}
