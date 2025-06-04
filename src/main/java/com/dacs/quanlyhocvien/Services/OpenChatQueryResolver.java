package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Exceptions.ChatbotException;
import com.dacs.quanlyhocvien.models.ResolvedQuery;
import com.dacs.quanlyhocvien.Services.Clients.OpenChatApiClient;
import com.dacs.quanlyhocvien.Utils.DatabaseSchemaExtractor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OpenChatQueryResolver {

    @Autowired private OpenChatApiClient openChatApiClient;
    @Autowired private DatabaseSchemaExtractor schemaExtractor;
    private static final Logger log = LoggerFactory.getLogger(OpenChatQueryResolver.class);
    @Autowired
    private ObjectMapper objectMapper;

    public ResolvedQuery resolve(String question, String schemaJson) {
        try {
            String rawResponse = openChatApiClient.getResponse(question, schemaJson);
            log.info("🧾 Response OpenChat:\n{}", rawResponse);

            JsonNode root = objectMapper.readTree(rawResponse);
            String content = root.at("/choices/0/message/content").asText().trim();

            // 🎯 Parse lại JSON thực trong content
            JsonNode result = objectMapper.readTree(content);
            return objectMapper.treeToValue(result, ResolvedQuery.class);

        } catch (Exception e) {
            throw new ChatbotException("ERR_PARSE_OPENCHAT", "❌ Lỗi phân tích phản hồi từ OpenChat: " + e.getMessage(), e.getMessage());
        }
    }


    private String extractJson(String response) {
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start == -1 || end == -1 || end <= start) {
            throw new RuntimeException("❌ Không tìm thấy JSON trong phản hồi OpenChat");
        }
        return response.substring(start, end + 1);
    }
}
