package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.models.AccountModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(AccountModel account, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(account.getEmail());
        message.setSubject("Xác nhận Email");

        String confirmationUrl = baseUrl + "/verify-account?token=" + token;

        String emailContent = "Chào " + account.getUsername() + ",\n\n"
                + "Vui lòng nhấp vào liên kết dưới đây để xác nhận địa chỉ email của bạn:\n\n"
                + confirmationUrl + "\n\n"
                + "Liên kết này sẽ hết hạn sau 24 giờ.\n\n"
                + "Trân trọng,\n"
                + "Đội ngũ hỗ trợ";

        message.setText(emailContent);
        mailSender.send(message);
    }
    public void sendPasswordResetEmail(AccountModel account, String token){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(account.getEmail());
        message.setSubject("Đặt lại mật khẩu");

        String confirmationUrl = baseUrl + "/reset-password?token=" + token;

        String emailContent = "Chào " + account.getUsername() + ",\n\n"
                + "Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng nhấp vào liên kết dưới đây để đặt lại mật khẩu của bạn:\n\n"
                + confirmationUrl + "\n\n"
                + "Liên kết này sẽ hết hạn sau 24 giờ.\n\n"
                + "Trân trọng,\n"
                + "Đội ngũ hỗ trợ";

        message.setText(emailContent);
        mailSender.send(message);
    }
}