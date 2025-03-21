package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.IAdminRepository;
import com.dacs.quanlyhocvien.models.AdminModel;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.Repository.IAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {
    private final IAdminRepository adminRepository;
    private final IAccountRepository accountRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public AdminService(IAdminRepository adminRepository, IAccountRepository accountRepository, FileStorageService fileStorageService) {
        this.adminRepository = adminRepository;
        this.accountRepository = accountRepository;
        this.fileStorageService = fileStorageService;
    }

    public AdminModel addAdmin(AdminModel admin, MultipartFile file) {
        if (admin == null || admin.getAccount() == null) {
            throw new IllegalArgumentException("Admin hoặc Account không được null");
        }

        AccountModel account = admin.getAccount();
        // Kiểm tra xem tài khoản đã tồn tại chưa
        AccountModel existingAccount = accountRepository.findByEmail(account.getEmail());
        if (existingAccount != null) {
            throw new IllegalArgumentException("Email đã tồn tại, không thể thêm admin mới!");
        } else {
            // Nếu có file ảnh, lưu ảnh
            if (file != null && !file.isEmpty()) {
                account.setAvatarPath(fileStorageService.storeFile(file, account.getEmail()));
            }
            account.setPassword("1234"); // Gán password mặc định
            account = accountRepository.save(account); // Lưu tài khoản trước
        }

        admin.setAccount(account); // Gán account đã lưu vào admin
        return adminRepository.save(admin);
    }

    public AdminModel updateAdmin(AdminModel admin) {
        if (admin == null || admin.getAccount() == null) {
            throw new IllegalArgumentException("Admin hoặc tài khoản không được null");
        }

        Optional<AccountModel> existingAccount = accountRepository.findById(admin.getAccount().getAccountId());
        if (existingAccount.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản admin liên kết!");
        }

        AccountModel updatedAccount = existingAccount.get();
        updatedAccount.setFullName(admin.getAccount().getFullName());
        updatedAccount.setDateOfBirth(admin.getAccount().getDateOfBirth());
        updatedAccount.setPhoneNumber(admin.getAccount().getPhoneNumber());
        updatedAccount.setAddress(admin.getAccount().getAddress());
        updatedAccount.setGender(admin.getAccount().getGender());
        updatedAccount.setUpdatedAt(java.time.LocalDate.now()); // Cập nhật thời gian sửa đổi

        accountRepository.save(updatedAccount);

        Optional<AdminModel> existingAdmin = adminRepository.findById(admin.getAdminId());
        if (existingAdmin.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy admin!");
        }

        AdminModel updatedAdmin = existingAdmin.get();
        updatedAdmin.setAccount(updatedAccount);
        // Cập nhật các trường đặc thù của AdminModel nếu cần
        // Ví dụ: updatedAdmin.setAdminRole(admin.getAdminRole());
        //        updatedAdmin.setAccessLevel(admin.getAccessLevel());

        return adminRepository.save(updatedAdmin);
    }

    public List<AdminModel> getAllAdmins() {
        return adminRepository.findAll();
    }

    public AdminModel getAdminById(Long id) {
        return adminRepository.findById(id).orElse(null);
    }

    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }
}