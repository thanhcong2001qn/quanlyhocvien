package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.models.TeacherModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface IAccountRepository extends JpaRepository<AccountModel, Long>{
    Optional<AccountModel> findByEmail(String email);
//    Page<AccountModel> searchAccounts(String keyword, String role, String status, int page, int pageSize);
//    AccountModel getAccountById(Long id);
//    AccountModel addAccount(AccountModel account);
//    AccountModel updateAccount(AccountModel account);
//    void deleteAccount(Long id);
//    boolean toggleAccountStatus(Long id);
}
