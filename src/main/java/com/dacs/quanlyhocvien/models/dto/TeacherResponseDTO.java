// src/main/java/com/dacs/quanlyhocvien/models/dto/TeacherResponseDTO.java

package com.dacs.quanlyhocvien.models.dto;

import lombok.Data;

@Data
public class TeacherResponseDTO {
    private Long teacherId;
    private String fullName;
    private String gender;
    private String subjectSpecialization;
    private String hireDate;
}
