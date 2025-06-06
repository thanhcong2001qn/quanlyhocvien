package com.dacs.quanlyhocvien.Controllers.admin;

import com.dacs.quanlyhocvien.Services.AccountService;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.models.TeacherModel;
import com.dacs.quanlyhocvien.models.dto.AccountResponseDTO;
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
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;


    @GetMapping("/search")
    public String searchAccounts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) Integer role,
            @RequestParam(value = "status", required = false) Boolean status,
            Model model,
            HttpServletRequest request) {

        List<AccountModel> accounts = accountService.searchAccounts(keyword, role, status);
        model.addAttribute("accounts", accounts);

        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return "fragments/account/accounts-table :: tbody"; // Trả về chỉ tbody
        }
        return "views/admin/AllAccounts"; // Trả về trang đầy đủ
    }


    @GetMapping("/api/accounts")
    public ResponseEntity<Page<AccountResponseDTO>> getAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AccountResponseDTO> accountDTOs = accountService.getAccounts(pageable); // 👉 gọi service
        return ResponseEntity.ok(accountDTOs);
    }

    @PutMapping(value = "/api/update")
    public ResponseEntity<AccountModel> updateTeacher(@RequestBody AccountModel account) {
        AccountModel savedAccount = accountService.updateAccount(account);
        return new ResponseEntity<>(savedAccount, HttpStatus.OK);
    }


    @PutMapping("/deactivate/{id}")
    public ResponseEntity<String> deactivateAccount(@PathVariable Long id) {
        AccountModel account = accountService.getAccountById(id);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }

        account.setIsActive(false);
        accountService.updateAccount(account);
        return ResponseEntity.ok("Tài khoản đã được vô hiệu hóa");
    }

    @PutMapping("/updateStatus/{id}")
    public ResponseEntity<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        Boolean isActive = payload.get("isActive");
        accountService.updateAccountStatus(id, isActive);
        return ResponseEntity.ok("Trạng thái tài khoản đã được cập nhật");
    }
}
