package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.IVerificationTokenRepository;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.models.RoleModel;
import com.dacs.quanlyhocvien.models.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final AccountService accountService;
    private final RoleService roleService;
    private final IVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    @Autowired
    public AuthService(AccountService accountService, RoleService roleService, IVerificationTokenRepository tokenRepository , EmailService emailService) {
        this.emailService = emailService;
        this.accountService = accountService;
        this.roleService = roleService;
        this.tokenRepository = tokenRepository;
    }
    public boolean checkLogin(String username, String password) {
        AccountModel account = accountService.getAccountByUsername(username);
        if (account == null) {
            return false;
        }
        return account.getPassword().equals(password);
    }
    public boolean checkRole(String username, String role) {
        AccountModel account = accountService.getAccountByUsername(username);
        if (account == null) {
            return false;
        }
        RoleModel roleModel = roleService.getRoleByRoleName(role);
        if (roleModel == null) {
            return false;
        }
        return account.getRole().getRoleId().equals(roleModel.getRoleId());
    }
    public boolean checkIsVerifiedEmail (String username){
        AccountModel account = accountService.getAccountByUsername(username);
        if (account == null) {
            return false;
        }
        return account.getIsEmailVerified();
    }
    public boolean accountExists(String emailOrUsername) {
        // Classify whether input is email or username
        boolean isEmail = emailOrUsername.contains("@");
        String identifierType = isEmail ? "email" : "username";
        AccountModel account = isEmail ? accountService.getAccountByEmail(emailOrUsername) : accountService.getAccountByUsername(emailOrUsername);
        if (account == null) {
            return false;
        }
        return true;
    }
    @Transactional
    public void sendPasswordResetEmail(String emailOrUsername) {
        // Classify whether input is email or username
        boolean isEmail = emailOrUsername.contains("@");
        String identifierType = isEmail ? "email" : "username";
        AccountModel account = isEmail ? accountService.getAccountByEmail(emailOrUsername) : accountService.getAccountByUsername(emailOrUsername);
        if (account == null) {
            return;
        }
        // Generate token
        String tokenValue = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(tokenValue, account);
        tokenRepository.save(verificationToken);
        // Send email asynchronously so it doesn't block the response
        emailService.sendPasswordResetEmail(account, tokenValue);
    }
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<VerificationToken> verificationToken = tokenRepository.findByToken(token);
        if (verificationToken.isPresent() && !verificationToken.get().isExpired()) {
            AccountModel account = verificationToken.get().getAccount();
            account.setPassword(newPassword);
            accountService.save(account);
            // Xóa token sau khi xác minh thành công
            tokenRepository.delete(verificationToken.get());
            return true;
        }
        return false;
    }
    @Transactional
    public boolean verifyToken(String token) {
        Optional<VerificationToken> verificationToken = tokenRepository.findByToken(token);
        if (verificationToken.isPresent() && !verificationToken.get().isExpired()) {
            return true;
        }
        return false;
    }
}
