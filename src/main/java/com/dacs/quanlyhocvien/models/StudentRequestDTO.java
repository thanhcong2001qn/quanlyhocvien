package com.dacs.quanlyhocvien.models;

import org.springframework.web.multipart.MultipartFile;

public class StudentRequestDTO {
    private StudentModel student;
    private MultipartFile file;

    public StudentModel getStudent() {
        return student;
    }

    public void setStudent(StudentModel student) {
        this.student = student;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
