package com.dacs.quanlyhocvien.Controllers.Login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthViewController {
    @GetMapping(value = "/register")
    public String register(){
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
