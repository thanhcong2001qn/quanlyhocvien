package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.AdminModel;
import com.dacs.quanlyhocvien.models.TeacherModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IAdminRepository extends JpaRepository<AdminModel, Long> {
    AdminModel findByAccount_Email(String email);
    List<AdminModel> findByAccountFullNameContainingIgnoreCaseAndAccountRoleRoleId(String name, Integer roleId);
}
