package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.IAdminRepository;
import com.dacs.quanlyhocvien.models.AdminModel;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.Repository.IAccountRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.dacs.quanlyhocvien.Repository.IAccountRepository;
import com.dacs.quanlyhocvien.models.AccountModel;
import org.springframework.stereotype.Service;

@Service
public class AccountService{
    @Autowired
    private  IAccountRepository accountRepository;

    public List<AccountModel> getAllAccounts() {
        return accountRepository.findAll();
    }
    public void save(AccountModel account) {
        accountRepository.save(account);
    }
    public AccountModel getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }
    public AccountModel getAccountByUsername(String username) {
        return accountRepository.findByUsername(username);
    }
}


