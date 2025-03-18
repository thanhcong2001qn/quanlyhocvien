package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.IAccountRepository;
import com.dacs.quanlyhocvien.models.AccountModel;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final IAccountRepository accountRepository;
    public AccountService(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
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
}
