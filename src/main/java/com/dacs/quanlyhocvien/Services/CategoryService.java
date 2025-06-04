package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.ICategoryRepository;
import com.dacs.quanlyhocvien.Repository.ICourseRepository;
import com.dacs.quanlyhocvien.models.CourseCategoryModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {
    private final String UPLOAD_DIR = "static/images/category-icons";
    @Autowired
    private ICategoryRepository categoryRepository;
    @Autowired
    private ICourseRepository courseRepository;

    public List<CourseCategoryModel> getAllCategories() {
        // Logic to retrieve all categories
        return categoryRepository.findAll();
    }
    public CourseCategoryModel addCategory(CourseCategoryModel category) {
        // Logic to add a new category
        return categoryRepository.save(category);
    }
    public String saveIconFile(MultipartFile iconFile) throws IOException {
        // Kiểm tra file
        if (iconFile.isEmpty()) {
            return null;
        }

        // Lấy tên file gốc
        String originalFilename = iconFile.getOriginalFilename();

        // Tạo tên file mới để tránh trùng lặp
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }

        // Tạo tên file duy nhất với timestamp và UUID
        String newFilename = "category-icon-" + System.currentTimeMillis() +
                "-" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;

        // Tạo thư mục lưu trữ nếu chưa tồn tại
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Lưu file
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(iconFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về đường dẫn tương đối để lưu vào database
        return "/images/category-icons/" + newFilename;
    }
    public boolean deleteCategory(Long categoryId) {
        // Kiểm tra danh mục có tồn tại không
        Optional<CourseCategoryModel> categoryOpt = categoryRepository.findById(categoryId);

        if (!categoryOpt.isPresent()) {
            return false; // Không tìm thấy danh mục
        }

        CourseCategoryModel category = categoryOpt.get();

        // Kiểm tra xem danh mục có đang được sử dụng bởi khóa học nào không
        long courseCount = courseRepository.countByCategoryId(categoryId);

        if (courseCount > 0) {
            throw new IllegalStateException(
                    "Không thể xóa danh mục này vì đang có " + courseCount + " khóa học thuộc danh mục");
        }

        // Xóa file icon nếu có
        String iconPath = category.getIconPath();
        if (iconPath != null && !iconPath.isEmpty()) {
            try {
                // Chuyển đổi đường dẫn tương đối thành đường dẫn tuyệt đối
                String fullPath = UPLOAD_DIR + iconPath.substring(iconPath.lastIndexOf('/'));
                Path fileToDelete = Paths.get(fullPath);

                // Kiểm tra file tồn tại trước khi xóa
                if (Files.exists(fileToDelete)) {
                    Files.delete(fileToDelete);
                }
            } catch (IOException e) {
                // Log lỗi nhưng vẫn tiếp tục xóa danh mục từ database
                System.err.println("Không thể xóa file icon: " + e.getMessage());
            }
        }

        // Xóa danh mục từ database
        categoryRepository.deleteById(categoryId);
        return true;
    }
}
