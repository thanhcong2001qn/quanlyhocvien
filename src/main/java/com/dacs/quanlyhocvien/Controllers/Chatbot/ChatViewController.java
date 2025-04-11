package com.dacs.quanlyhocvien.Controllers.Chatbot;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatViewController {

    @GetMapping("/chat")
    public String chatPage() {
        return "/views/chatbot/chatbot";
    }
}
