package com.dacs.quanlyhocvien.DTO.Response;

import com.dacs.quanlyhocvien.DTO.Response.CategoryResponseDTO;
import com.dacs.quanlyhocvien.DTO.Response.InstructorResponseDTO;
import com.dacs.quanlyhocvien.models.CourseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Course Detail Page
 * Created by: thanhcong2001qncode
 * Last updated: 2025-04-23 12:33:18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailResponeDTO {

    // Basic course information
    private Long courseId;
    private String title;
    private String description;
    private String thumbnailPath;

    // Pricing information
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Boolean hasFreeContent;

    // Course metadata
    private String level;
    private Integer duration; // Total duration in minutes
    private Integer totalModules;
    private Integer totalLessons;
    private LocalDateTime publishedAt;
    private Boolean isPublished;
    private Boolean isFeatured;

    // Course statistics
    private Double rating;
    private Integer totalStudents;
    private Integer totalReviews;
    private Integer totalCompletions;

    // Relationships
    private CategoryResponseDTO category;
    private InstructorResponseDTO instructor;
    private List<ModuleResponseDTO> modules = new ArrayList<>();

    // Requirements and what you'll learn
    private List<String> requirements = new ArrayList<>();
    private List<String> objectives = new ArrayList<>();

    // Course URL for sharing
    private String courseUrl;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper methods for presentation logic
    public boolean hasDiscount() {
        return discountPrice != null && discountPrice.compareTo(price) < 0;
    }

    public double getDiscountPercentage() {
        if (hasDiscount()) {
            return Math.round((1 - (discountPrice.doubleValue() / price.doubleValue())) * 100);
        }
        return 0.0;
    }

    public String getDurationFormatted() {
        if (duration == null) return "0h 0m";

        int hours = duration / 60;
        int minutes = duration % 60;

        return String.format("%dh %dm", hours, minutes);
    }
    public static CourseDetailResponeDTO fromEntity(CourseModel course) {
        if (course == null) {
            return null;
        }

        CourseDetailResponeDTO dto = new CourseDetailResponeDTO();

        // Basic course information
        dto.setCourseId(course.getCourseId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setThumbnailPath(course.getThumbnailPath());

        // Pricing information
        dto.setPrice(course.getPrice());
        dto.setDiscountPrice(course.getDiscountPrice());

        // Course metadata
        dto.setLevel(course.getLevel());
        dto.setDuration(course.getDuration());
        dto.setIsPublished(course.getIsPublished());
        dto.setIsFeatured(course.getIsFeatured());

        // Category mapping
        if (course.getCategory() != null) {
            CategoryResponseDTO categoryDTO = new CategoryResponseDTO();
            categoryDTO.setCategoryId(course.getCategory().getCategoryId());
            categoryDTO.setCategoryName(course.getCategory().getCategoryName());
            dto.setCategory(categoryDTO);
        }

        // Timestamps
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());

        return dto;
    }
}