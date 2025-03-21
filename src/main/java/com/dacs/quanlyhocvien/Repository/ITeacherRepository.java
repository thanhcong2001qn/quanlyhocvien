package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.StudentModel;
import com.dacs.quanlyhocvien.models.TeacherModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ITeacherRepository extends JpaRepository<TeacherModel, Long> {
    TeacherModel findByAccount_Email(String email);
}
