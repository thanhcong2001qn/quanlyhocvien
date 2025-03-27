package com.dacs.quanlyhocvien.models;


import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin")
public class AdminModel {
    @Id
    private Long adminId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "admin_id")
    private AccountModel account;

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private AdminRole adminRole = AdminRole.SUPER_ADMIN;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private AccessLevel accessLevel = AccessLevel.FULL_ACCESS;

    // No-arg constructor required by JPA

    public AdminModel() {

    }
    public AdminModel(AccountModel account) {
        this.account = account;
    }

    // Getters and Setters
    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public AccountModel getAccount() {
        return account;
    }

    public void setAccount(AccountModel account) {
        this.account = account;
    }


}
