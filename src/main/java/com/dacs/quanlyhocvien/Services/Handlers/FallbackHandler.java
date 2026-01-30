package com.dacs.quanlyhocvien.Services.Handlers;

import org.springframework.stereotype.Component;

@Component
public class FallbackHandler {

    public String handle(String question) {
        return """
            Xin lỗi, hiện tại tôi chưa có đủ thông tin để trả lời câu hỏi này.
            Bạn có thể thử hỏi câu khác, hoặc liên hệ với quản trị viên để được hỗ trợ thêm nhé!
            """;
    }
}
