package com.dacs.quanlyhocvien.Controllers.user;

import com.dacs.quanlyhocvien.Services.EnrollmentService;
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
@RequestMapping("/api/enrollments")
public class EnrollmentApiController {

    private final EnrollmentService enrollmentService;

    @Autowired
    public EnrollmentApiController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /**
     * Kiểm tra người dùng đã đăng ký khóa học chưa
     */
    @GetMapping ("/check/{courseId}")
    public ResponseEntity<Map<String, Object>> checkEnrollment(@PathVariable Long courseId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            boolean isEnrolled = enrollmentService.isUserEnrolled(courseId, username);
            Map<String, Object> response = new HashMap<>();
            response.put("enrolled", isEnrolled);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("enrolled", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Đăng ký khóa học
     */
    @PostMapping("/enroll")
    public ResponseEntity<Map<String, Object>> enrollCourse(@RequestBody Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            Long courseId = Long.parseLong(request.get("courseId").toString());
            boolean success = enrollmentService.enrollCourse(courseId,username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            if (!success) {
                response.put("message", "Không thể đăng ký khóa học.");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}