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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class RegistrationService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private IVerificationTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private IStudentRepository studentRepository;

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
        new Thread(() -> {
            sendEmail(registerRequest, tokenValue);
        }).start();
    }
    @Transactional
    public void sendEmail(RegisterRequest registerRequest, String tokenValue) {
        AccountModel accountModel = accountService.getAccountByEmail(registerRequest.getEmail());
        emailService.sendVerificationEmail(accountModel, tokenValue);
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


}