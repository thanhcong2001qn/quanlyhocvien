package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.CourseCategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICategoryRepository extends JpaRepository<CourseCategoryModel, Long> {
}
