package com.dacs.quanlyhocvien.DTO.Response;


import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO để cập nhật thông tin khóa học
 */
@Data
public class CourseUpdateDTO {
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Long categoryId;
    private Integer duration;
    private String level;
    private Boolean isPublished;
    private Boolean isFeatured;
}