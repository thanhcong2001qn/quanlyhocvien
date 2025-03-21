package com.dacs.quanlyhocvien.Controllers.Login;

import com.dacs.quanlyhocvien.Services.AuthService;
import com.dacs.quanlyhocvien.Services.RegistrationService;
import com.dacs.quanlyhocvien.models.AccountModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class VerificationController {

    private final RegistrationService registrationService;
    private final AuthService authService;
    public VerificationController(RegistrationService registrationService, AuthService authService) {
        this.registrationService = registrationService;
        this.authService = authService;

    }

    @GetMapping("/verify-account")
    public String verifyAccount(@RequestParam("token") String token, Model model) {
        boolean isVerified = registrationService.verifyAccount(token);
        if (isVerified) {
            model.addAttribute("status", "success");
            model.addAttribute("message", "Your account has been successfully verified!");
        } else {
            model.addAttribute("status", "failed");
            model.addAttribute("message", "Invalid or expired verification token");
        }
        return "views/signIn/verify-account";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token, Model model) {
        boolean isVerified = authService.verifyToken(token);
        if(isVerified){
            model.addAttribute("status", "success");
            model.addAttribute("message", "Your account has been successfully verified!");
        }else{
            model.addAttribute("status", "failed");
            model.addAttribute("message", "Invalid or expired verification token");
        }
        return "views/signIn/reset-password";
    }
}
