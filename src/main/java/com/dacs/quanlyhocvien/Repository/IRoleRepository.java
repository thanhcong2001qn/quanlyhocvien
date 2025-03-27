package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.RoleModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRoleRepository extends JpaRepository<RoleModel, Long> {
    RoleModel findByRoleName(String roleName);
}
