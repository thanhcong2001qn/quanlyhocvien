package com.dacs.quanlyhocvien.Controllers.Login;

import com.dacs.quanlyhocvien.Services.CaptchaService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public String Login(HttpServletResponse response){
        // Thêm header chống cache cho tất cả các request
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            // Kiểm tra xem người dùng có vai trò admin không
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

            // Điều hướng dựa trên vai trò
            if (isAdmin) {
                return "redirect:/dashboard";
            } else {
                return "redirect:/home";
            }
        }
        return "views/signIn/login";
    }
    @GetMapping(value = "/forgot-password")
    public String forgotPassword(){
        return "views/signIn/forgot-password";
    }
}
