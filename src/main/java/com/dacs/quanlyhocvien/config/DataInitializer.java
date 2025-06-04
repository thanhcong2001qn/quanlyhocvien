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

    @Override
    public void run(String... args) throws Exception {
        // Tạo các vai trò mặc định nếu chưa tồn tại
        createRoleIfNotFound("ADMIN");
        createRoleIfNotFound("TEACHER");
        createRoleIfNotFound("STUDENT");
    }

    private void createRoleIfNotFound(String name) {
        if (roleRepository.findByRoleName(name) == null) {
            RoleModel role = new RoleModel();
            role.setRoleName(name);
            roleRepository.save(role);
        }
    }
}