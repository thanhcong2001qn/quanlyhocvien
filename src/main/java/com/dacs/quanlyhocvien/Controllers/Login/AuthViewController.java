package com.dacs.quanlyhocvien.Controllers.Login;

import com.dacs.quanlyhocvien.Services.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthViewController {

    @Value("${google.recaptcha.key.site}")
    private String recaptchaSite;
    @Autowired
    private CaptchaService captchaService;
    @GetMapping(value = "/register")
    public String register(Model model){
        model.addAttribute("recaptchaSite", recaptchaSite);
        return "views/signIn/register";
    }
    @GetMapping(value = "/login")
    public String Login(){
        return "views/signIn/login";
    }
    @GetMapping(value = "/forgot-password")
    public String forgotPassword(){
        return "views/signIn/forgot-password";
    }
}
