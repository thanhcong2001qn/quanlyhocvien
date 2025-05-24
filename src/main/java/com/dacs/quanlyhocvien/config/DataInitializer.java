package com.dacs.quanlyhocvien.config;

import com.dacs.quanlyhocvien.Repository.IRoleRepository;
import com.dacs.quanlyhocvien.models.RoleModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private IRoleRepository roleRepository;

    private void createRoleIfNotFound(String name) {
        String roleName = name.startsWith("ROLE_") ? name : "ROLE_" + name;
        if (roleRepository.findByRoleName(roleName) == null) {
            RoleModel role = new RoleModel();
            role.setRoleName(roleName);
            roleRepository.save(role);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        // Tạo các vai trò mặc định nếu chưa tồn tại
        createRoleIfNotFound("ROLE_ADMIN");
        createRoleIfNotFound("ROLE_TEACHER");
        createRoleIfNotFound("ROLE_STUDENT");
    }
}