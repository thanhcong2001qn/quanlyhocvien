package com.dacs.quanlyhocvien.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SignInController {
    @GetMapping(value = "/Login")
    public String admin(){
        return "views/SignIn/Login";
    }
    @GetMapping(value = "/register")
    public String register(){
        return "views/SignIn/register";
    }
}
