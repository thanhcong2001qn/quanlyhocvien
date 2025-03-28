package com.dacs.quanlyhocvien.Controllers.admin;

import com.dacs.quanlyhocvien.Services.AccountService;
import com.dacs.quanlyhocvien.models.AccountModel;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;



    @GetMapping("/accounts")
    public String searchAccounts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) Integer role,
            @RequestParam(value = "status", required = false) Boolean status,
            Model model,
            HttpServletRequest request) {

        List<AccountModel> accounts = accountService.searchAccounts(keyword, role, status);
        model.addAttribute("accounts", accounts);

        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return "fragments/account/account-table :: tbody"; // Trả về chỉ tbody
        }
        return "views/admin/AllAccounts"; // Trả về trang đầy đủ
    }

}
