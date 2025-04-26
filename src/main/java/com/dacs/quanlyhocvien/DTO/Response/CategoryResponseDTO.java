package com.dacs.quanlyhocvien.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {
    private Long categoryId;
    private String categoryName;
    private String description;
    private String iconPath;
    private Integer totalCourses;
}