package com.dacs.quanlyhocvien.DTO.Response;

import com.dacs.quanlyhocvien.models.CourseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data  // Lombok annotation tự động tạo getters, setters, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDTO {
    private Long courseId;
    private String title;
    private String description;
    private String thumbnailPath;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String level;
    private Boolean isFeatured;
    private LocalDateTime publishedAt;
    private Float rating;
    private Integer totalStudents;
    private Integer totalReviews;
    private CategoryResponeDTO category;

    // Getters, Setters, Constructors...

    // Phương thức chuyển đổi từ Entity sang DTO
    public static CourseResponseDTO fromEntity(CourseModel entity) {
        if (entity == null) return null;

        CourseResponseDTO dto = new CourseResponseDTO();
        dto.setCourseId(entity.getCourseId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setThumbnailPath(entity.getThumbnailPath());
        dto.setPrice(entity.getPrice());
        dto.setDiscountPrice(entity.getDiscountPrice());
        dto.setLevel(entity.getLevel());
        dto.setIsFeatured(entity.getIsFeatured());
        dto.setPublishedAt(entity.getPublishedAt());
        dto.setRating(entity.getRating());
        dto.setTotalStudents(entity.getTotalStudents());
        dto.setTotalReviews(entity.getTotalReviews());

        // Chỉ lấy thông tin cơ bản của danh mục để tránh vấn đề lazy loading
        if (entity.getCategory() != null) {
            CategoryResponeDTO categoryDTO = new CategoryResponeDTO();
            categoryDTO.setCategoryId(entity.getCategory().getCategoryId());
            categoryDTO.setCategoryName(entity.getCategory().getCategoryName());
            dto.setCategory(categoryDTO);
        }

        return dto;
    }
}
