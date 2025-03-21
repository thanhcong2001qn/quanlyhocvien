package com.dacs.quanlyhocvien.Controllers.Login;

import com.dacs.quanlyhocvien.DTO.Request.LoginRequest;
import com.dacs.quanlyhocvien.DTO.Request.RegisterRequest;
import com.dacs.quanlyhocvien.Services.AccountService;
import com.dacs.quanlyhocvien.Services.AuthService;
import com.dacs.quanlyhocvien.Services.RegistrationService;
import com.dacs.quanlyhocvien.Services.StudentService;
import com.dacs.quanlyhocvien.config.JwtTokenProvider;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.models.StudentModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@CrossOrigin
@RequestMapping(value = "/api")
public class AuthController {
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private AuthService authService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping(value ="/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest){
        try {
            registrationService.registerStudent(registerRequest);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (IllegalArgumentException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }
    @PostMapping(value ="/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Tạo đối tượng Authentication - quan trọng nhất!
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );
            // Xác thực thông qua AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Lưu Authentication vào Security Context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Lấy thông tin người dùng đã xác thực
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String token = jwtTokenProvider.generateToken(authentication);

            // Tạo response bao gồm thông tin người dùng
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", userDetails.getUsername());

            // Xác định vai trò
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isTeacher = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
            boolean isStudent = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));

            response.put("isAdmin", isAdmin);
            response.put("isTeacher", isTeacher);
            response.put("isStudent", isStudent);

            // Trả về thông tin người dùng
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }  catch (Exception e) {
            return new ResponseEntity<>("Authentication failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String emailOrUsername = request.get("emailOrUsername");
        if (!authService.accountExists(emailOrUsername)) {
            return new ResponseEntity<>("Account not found", HttpStatus.NOT_FOUND);
        }
        new Thread(() -> {
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
    @GetMapping("/verify-token")
    public ResponseEntity<?> verifyToken(HttpServletRequest request) {
        try {
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                String jwt = bearerToken.substring(7);
                if (jwtTokenProvider.validateToken(jwt)) {
                    String username = jwtTokenProvider.getUsernameFromToken(jwt);
                    Map<String, Object> response = new HashMap<>();
                    response.put("username", username);
                    return ResponseEntity.ok(response);
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    @PostMapping(value = "/resend-verification-email")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody Map<String, String> request) {
        try {
            authService.resendVerificationToken(request.get("email"));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>("Error sending verification email: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
