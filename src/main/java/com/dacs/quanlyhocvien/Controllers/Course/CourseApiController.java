package com.dacs.quanlyhocvien.Controllers.Course;

import com.dacs.quanlyhocvien.DTO.Response.ApiResponse;
import com.dacs.quanlyhocvien.DTO.Response.CourseDetailResponeDTO;
import com.dacs.quanlyhocvien.DTO.Response.CourseResponseDTO;
import com.dacs.quanlyhocvien.DTO.Response.CourseUpdateDTO;
import com.dacs.quanlyhocvien.Services.AccountService;
import com.dacs.quanlyhocvien.Services.CloudinaryService;
import com.dacs.quanlyhocvien.Services.CourseService;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.models.CourseCategoryModel;
import com.dacs.quanlyhocvien.models.CourseModel;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
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
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/courses")
public class CourseApiController {

    private final CourseService courseService;
    private final AccountService accountService;
    private final CloudinaryService cloudinaryService;

    public CourseApiController(CourseService courseService, AccountService accountService, CloudinaryService cloudinaryService) {
        this.courseService = courseService;
        this.accountService = accountService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("all-courses")
    public ResponseEntity<?> getCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoryIds,
            @RequestParam(required = false) String levels,
            @RequestParam(required = false) String priceTypes,
            @RequestParam(required = false) String isPublished,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            // Lấy thông tin người dùng hiện tại
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            AccountModel account = accountService.getAccountByUsername(username);

            if (account == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Người dùng không hợp lệ");
            }

            // Tạo Pageable cho phân trang và sắp xếp
            Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            // Gọi service để lấy danh sách khóa học
            Page<CourseResponseDTO> coursesPage = courseService.getCourses(
                    pageable,
                    search,
                    categoryIds,
                    levels,
                    priceTypes,
                    isPublished,
                    account.getAccountId()
            );

            // Tính toán các thống kê
            long totalCourses = courseService.countAllCourses();
            long publishedCourses = courseService.countByStatus(true);
            long draftCourses = courseService.countByStatus(false);
            long featuredCourses = courseService.countByFeatured(true);

            // Tạo response data
            Map<String, Object> response = new HashMap<>();
            response.put("courses", coursesPage.getContent());
            response.put("currentPage", coursesPage.getNumber());
            response.put("totalItems", coursesPage.getTotalElements());
            response.put("totalPages", coursesPage.getTotalPages());
            response.put("totalCourses", totalCourses);
            response.put("publishedCount", publishedCourses);
            response.put("draftCount", draftCourses);
            response.put("featuredCount", featuredCourses);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách khóa học: " + e.getMessage());
        }
    }
    @GetMapping("/{courseId}")
    public ResponseEntity<?> getCourseById(@PathVariable Long courseId) {
        try {
            CourseModel course = courseService.getCourseById(courseId);
            if (course == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Không tìm thấy khóa học với ID: " + courseId));
            }

            // Kiểm tra quyền sở hữu khóa học
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            AccountModel account = accountService.getAccountByUsername(username);

            CourseDetailResponeDTO courseDetail = CourseDetailResponeDTO.fromEntity(course);
            return ResponseEntity.ok(courseDetail);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi khi lấy thông tin khóa học: " + e.getMessage()));
        }
    }
    @PostMapping("/add-course")
    public ResponseEntity<?> addCourse(
            @RequestParam(value = "title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", defaultValue = "0") BigDecimal price,
            @RequestParam(value = "discountPrice", required = false) BigDecimal discountPrice,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "duration", defaultValue = "0") Integer duration,
            @RequestParam(value = "level", required = false) String level,
            @RequestParam(value = "isPublished", defaultValue = "false") Boolean isPublished,
            @RequestParam(value = "isFeatured", defaultValue = "false") Boolean isFeatured,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {

        try {
            // Lấy thông tin người dùng hiện tại
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            AccountModel account = accountService.getAccountByUsername(username);

            String safeDescription = cleanParagraphTags(description);;

            // Tạo đối tượng CourseModel từ các tham số form
            CourseModel course = new CourseModel();
            course.setTitle(title);
            course.setDescription(safeDescription);
            course.setPrice(price);
            course.setDiscountPrice(discountPrice);
            if (categoryId != null) {
                CourseCategoryModel category = new CourseCategoryModel();
                category.setCategoryId(categoryId);
                course.setCategory(category);
            }
            course.setDuration(duration);
            course.setLevel(level);
            course.setIsPublished(isPublished);
            course.setIsFeatured(isFeatured);


            // Xử lý file thumbnail nếu có
            if (thumbnail != null && !thumbnail.isEmpty()) {
                // Upload ảnh lên Cloudinary
                String imageUrl = cloudinaryService.uploadImage(thumbnail, "course-thumbnails");

                // Lưu URL vào đối tượng course
                if (imageUrl != null) {
                    course.setThumbnailPath(imageUrl);
                }
            }

            // Lưu khóa học mới
            CourseModel savedCourse = courseService.saveCourse(course);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedCourse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm khóa học: " + e.getMessage());
        }
    }
    @DeleteMapping("/delete-course/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        try {
            // Lấy thông tin người dùng hiện tại
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            AccountModel account = accountService.getAccountByUsername(username);

            if (account == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Người dùng không hợp lệ");
            }

            // Lấy thông tin khóa học trước khi xóa (để ghi log)
            CourseModel course = courseService.getCourseById(id);
            if (course == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy khóa học");
            }

            // Gọi service để xóa khóa học
            courseService.deleteCourse(id);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Đã xóa khóa học thành công",
                    "courseId", id,
                    "courseTitle", course.getTitle()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa khóa học: " + e.getMessage());
        }
    }
    @PutMapping("/update-course/{id}")
    public ResponseEntity<?> updateCourse(
            @PathVariable Long id,
            @RequestParam(value = "title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", defaultValue = "0") BigDecimal price,
            @RequestParam(value = "discountPrice", required = false) BigDecimal discountPrice,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "duration", defaultValue = "0") Integer duration,
            @RequestParam(value = "level", required = false) String level,
            @RequestParam(value = "isPublished", defaultValue = "false") Boolean isPublished,
            @RequestParam(value = "isFeatured", defaultValue = "false") Boolean isFeatured,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {

        try {
            // Lấy thông tin người dùng hiện tại
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            AccountModel account = accountService.getAccountByUsername(username);

            if (account == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Người dùng không hợp lệ"));
            }

            // Kiểm tra quyền sở hữu khóa học
            CourseModel existingCourse = courseService.getCourseById(id);
            if (existingCourse == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Không tìm thấy khóa học"));
            }


            // Validate input
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "Tên khóa học không được để trống"));
            }

            if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "Giá khóa học không được âm"));
            }

            if (discountPrice != null && price != null && discountPrice.compareTo(price) > 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "Giá khuyến mãi không được lớn hơn giá gốc"));
            }

            // Validate thumbnail nếu có
            if (thumbnail != null && !thumbnail.isEmpty()) {
                // Kiểm tra định dạng file
                String contentType = thumbnail.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse(false, "File không phải là ảnh"));
                }

                // Kiểm tra kích thước file (max 5MB)
                if (thumbnail.getSize() > 5 * 1024 * 1024) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse(false, "Kích thước ảnh không được vượt quá 5MB"));
                }
            }
            String safeDescription = cleanParagraphTags(description);;
            // Tạo đối tượng CourseUpdateDTO
            CourseUpdateDTO courseUpdateDTO = new CourseUpdateDTO();
            courseUpdateDTO.setTitle(title);
            courseUpdateDTO.setDescription(safeDescription);
            courseUpdateDTO.setPrice(price);
            courseUpdateDTO.setDiscountPrice(discountPrice);
            courseUpdateDTO.setCategoryId(categoryId);
            courseUpdateDTO.setDuration(duration);
            courseUpdateDTO.setLevel(level);
            courseUpdateDTO.setIsPublished(isPublished);
            courseUpdateDTO.setIsFeatured(isFeatured);

            // Gọi service để cập nhật khóa học
            CourseModel updatedCourse = courseService.updateCourse(id, courseUpdateDTO, thumbnail);

            // Sử dụng thời gian và username hiện tại

            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật khóa học thành công", updatedCourse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi khi cập nhật khóa học: " + e.getMessage()));
        }
    }
    private String cleanParagraphTags(String description) {
        if (description == null || description.trim().isEmpty()) {
            return description;
        }



        // Loại bỏ thẻ <p> và </p>, giữ lại nội dung bên trong
        String result = description.replaceAll("<p>", "").replaceAll("</p>", "<br>");

        // Loại bỏ thẻ <br> ở cuối cùng nếu có
        result = result.replaceAll("<br>$", "");

        return result;
    }
}

