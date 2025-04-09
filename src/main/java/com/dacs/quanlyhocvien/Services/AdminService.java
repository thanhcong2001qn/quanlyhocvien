package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.IAdminRepository;
import com.dacs.quanlyhocvien.Repository.IRoleRepository;
import com.dacs.quanlyhocvien.models.AdminModel;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.Repository.IAccountRepository;
import com.dacs.quanlyhocvien.models.RoleModel;
import com.dacs.quanlyhocvien.models.StudentModel;
import com.dacs.quanlyhocvien.models.dto.AdminResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final IAdminRepository adminRepository;
    private final IAccountRepository accountRepository;
    private final IRoleRepository roleRepository;
    private final FileStorageService fileStorageService;

    public AdminModel addAdmin(AdminModel admin, MultipartFile file) {
        if (admin == null || admin.getAccount() == null) {
            throw new IllegalArgumentException("Admin hoặc Account không được null");
        }
        RoleModel adminRole = roleRepository.findByRoleName("admin");
        AccountModel account = admin.getAccount();
        admin.getAccount().setRole(adminRole);
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
        updatedAccount.setGender(admin.getAccount().getGender());// Cập nhật thời gian sửa đổi

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

    public List<AdminModel> searchAdmins(String name, Integer roleId) {
        if ((name == null || name.isBlank()) && roleId == null) {
            return adminRepository.findAll();
        }

        return adminRepository.findByAccountFullNameContainingIgnoreCaseAndAccountRoleRoleId(name, roleId);
    }

    @Transactional
    public void deleteAdmin(Long id) {
        AdminModel admin = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Admin với ID: " + id));

        if (admin.getAccount() != null) {
            Long accountId = admin.getAccount().getAccountId();
            adminRepository.deleteById(id);      // Xóa admin trước
            accountRepository.deleteById(accountId); // Xóa luôn account
        } else {
            adminRepository.deleteById(id);
        }
    }
  public Page<AdminResponseDTO> getAdmins(Pageable pageable) {
        return adminRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    // ✅ Hàm convert 1 AdminModel -> AdminResponseDTO
    private AdminResponseDTO mapToDto(AdminModel admin) {
        AdminResponseDTO dto = new AdminResponseDTO();
        dto.setAdminId(admin.getAdminId());

        AccountModel account = admin.getAccount();
        if (account != null) {
            dto.setFullName(account.getFullName());
            dto.setEmail(account.getEmail());
            if (account.getRole() != null) {
                dto.setRoleName(account.getRole().getRoleName());
            }
            if (account.getCreatedAt() != null) {
                dto.setCreatedAt(account.getCreatedAt().toString());
            }
        }
        return dto;
    }
    public void saveAdmin(AdminModel admin) {
        adminRepository.save(admin);
    }

}