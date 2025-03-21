package com.dacs.quanlyhocvien.Controllers.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/user")
public class UserViewController {
    @GetMapping(value = "/profile")
    public String profile(){
        return "views/user/profile";
    }

}
