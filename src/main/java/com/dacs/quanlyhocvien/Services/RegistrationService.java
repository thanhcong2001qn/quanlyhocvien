package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.DTO.Request.RegisterRequest;
import com.dacs.quanlyhocvien.Repository.IAccountRepository;
import com.dacs.quanlyhocvien.Repository.IRoleRepository;
import com.dacs.quanlyhocvien.Repository.IStudentRepository;
import com.dacs.quanlyhocvien.Repository.IVerificationTokenRepository;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.models.RoleModel;
import com.dacs.quanlyhocvien.models.StudentModel;
import com.dacs.quanlyhocvien.models.VerificationToken;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RegistrationService {
    private final ExecutorService emailExecutor = Executors.newSingleThreadExecutor();
    @Autowired
    private AccountService accountService;

    @Autowired
    private IVerificationTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private IRoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private StudentService studentService;

    @Transactional
    public void registerStudent(RegisterRequest registerRequest) {
        if (accountService.getAccountByUsername(registerRequest.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (accountService.getAccountByEmail(registerRequest.getEmail()) != null) {
            throw new IllegalArgumentException("Email already exists");
        }
        // Lưu account với trạng thái chưa xác thực
        AccountModel account = new AccountModel();
        account.setUsername(registerRequest.getUsername());
        account.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        account.setEmail(registerRequest.getEmail());
        account.setIsEmailVerified(false);
        account.setRole(roleRepository.findByRoleName("STUDENT"));
        accountService.save(account);

        // Thiết lập mối quan hệ và lưu student
        StudentModel student = new StudentModel();
        student.setAccount(account);
        studentService.addStudent(student);
        // Tạo và lưu token xác nhận
        String tokenValue = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(tokenValue, account);
        tokenRepository.save(verificationToken);
        AccountModel accountModel = accountService.getAccountByEmail(registerRequest.getEmail());
        emailExecutor.submit(() -> {
            emailService.sendVerificationEmail(accountModel, tokenValue);
        });
    }
    @Transactional
    public boolean verifyAccount(String token) {
        Optional<VerificationToken> verificationToken = tokenRepository.findByToken(token);
        if (verificationToken.isPresent() && !verificationToken.get().isExpired()) {
            AccountModel account = verificationToken.get().getAccount();
            account.setIsEmailVerified(true);
            accountService.save(account);
            // Xóa token sau khi xác minh thành công
            tokenRepository.delete(verificationToken.get());
            return true;
        }
        return false;
    }
    @PreDestroy
    public void shutdown() {
        emailExecutor.shutdown();
    }

}