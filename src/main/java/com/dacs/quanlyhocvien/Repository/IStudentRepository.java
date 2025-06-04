package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.models.StudentModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IStudentRepository extends JpaRepository <StudentModel, Long>{
    StudentModel findByAccount_Email(String email);
    StudentModel findByAccount_Username(String userName);
    List<StudentModel> findByAccountFullNameContainingIgnoreCaseAndClassNameContainingIgnoreCase(String fullName, String className);
    List<StudentModel> findByAccountFullNameContainingIgnoreCase(String name);
    List<StudentModel> findByClassNameContainingIgnoreCase(String className);
}
