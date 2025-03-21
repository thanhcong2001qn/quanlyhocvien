package com.dacs.quanlyhocvien.Controllers.admin;

import com.dacs.quanlyhocvien.Services.AdminService;
import com.dacs.quanlyhocvien.models.AdminModel;
import com.dacs.quanlyhocvien.models.AdminRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping(value = "/apiAddAdmin", consumes = "multipart/form-data")
    public ResponseEntity<?> createAdmin(@ModelAttribute AdminRequestDTO requestDTO) {
        if (requestDTO.getAdmin() == null || requestDTO.getAdmin().getAccount() == null) {
            return new ResponseEntity<>("Lỗi: Thiếu thông tin admin hoặc tài khoản", HttpStatus.BAD_REQUEST);
        }

        // Nếu username null, đặt bằng email
        if (requestDTO.getAdmin().getAccount().getUsername() == null) {
            requestDTO.getAdmin().getAccount().setUsername(requestDTO.getAdmin().getAccount().getEmail());
        }

        // Nếu email trống, trả về lỗi
        if (requestDTO.getAdmin().getAccount().getEmail() == null ||
                requestDTO.getAdmin().getAccount().getEmail().isEmpty()) {
            return new ResponseEntity<>("Email không được để trống", HttpStatus.BAD_REQUEST);
        }

        AdminModel savedAdmin = adminService.addAdmin(requestDTO.getAdmin(), requestDTO.getFile());
        return new ResponseEntity<>(savedAdmin, HttpStatus.CREATED);
    }

    @PutMapping(value = "/apiEditAdmin")
    public ResponseEntity<AdminModel> updateAdmin(@RequestBody AdminModel admin) {
        AdminModel savedAdmin = adminService.updateAdmin(admin);
        return new ResponseEntity<>(savedAdmin, HttpStatus.OK);
    }

    @DeleteMapping(value = "/deleteAdmin/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}