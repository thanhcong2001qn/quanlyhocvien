package com.dacs.quanlyhocvien.Utils;

import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SmartEntityExtractor {

    @Autowired
    private GeminiApiClient geminiApiClient;

    public Set<String> extractMeaningfulEntities(String question, String sqlContext) {
        try {
            // 🔍 Prompt ban đầu để trích thực thể
            String prompt = """
            Bạn là một AI hỗ trợ truy vấn cơ sở dữ liệu.

            Người dùng vừa đặt câu hỏi:
            "%s"

            SQL đã được sinh ra từ câu hỏi trên là:
            "%s"

            Nhiệm vụ của bạn:
            - Phân tích câu hỏi và trích xuất các giá trị cụ thể (ví dụ: số, tên, ID, thời gian, từ khóa, cụm từ cụ thể mà người dùng cần truy vấn).
            - Không liệt kê các từ/cụm chung chung hoặc tên bảng đã có trong SQL (ví dụ: nếu SQL có FROM course thì không cần nhắc lại "khoá học").
            - Không cần ghi rõ loại thực thể là gì. Chỉ cần trả ra **giá trị cụ thể cần kiểm tra**.
            - Không liệt kê từ dừng (như: các, trong, là, và, hoặc...).

            Chỉ trả về danh sách các thực thể phân tách bằng dấu phẩy.
        """.formatted(question, sqlContext);

            String rawResponse = geminiApiClient.getResponse(prompt).trim();

            List<String> rawEntities = Arrays.stream(rawResponse.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toList());

            if (rawEntities.isEmpty()) return Set.of();

            // 🔁 Prompt refinement để loại bỏ mô tả/trạng thái
            String refinementPrompt = """
            Bạn vừa trích xuất được các thực thể từ câu hỏi người dùng: "%s"

            Danh sách thực thể hiện tại: %s

            Nhiệm vụ của bạn:
            - Chỉ giữ lại những thực thể **mang tính định danh cụ thể** (ví dụ: tên, số, thời gian, từ khóa).
            - Loại bỏ những cụm từ chung chung, mô tả, trạng thái như "hiện có", "đang hoạt động", "còn mở",...
            - Không viết gì thêm, chỉ trả lại danh sách các thực thể còn lại, phân tách bằng dấu phẩy.
        """.formatted(question, String.join(", ", rawEntities));

            String refinedResponse = geminiApiClient.getResponse(refinementPrompt).trim();

            return Arrays.stream(refinedResponse.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toSet());

        } catch (Exception e) {
            return Set.of();
        }
    }
    public Set<String> extractAliasOnly(String question) {
        try {
            String prompt = """
        Người dùng vừa hỏi: "%s"
        Hãy trích ra các từ viết tắt, từ khóa ngắn có thể là alias trong cơ sở dữ liệu (vd: hs, gv, lop, kh, ...).
        - Chỉ cần các từ có khả năng ánh xạ sang bảng/cột.
        - Không viết thêm gì. Trả danh sách các alias, cách nhau bằng dấu phẩy.
        """.formatted(question);

            String raw = geminiApiClient.getResponse(prompt);
            return Arrays.stream(raw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toSet());

        } catch (Exception e) {
            return Set.of(); // fallback an toàn
        }
    }
}
