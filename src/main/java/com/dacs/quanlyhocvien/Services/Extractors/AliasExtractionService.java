package com.dacs.quanlyhocvien.Services.Extractors;

import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AliasExtractionService {

    @Autowired
    private GeminiApiClient gemini;

    public List<String> extractAliases(String userInput) {
        try {
            String prompt = """
                Người dùng vừa nhập một câu có thể chứa nhiều alias (viết tắt hoặc cụm truy vấn).
                Hãy trích xuất danh sách các alias độc lập từ câu đó và trả về **mảng JSON thuần**.

                ✨ Ví dụ:
                - Input: "các kh html, js và python"
                - Output: ["kh html", "kh js", "kh python"]

                - Input: "email gv, sdt hv"
                - Output: ["email gv", "sdt hv"]
                
                - Input: "các khóa học html-js"
                - Output: ["khóa học html", "khóa học js"]
                
                - Input: "các học sinh lớp 1 2"
                - Output: ["học sinh lớp 1", "học sinh lớp 2"]
                
                - XEM THẬT KĨ CÁC VÍ DỤ ĐỂ HỌC VÀ LÀM TƯƠNG TỰ CHO CÁC TRƯỜNG HỢP KHÁC.
                - Input: "%s"
                - Output:
                """.formatted(userInput.trim());

            String json = gemini.getResponse(prompt).trim();

            if (json.startsWith("```")) {
                json = json.replaceAll("(?s)```json\\s*", "")
                        .replaceAll("(?s)```", "")
                        .trim();
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("❌ AliasExtraction error: " + e.getMessage());
            return List.of(userInput); // fallback: giữ nguyên input
        }
    }
}
