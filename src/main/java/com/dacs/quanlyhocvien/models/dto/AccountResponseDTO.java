package com.dacs.quanlyhocvien.models.dto;

import lombok.Data;

@Data
public class AccountResponseDTO {
    private Long accountId;
    private String username;
    private String fullName;
    private String email;
    private String roleName;
    private Boolean isActive;
    private String createdAt;
}
