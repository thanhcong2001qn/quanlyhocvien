package com.dacs.quanlyhocvien.models.dto;

import lombok.Data;

@Data
public class AdminResponseDTO {
    private Long adminId;
    private String fullName;
    private String email;
    private String roleName;
    private String createdAt;
}
