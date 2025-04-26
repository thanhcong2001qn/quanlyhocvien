package com.dacs.quanlyhocvien.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstructorResponseDTO {
    private Long accountId;
    private String fullName;
    private String biography;
    private String avatarUrl;
    private String profession;
    private Integer totalCourses;
    private Integer totalStudents;
    private Double averageRating;
}