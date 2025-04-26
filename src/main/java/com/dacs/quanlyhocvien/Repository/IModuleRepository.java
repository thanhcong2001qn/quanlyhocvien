package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.ModuleModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IModuleRepository extends JpaRepository<ModuleModel,Long> {
}
