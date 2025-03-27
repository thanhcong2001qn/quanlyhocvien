package com.dacs.quanlyhocvien.Controllers.user;

import com.dacs.quanlyhocvien.DTO.Request.ChangePasswordRequest;
import com.dacs.quanlyhocvien.DTO.Request.UpdateStudentRequest;
import com.dacs.quanlyhocvien.Services.StudentService;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.models.StudentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping (value = "/api/user")
public class UserAPIController {

    @Autowired
    private StudentService studentService;

    @GetMapping(value = "/profile")
    public ResponseEntity<?> profile() {
        try {
            // Get authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            // Get student information
            StudentModel student = studentService.getStudentByUserName(username);
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student profile not found");
            }

            // Build profile response
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("fullName", student.getAccount().getFullName());
            profileData.put("studentId", student.getStudentId());
            profileData.put("email", student.getAccount().getEmail());
            if (!student.getAccount().getIsEmailVerified()){
                profileData.put("isEmailVerified", false);
            }
            profileData.put("gender", student.getAccount().getGender());
            profileData.put("phone", student.getAccount().getPhoneNumber());
            profileData.put("joinDate", student.getAccount().getCreatedAt());
            //profileData.put("profileImage", student.getAccount().getProfileImage());
            profileData.put("dateOfBirth", student.getAccount().getDateOfBirth());
            profileData.put("address", student.getAccount().getAddress());

            return ResponseEntity.ok(profileData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving profile: " + e.getMessage());
        }
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateStudentRequest profileData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            StudentModel updatedStudent = studentService.updateStudentByUserName(username, profileData);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating profile: " + e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest passwordData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            boolean success = studentService.changePassword(username, passwordData);
            if (!success) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current password is incorrect");
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error changing password: " + e.getMessage());
        }
    }
}