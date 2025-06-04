package com.dacs.quanlyhocvien.Controllers.user;

import com.dacs.quanlyhocvien.DTO.Request.ChangePasswordRequest;
import com.dacs.quanlyhocvien.DTO.Request.UpdateStudentRequest;
import com.dacs.quanlyhocvien.DTO.Response.CategoryResponeDTO;
import com.dacs.quanlyhocvien.DTO.Response.CourseResponseDTO;
import com.dacs.quanlyhocvien.DTO.Response.UserStatsDTO;
import com.dacs.quanlyhocvien.Services.*;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.models.CourseCategoryModel;
import com.dacs.quanlyhocvien.models.StudentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping (value = "/api/user")
public class UserAPIController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private UserStatsService userStatsService;

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
    @GetMapping("/all-courses")
    public ResponseEntity<?> getAllCourse(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "6") int size,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam(required = false) String search,
                                          @RequestParam(required = false) String categories,
                                          @RequestParam(required = false) String levels,
                                          @RequestParam(required = false) String priceTypes,
                                          @RequestParam(required = false) String enrollmentStatus) {
        try {
            // Lấy thông tin người dùng hiện tại
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            AccountModel account = accountService.getAccountByUsername(username);
            if (account == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
            }

            // Tạo pageable từ các tham số phân trang và sắp xếp
            Pageable pageable = createPageable(page, size, sort);

            // Gọi service với các tham số lọc bao gồm cả trạng thái đăng ký
            Page<CourseResponseDTO> courses = courseService.getCourses(
                    pageable, search, categories, levels, priceTypes, enrollmentStatus, account.getAccountId());

            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving course: " + e.getMessage());
        }
    }
    @GetMapping("/all-categories")
    public ResponseEntity<?> getAllCategories() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            AccountModel account = accountService.getAccountByUsername(username);
            if (account == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
            }
            List<CourseCategoryModel> categoryEntities = categoryService.getAllCategories();
            List<CategoryResponeDTO> categoryDTOs = CategoryResponeDTO.fromEntities(categoryEntities);

            return ResponseEntity.ok(categoryDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving categories: " + e.getMessage());
        }
    }
    // Phương thức hỗ trợ xử lý sắp xếp
    private Pageable createPageable(int page, int size, String sort) {
        // Xử lý sắp xếp dựa trên chuỗi sort
        if (sort != null) {
            switch (sort) {
                case "newest":
                    return PageRequest.of(page, size, Sort.by("publishedAt").descending());
                case "popularity":
                    return PageRequest.of(page, size, Sort.by("totalStudents").descending());
                case "price-asc":
                    return PageRequest.of(page, size, Sort.by("price").ascending());
                case "price-desc":
                    return PageRequest.of(page, size, Sort.by("price").descending());
                default:
                    return PageRequest.of(page, size);
            }
        }
        return PageRequest.of(page, size);
    }
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularCourses(
            @RequestParam(defaultValue = "3") int size) {
        try {
            // Gọi service để lấy các khóa học phổ biến nhất
            List<CourseResponseDTO> popularCourses = courseService.getPopularCourses(size);
            return ResponseEntity.ok(popularCourses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving popular courses: " + e.getMessage());
        }
    }
    @GetMapping("/stats")
    public ResponseEntity<UserStatsDTO> getUserStats(Authentication authentication) {
        String username = authentication.getName();
        UserStatsDTO stats = userStatsService.getUserStats(username);
        return ResponseEntity.ok(stats);
    }
}