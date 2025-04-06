package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.IAdminRepository;
import com.dacs.quanlyhocvien.models.AdminModel;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.Repository.IAccountRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AccountService{
    private final IAccountRepository accountRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public AccountService(IAccountRepository accountRepository, FileStorageService fileStorageService) {
        this.accountRepository = accountRepository;
        this.fileStorageService = fileStorageService;
    }



    public List<AccountModel> getAllAccounts() {
        return accountRepository.findAll();
    }

    public AccountModel getAccountById(Long id) {
        return accountRepository.findById(id).orElse(null);
    }
    public void save(AccountModel account) {
        accountRepository.save(account);
    }
    public AccountModel getAccountByRoleId (Integer roleId) {
        return accountRepository.findByRole_RoleId(roleId);
    }
    public AccountModel getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }
    public AccountModel getAccountByUsername(String username) {
        return accountRepository.findByUsername(username);
    }
    public List<AccountModel> searchAccounts(String keyword, Integer role, Boolean status) {
        // Logic tìm kiếm tài khoản
        return accountRepository.findAccountsByCriteria(keyword, role, status);
    }
}


