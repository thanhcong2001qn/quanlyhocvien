package com.dacs.quanlyhocvien.Controllers.admin;

import com.dacs.quanlyhocvien.Repository.IAdminRepository;
import com.dacs.quanlyhocvien.Services.AdminService;
import com.dacs.quanlyhocvien.models.AdminModel;
import com.dacs.quanlyhocvien.DTO.Request.AdminRequestDTO;
import com.dacs.quanlyhocvien.models.dto.AdminResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final IAdminRepository adminRepository;

    @PostMapping(value = "/apiAddAdmin", consumes = "multipart/form-data")
    public ResponseEntity<?> createAdmin(@ModelAttribute AdminRequestDTO requestDTO) {
        try {
            if (requestDTO.getAdmin() == null || requestDTO.getAdmin().getAccount() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Thiếu thông tin admin hoặc tài khoản"));
            }

            if (requestDTO.getAdmin().getAccount().getUsername() == null) {
                requestDTO.getAdmin().getAccount().setUsername(requestDTO.getAdmin().getAccount().getEmail());
            }

            if (requestDTO.getAdmin().getAccount().getEmail() == null ||
                    requestDTO.getAdmin().getAccount().getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email không được để trống"));
            }

            AdminModel savedAdmin = adminService.addAdmin(requestDTO.getAdmin(), requestDTO.getFile());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAdmin);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi không xác định: " + ex.getMessage()));
        }
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

    @GetMapping("/search")
    public String searchAdmins(@RequestParam(value = "name", required = false) String name,
                                 @RequestParam(value = "role", required = false) Integer role,
                                 Model model,
                                 HttpServletRequest request) {

        List<AdminModel> admins = adminService.searchAdmins(name, role);
        model.addAttribute("admins", admins);

        // Kiểm tra nếu là AJAX request → trả về fragment
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return "fragments/admin/admins-table :: tbody"; // chỉ return <tbody>
        }

        return "views/admin/AllAdmin"; // return full page nếu không phải AJAX
    }

    @GetMapping("/api/admins")
    public ResponseEntity<Page<AdminResponseDTO>> getAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminResponseDTO> adminDTOs = adminService.getAdmins(pageable); // 👉 gọi service
        return ResponseEntity.ok(adminDTOs);
    }

}