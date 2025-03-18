package com.dacs.quanlyhocvien.models;

import jakarta.persistence.Entity;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "teacher")
public class TeacherModel {
    @Id
    @Column(name = "teacher_id")
    private Integer teacherId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "teacher_id")
    private AccountModel account;

    @Column(name = "subject_specialization", length = 100)
    private String subjectSpecialization;

    @Column(name = "qualification", length = 100)
    private String qualification;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    // Constructors
    public TeacherModel() {
    }

    public TeacherModel(AccountModel account, String subjectSpecialization, String qualification, LocalDate hireDate) {
        this.account = account;
        this.subjectSpecialization = subjectSpecialization;
        this.qualification = qualification;
        this.hireDate = hireDate;
    }

    // Getters and Setters
    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Integer teacherId) {
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
