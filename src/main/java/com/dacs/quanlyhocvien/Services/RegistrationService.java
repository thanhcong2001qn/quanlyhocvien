package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.IAccountRepository;
import com.dacs.quanlyhocvien.Repository.IRoleRepository;
import com.dacs.quanlyhocvien.Repository.IStudentRepository;
import com.dacs.quanlyhocvien.Repository.IVerificationTokenRepository;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.models.RoleModel;
import com.dacs.quanlyhocvien.models.StudentModel;
import com.dacs.quanlyhocvien.models.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Transactional
    public String registerStudent(AccountModel account, StudentModel student) {
        RoleModel roleModel = roleRepository.findById(3).orElse(null);
        // Lưu account với trạng thái chưa xác thực
        account.setIsEmailVerified(false);
        account.setRole(roleModel);
        accountService.save(account);

        // Thiết lập mối quan hệ và lưu student
        student.setAccount(account);
        studentRepository.save(student);

        // Tạo và lưu token xác nhận
        String tokenValue = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(tokenValue, account);
        tokenRepository.save(verificationToken);
        return tokenValue;
    }
    @Transactional
    public void sendEmail(AccountModel account,String tokenValue) {
        emailService.sendVerificationEmail(account, tokenValue);
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

    @Transactional
    public void resendVerificationToken(String email) {
        AccountModel account = accountService.getAccountByEmail(email);


        // Xóa token cũ (nếu có)
        tokenRepository.deleteByAccount(account);

        // Tạo token mới
        String tokenValue = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(tokenValue, account);
        tokenRepository.save(verificationToken);

        // Gửi lại email xác nhận
        emailService.sendVerificationEmail(account, tokenValue);
    }
}