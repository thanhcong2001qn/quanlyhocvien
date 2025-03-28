package com.dacs.quanlyhocvien.models;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "teacher")
public class TeacherModel {
    @Id
    @Column(name = "teacher_id")
    private Long teacherId;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "teacher_id", nullable = false)
    private AccountModel account;

    @Column(name = "subject_specialization", length = 100)
    private String subjectSpecialization;

    @Column(name = "qualification", length = 100)
    private String qualification;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    // Constructors
    public TeacherModel() {
        this.account = new AccountModel(); // Đảm bảo account không bị null
    }

    public TeacherModel(AccountModel account, String subjectSpecialization, String qualification, LocalDate hireDate) {
        this.account = account;
        this.subjectSpecialization = subjectSpecialization;
        this.qualification = qualification;
        this.hireDate = hireDate;
    }

    // Getters and Setters
    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public AccountModel getAccount() {
        return account;
    }

    public void setAccount(AccountModel account) {
        this.account = account;
    }

    public String getSubjectSpecialization() {
        return subjectSpecialization;
    }

    public void setSubjectSpecialization(String subjectSpecialization) {
        this.subjectSpecialization = subjectSpecialization;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }
}
