package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.StudentModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IStudentRepository extends JpaRepository <StudentModel, Long>{
    public StudentModel findByEmail(String email);
}
