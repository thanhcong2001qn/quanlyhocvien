package com.dacs.quanlyhocvien.models;

import org.springframework.web.multipart.MultipartFile;

public class TeacherRequestDTO {
    private TeacherModel teacher;
    private MultipartFile file;

    public TeacherRequestDTO() {
        this.teacher = new TeacherModel();
        this.teacher.setAccount(new AccountModel());
    }

    public TeacherRequestDTO(TeacherModel teacher, MultipartFile file) {
        this.teacher = teacher;
        this.file = file;
    }

    public TeacherModel getTeacher() {
        return teacher;
    }

    public void setTeacher(TeacherModel teacher) {
        if (teacher == null) {
            this.teacher = new TeacherModel();
            this.teacher.setAccount(new AccountModel());
        } else if (teacher.getAccount() == null) {
            teacher.setAccount(new AccountModel());
        }
        this.teacher = teacher;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
