package com.dacs.quanlyhocvien.Controllers.Course;

import com.dacs.quanlyhocvien.Services.CategoryService;
import com.dacs.quanlyhocvien.models.CourseCategoryModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/category")
public class CategoryApiController {

    private final CategoryService categoryService;

    public CategoryApiController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/add-category")
    public ResponseEntity<?> addCategory(
            @RequestParam("categoryName") String categoryName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isActive", defaultValue = "true") boolean isActive,
            @RequestParam(value = "iconFile", required = false) MultipartFile iconFile) {

        try {
            // Validate dữ liệu
            if (categoryName == null || categoryName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên danh mục không được để trống");
            }

            // Chuẩn bị dữ liệu
            CourseCategoryModel category = new CourseCategoryModel();
            category.setCategoryName(categoryName);
            category.setDescription(description);
            category.setIsActive(isActive);

            // Xử lý upload file
            if (iconFile != null && !iconFile.isEmpty()) {
                String iconPath = categoryService.saveIconFile(iconFile);
                category.setIconPath(iconPath);
            }

            // Lưu danh mục
            CourseCategoryModel savedCategory = categoryService.addCategory(category);

            // Trả về kết quả
            return ResponseEntity.ok(savedCategory);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm danh mục: " + e.getMessage());
        }
    }
    @GetMapping("/all-categories")
    public ResponseEntity<?> getAllCategories() {
        try {
            var categories = categoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách danh mục: " + e.getMessage());
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable("id") Long id) {
        try {
            boolean deleted = categoryService.deleteCategory(id);

            if (deleted) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Đã xóa danh mục thành công");
                response.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy danh mục với ID: " + id);
            }
        } catch (IllegalStateException e) {
            // Danh mục đang được sử dụng bởi khóa học
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa danh mục: " + e.getMessage());
        }
    }
}
