package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.IRoleRepository;
import com.dacs.quanlyhocvien.models.RoleModel;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    private final IRoleRepository roleRepository;
    public RoleService(IRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    public RoleModel getRoleByRoleName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }
}
