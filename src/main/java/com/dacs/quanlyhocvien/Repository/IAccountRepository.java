package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.AccountModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IAccountRepository extends JpaRepository<AccountModel,Long> {
    AccountModel findByEmail(String email);
    AccountModel findByRole_RoleId(Integer roleId);
    AccountModel findByUsername(String username);
}
