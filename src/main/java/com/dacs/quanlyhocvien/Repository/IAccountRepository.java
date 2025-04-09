package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.AccountModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface IAccountRepository extends JpaRepository<AccountModel,Long> {
    AccountModel findByEmail(String email);
    AccountModel findByRole_RoleId(Integer roleId);
    AccountModel findByUsername(String username);
    @Query("SELECT a FROM AccountModel a WHERE "
            + "(?1 IS NULL OR a.username LIKE %?1% OR a.email LIKE %?1% OR a.fullName LIKE %?1%) "
            + "AND (?2 IS NULL OR a.role.roleId = ?2) "
            + "AND (?3 IS NULL OR a.isActive = ?3)")
    List<AccountModel> findAccountsByCriteria(String keyword, Integer role, Boolean status);
}
