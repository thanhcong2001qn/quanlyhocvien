package com.dacs.quanlyhocvien.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {
    @GetMapping(value = "/admin")
    public String admin(){
        return "views/admin/admin";
    }
    @GetMapping(value = "/AllStudent")
    public String allStudent(){
        return "views/admin/AllStudent";
    }
}
