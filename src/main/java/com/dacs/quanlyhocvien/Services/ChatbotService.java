package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Services.Executors.SafeQueryExecutor;
import com.dacs.quanlyhocvien.Services.Generators.SqlQueryGenerator;
import com.dacs.quanlyhocvien.Services.Handlers.GeneralQuestionHandler;   // <-- Thêm mới
import com.dacs.quanlyhocvien.Services.Formatters.ResponseFormatter;
import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    @Autowired
    private GeneralQuestionHandler generalQuestionHandler;    // <-- Thêm mới

    @Autowired
    private GeminiApiClient geminiApiClient;

    @Autowired
    private ResponseFormatter responseFormatter;

    @Autowired
    private SafeQueryExecutor safeQueryExecutor;

    @Autowired
    private SqlQueryGenerator sqlQueryGenerator;

    public String processQuery(String userQuestion) {
        try {
            // 1. Check câu hỏi tự nhiên trước
            String generalAnswer = generalQuestionHandler.matchGeneralQuestion(userQuestion);
            if (generalAnswer != null) {
                return formatAnswer(generalAnswer);
            }

            // 2. Nếu không phải câu hỏi tự nhiên ➔ Sinh SQL
            String sql = sqlQueryGenerator.generateIntentBasedSql(userQuestion);

            // 3. Validate SQL
            if (!safeQueryExecutor.isSafeSql(sql)) {
                return formatAnswer("❌ Xin lỗi, truy vấn này có thể ảnh hưởng đến hệ thống nên mình không thể thực hiện nha. 🛡️");
            }

            // 4. Thực thi SQL
            List<Map<String, Object>> results = safeQueryExecutor.safeExecute(sql);

            // 5. Nếu có kết quả
            if (results != null && !results.isEmpty()) {
                return responseFormatter.format(userQuestion, results);
            } else {
                return formatAnswer("\uD83D\uDCCB Mình đã kiểm tra nhưng hiện tại chưa có dữ liệu phù hợp với yêu cầu của bạn. 📋");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return formatAnswer("Mình gặp chút sự cố khi xử lý yêu cầu của bạn. Bạn thử hỏi lại sau nhé. 🙏");
        }
    }



    private String formatAnswer(String answer) {
        return "<div style='font-family: Arial, sans-serif; color: white;'>" +
                "<p>" + answer + "</p>" +
                "</div>";
    }
}
