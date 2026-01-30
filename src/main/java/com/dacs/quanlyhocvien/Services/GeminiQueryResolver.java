package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Exceptions.ChatbotException;
import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import com.dacs.quanlyhocvien.Utils.SmartUnsafeCommandDetector;
import com.dacs.quanlyhocvien.models.ResolvedQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.dacs.quanlyhocvien.Services.PromptBuilder;
import org.springframework.stereotype.Service;

@Service
public class GeminiQueryResolver {

    @Autowired
    private GeminiApiClient geminiApiClient;

    @Autowired
    private PromptBuilder promptBuilder;

    public ResolvedQuery resolve(String userQuestion, String schemaJson) {
        String cleaned = ""; // ✅ tránh lỗi nếu exception xảy ra sớm
        try {
            String prompt = promptBuilder.buildUnifiedPrompt(userQuestion, schemaJson);
            String rawText = geminiApiClient.getResponse(prompt);

            System.out.println("Gemini text raw = \n" + rawText);

            cleaned = rawText
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```", "")
                    .trim();

            System.out.println("🔍 Cleaned JSON:\n" + cleaned);

            if (!isValidJson(cleaned)) {
                throw new RuntimeException("❌ Kết quả từ Gemini không phải JSON hợp lệ:\n" + cleaned);
            }

            System.out.println("🔧 Prompt gửi lên Gemini:\n" + prompt);

            ObjectMapper mapper = new ObjectMapper();
            ResolvedQuery resolved = mapper.readValue(cleaned, ResolvedQuery.class);

            // ✅ Ghi đè nếu truy vấn nguy hiểm
            if (SmartUnsafeCommandDetector.isUnsafe(userQuestion)) {
                resolved.setIntent("UNSAFE_COMMAND");
            }

            return resolved;

        } catch (Exception e) {
            e.printStackTrace();
            throw new ChatbotException("LLM_CALL_FAILED", "❌ Lỗi khi xử lý phản hồi từ Gemini", e.getMessage());
        }
    }


    private boolean isValidJson(String json) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
