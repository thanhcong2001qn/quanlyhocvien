package com.dacs.quanlyhocvien.models;


import jakarta.persistence.*;

@Entity
@Table(name = "admin")
public class AdminModel {
    @Id
    @Column(name = "admin_id")
    private Integer adminId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "admin_id")
    private AccountModel account;
    // No-arg constructor required by JPA
    public AdminModel() {
    }
    public AdminModel(AccountModel account) {
        this.account = account;
    }

    // Getters and Setters
    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public AccountModel getAccount() {
        return account;
    }

    public void setAccount(AccountModel account) {
        this.account = account;
    }

}
