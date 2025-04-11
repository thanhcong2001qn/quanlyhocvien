package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.IAdminRepository;
import com.dacs.quanlyhocvien.models.AdminModel;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.Repository.IAccountRepository;
import com.dacs.quanlyhocvien.models.dto.AccountResponseDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<AccountModel> findAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    public Page<AccountResponseDTO> getAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    // ✅ Hàm map từng AccountModel → AccountResponseDTO
    private AccountResponseDTO mapToDto(AccountModel account) {
        AccountResponseDTO dto = new AccountResponseDTO();
        dto.setAccountId(account.getAccountId());
        dto.setUsername(account.getUsername());
        dto.setFullName(account.getFullName());
        dto.setEmail(account.getEmail());
        dto.setRoleName(account.getRole() != null ? account.getRole().getRoleName() : null);
        dto.setIsActive(account.getIsActive());
        dto.setCreatedAt(account.getCreatedAt() != null ? account.getCreatedAt().toString() : null);
        return dto;
    }
}


