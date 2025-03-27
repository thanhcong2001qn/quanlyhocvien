package com.dacs.quanlyhocvien.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CaptchaService {

    private final RestTemplate restTemplate;

    @Value("${google.recaptcha.key.secret}")
    private String recaptchaSecret;

    @Value("${google.recaptcha.key.threshold}")
    private float threshold;

    public CaptchaService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public boolean validateCaptcha(String captchaResponse) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("secret", recaptchaSecret);
        formData.add("response", captchaResponse);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://www.google.com/recaptcha/api/siteverify",
                formData,
                Map.class
        );

        Map<String, Object> body = response.getBody();

        if (body == null || !Boolean.TRUE.equals(body.get("success"))) {
            return false;
        }

        // Kiểm tra điểm số (score) - chỉ áp dụng cho reCAPTCHA v3
        if (body.containsKey("score")) {
            float score = Float.parseFloat(body.get("score").toString());
            return score >= threshold;
        }

        return true;
    }
}