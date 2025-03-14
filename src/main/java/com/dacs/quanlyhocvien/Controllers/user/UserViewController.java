package com.dacs.quanlyhocvien.Controllers.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserViewController {
    @GetMapping(value = "/user")
    public String user(){
        return "views/user/user";
    }
}
