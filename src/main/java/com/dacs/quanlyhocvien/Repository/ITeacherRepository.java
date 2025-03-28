package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.StudentModel;
import com.dacs.quanlyhocvien.models.TeacherModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ITeacherRepository extends JpaRepository<TeacherModel, Long> {
    TeacherModel findByAccount_Email(String email);
    @Query("SELECT t FROM TeacherModel t " +
            "WHERE (:name IS NULL OR LOWER(t.account.fullName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:subject IS NULL OR LOWER(t.subjectSpecialization) LIKE LOWER(CONCAT('%', :subject, '%')))")
    List<TeacherModel> searchTeachers(String name, String subject);
}
