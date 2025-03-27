package com.dacs.quanlyhocvien.DTO.Request;

import com.dacs.quanlyhocvien.models.AdminModel;
import org.springframework.web.multipart.MultipartFile;

public class AdminRequestDTO {
    private AdminModel admin;
    private MultipartFile file;

    // Getters and Setters
    public AdminModel getAdmin() {
        return admin;
    }

    public void setAdmin(AdminModel admin) {
        this.admin = admin;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}