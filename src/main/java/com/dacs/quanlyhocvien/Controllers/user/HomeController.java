package com.dacs.quanlyhocvien.Controllers.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping(value = "/home")
public class HomeController {
    @GetMapping()
    public String home() {
        return "views/user/home";
    }
}
