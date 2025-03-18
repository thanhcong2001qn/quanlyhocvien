package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.StudentModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IStudentRepository extends JpaRepository <StudentModel, Long>{
    StudentModel findByAccount_Email(String email);
    StudentModel findByAccount_Username(String userName);
}
