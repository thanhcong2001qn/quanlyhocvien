package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Services.Clients.GeminiApiClient;
import com.dacs.quanlyhocvien.Services.PromptBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeminiUnifiedService {

    @Autowired
    private GeminiApiClient geminiApiClient;

    @Autowired
    private PromptBuilder promptBuilder;

    public String handleUserQuery(String question) {
        String fullPrompt = promptBuilder.buildUnifiedPrompt(question);
        return geminiApiClient.getResponse(fullPrompt);
    }
}
