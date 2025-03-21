package com.dacs.quanlyhocvien.models;

import jakarta.persistence.*;

@Entity
@Table(name = "student")
public class StudentModel {
    @Id
    @Column(name = "student_id")
    private Integer studentId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "student_id")
    private AccountModel account;

    @Column(name = "class", length = 20)
    private String className;

    @Column(name = "avatar_path")
    private String avatarPath;
    // Constructors
    public StudentModel() {
    }

    public StudentModel(AccountModel account, String className
    ) {
        this.account = account;

        this.className = className;

    }

    // Getters and Setters
    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public AccountModel getAccount() {
        return account;
    }

    public void setAccount(AccountModel account) {
        this.account = account;
    }


    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
}
