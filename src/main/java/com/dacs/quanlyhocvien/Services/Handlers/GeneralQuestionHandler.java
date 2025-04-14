package com.dacs.quanlyhocvien.Services.Handlers;

import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class GeneralQuestionHandler {

    @Autowired
    private GeminiApiClient geminiApiClient;

    private String knowledgeBase;

    private static final List<String> queryKeywords = List.of(
            "bao nhiêu", "có bao nhiêu", "liệt kê", "tìm", "tìm kiếm", "truy vấn",
            "danh sách", "đếm", "số lượng", "hiển thị", "thống kê", "xem", "có tất cả",
            "gồm những", "list", "show", "count", "display", "how many", "tổng số", "bao gồm",
            "có bao nhiêu", "số lượng account", "số lượng học viên", "dữ liệu", "record", "records",
            "tính toán", "có những", "những ai", "kể tên", "ai đang", "gồm", "những gì"
    );


    public GeneralQuestionHandler() {
        try {
            this.knowledgeBase = loadKnowledgeBase();
        } catch (IOException e) {
            e.printStackTrace();
            this.knowledgeBase = "Xin lỗi, tôi chưa có dữ liệu hỗ trợ.";
        }
    }

    private String loadKnowledgeBase() throws IOException {
        Path path = Paths.get("src/main/resources/knowledge_base.txt");
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    /**
     * Nếu là câu hỏi chung, dùng AI trả lời dựa trên document
     */
    public String matchGeneralQuestion(String userQuestion) {
        try {
            String normalized = userQuestion.toLowerCase();

            // 1. Nếu câu hỏi chứa keyword dữ liệu ➔ KHÔNG phải câu hỏi tự nhiên
            for (String keyword : queryKeywords) {
                if (normalized.contains(keyword)) {
                    return null;  // Không được xử lý bằng Knowledge Base
                }
            }

            // 2. Nếu không có keyword dữ liệu ➔ cho AI đọc tài liệu Knowledge Base
            String prompt = String.format("""
            Dựa trên nội dung kiến thức sau:
            
            %s
            
            Trả lời ngắn gọn, thân thiện cho câu hỏi:
            "%s"
            
            Nếu câu hỏi không liên quan đến kiến thức trên, trả lời "null".
            """, knowledgeBase, userQuestion);

            String answer = geminiApiClient.getResponse(prompt);

            // 3. Nếu AI trả về "null" nghĩa là không tìm thấy câu trả lời
            if (answer.trim().equalsIgnoreCase("null")) {
                return null;
            }

            return answer;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
