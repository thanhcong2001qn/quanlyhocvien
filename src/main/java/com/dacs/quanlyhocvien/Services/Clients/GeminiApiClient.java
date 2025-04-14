package com.dacs.quanlyhocvien.Services.Clients;

import com.dacs.quanlyhocvien.config.GeminiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;
import java.util.logging.Logger;

@Component
public class GeminiApiClient {
    private static final Logger logger = Logger.getLogger(GeminiApiClient.class.getName());

    @Autowired
    private GeminiConfig geminiConfig;

    @Autowired
    private RestTemplate restTemplate;

    public String getResponse(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", geminiConfig.getApiKey());

        Map<String, Object> request = buildRequest(prompt);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            Map response = restTemplate.postForObject(
                    geminiConfig.getApiUrl(),
                    entity,
                    Map.class
            );
            return extractTextFromResponse(response);
        } catch (Exception e) {
            logger.severe("Lỗi khi gọi Gemini API: " + e.getMessage());
            throw new RuntimeException("Không thể kết nối với Gemini API", e);
        }
    }

    private Map<String, Object> buildRequest(String prompt) {
        // Thêm hướng dẫn về giọng điệu
        String wrappedPrompt = """
        Bạn là một trợ lý AI vui vẻ và thân thiện. 
        Hãy trả lời ngắn gọn, tự nhiên như khi trò chuyện với bạn bè.
        Tránh trả lời dài dòng như sách hoặc tài liệu kỹ thuật.
        
        Dưới đây là câu hỏi từ người dùng:
        """ + prompt;

        Map<String, Object> part = new HashMap<>();
        part.put("text", wrappedPrompt);

        Map<String, Object> content = new HashMap<>();
        content.put("role", "user");
        content.put("parts", List.of(part));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.5); // 0.5 -> sáng tạo hơn
        generationConfig.put("topK", 40);
        generationConfig.put("topP", 0.9);
        generationConfig.put("maxOutputTokens", 512); // Giới hạn bớt độ dài

        Map<String, Object> request = new HashMap<>();
        request.put("contents", List.of(content));
        request.put("generationConfig", generationConfig);

        return request;
    }


    private String extractTextFromResponse(Map response) {
        if (response == null) {
            logger.warning("Nhận được response null từ API");
            return "Không nhận được phản hồi từ API.";
        }

        try {
            // Kiểm tra và trích xuất text từ response
            if (response.containsKey("candidates")) {
                List<Map> candidates = (List<Map>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map candidate = candidates.get(0);
                    Map content = (Map) candidate.get("content");
                    List<Map> parts = (List<Map>) content.get("parts");
                    if (!parts.isEmpty()) {
                        Object text = parts.get(0).get("text");
                        if (text != null) {
                            return text.toString();
                        }
                    }
                }
            }

            logger.warning("Không tìm thấy text trong response: " + response);
            return "Không thể trích xuất văn bản từ phản hồi.";

        } catch (Exception e) {
            logger.severe("Lỗi khi xử lý response: " + e.getMessage());
            return "Lỗi khi xử lý phản hồi từ API: " + e.getMessage();
        }
    }
}