package com.dacs.quanlyhocvien.Controllers.Chatbot;

import com.dacs.quanlyhocvien.Services.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/query")
    public ResponseEntity<Map<String, String>> handleQuery(@RequestBody Map<String, String> request) {
        String userQuestion = request.get("question");
        String response = chatbotService.processQuery(userQuestion);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("response", response);

        return ResponseEntity.ok(responseBody);
    }
}