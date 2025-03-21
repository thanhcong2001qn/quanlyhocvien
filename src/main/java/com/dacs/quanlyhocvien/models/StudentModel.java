package com.dacs.quanlyhocvien.models;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "student")
public class StudentModel extends AbstractModel {
    @Id
    @Column(name = "student_id")
    private Long studentId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "student_id")
    private AccountModel account;

    @Column(name = "class", length = 20)
    private String className;


    // Constructors
    public StudentModel() {
    }

    public StudentModel(AccountModel account, String className
    ) {
        this.account = account;

        this.className = className;

    }

    // Getters and Setters
    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
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

}
