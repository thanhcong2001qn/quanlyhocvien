package com.dacs.quanlyhocvien.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponseDTO {
    private Long studentId;
    private String fullName;
    private String gender;
    private String className;
    private LocalDate dateOfBirth;
    private String address;
    private String phoneNumber;
}
