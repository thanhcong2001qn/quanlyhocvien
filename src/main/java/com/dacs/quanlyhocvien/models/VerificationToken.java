package com.dacs.quanlyhocvien.models;

import com.dacs.quanlyhocvien.models.AccountModel;
import jakarta.persistence.Entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_token")
public class VerificationToken {
    private static final int EXPIRATION_DAYS = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Integer tokenId;

    @Column(name = "token", nullable = false)
    private String token;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false)
    private AccountModel account;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    // Constructors
    public VerificationToken() {
        // Set expiry date to 24 hours from now
        this.expiryDate = LocalDateTime.now().plusDays(EXPIRATION_DAYS);
    }

    public VerificationToken(String token, AccountModel account) {
        this();
        this.token = token;
        this.account = account;
    }

    // Getters and Setters
    public Integer getTokenId() {
        return tokenId;
    }

    public void setTokenId(Integer tokenId) {
        this.tokenId = tokenId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AccountModel getAccount() {
        return account;
    }

    public void setAccount(AccountModel account) {
        this.account = account;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}