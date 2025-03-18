package com.dacs.quanlyhocvien.Controllers.Login;

import com.dacs.quanlyhocvien.Services.AccountService;
import com.dacs.quanlyhocvien.Services.AuthService;
import com.dacs.quanlyhocvien.Services.RegistrationService;
import com.dacs.quanlyhocvien.Services.StudentService;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.models.StudentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api")
public class AuthController {

    public RegistrationService registrationService;
    public StudentService studentService;
    public AuthService authService;
    @Autowired
    public AuthController(RegistrationService registrationService , StudentService studentService, AuthService authService) {
        this.registrationService = registrationService;
        this.studentService = studentService;
        this.authService = authService;
    }
    @PostMapping(value ="/register")
    public ResponseEntity<?> register(@RequestBody AccountModel accountModel,StudentModel studentModel){
        if (studentService.getStudentByEmail(accountModel.getEmail()) == null && studentService.getStudentByUserName(accountModel.getUsername()) == null) {
            String token = registrationService.registerStudent(accountModel, studentModel);
            HttpHeaders headers = new HttpHeaders();
            ResponseEntity<?> response = new ResponseEntity<>(HttpStatus.OK);
            // Send email asynchronously so it doesn't block the response
            new Thread(() -> {
                registrationService.sendEmail(accountModel, token);
                headers.add("Location", "/verify-account");
            }).start();
            return response;
        }
        if (studentService.getStudentByUserName(accountModel.getUsername()) != null) {
            return new ResponseEntity<>("Username already exists", HttpStatus.CONFLICT);
        } else if (studentService.getStudentByEmail(accountModel.getEmail()) != null) {
            return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    @PostMapping(value ="/login")
    public ResponseEntity<?> login(@RequestBody AccountModel accountModel){
        if (!authService.checkLogin(accountModel.getUsername(), accountModel.getPassword())){
            return new ResponseEntity<>("Invalid username or password", HttpStatus.CONFLICT);
        }else if(!authService.checkIsVerifiedEmail(accountModel.getUsername())){
            return new ResponseEntity<>("Email is not verified", HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String emailOrUsername = request.get("emailOrUsername");
        // Check if account exists
        if (!authService.accountExists(emailOrUsername)) {
            return new ResponseEntity<>("Account not found", HttpStatus.NOT_FOUND);
        }
        HttpHeaders headers = new HttpHeaders();
        new Thread(() -> {
            headers.add("Location", "/reset-password");
            // Generate token and send email
            authService.sendPasswordResetEmail(emailOrUsername);
        }).start();
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping(value = "/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("password");
        if (!authService.resetPassword(token, newPassword)) {
            return new ResponseEntity<>("Invalid or expired token", HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
