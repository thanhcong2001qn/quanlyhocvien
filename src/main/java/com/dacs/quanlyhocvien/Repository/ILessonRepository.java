package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.LessonModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ILessonRepository extends JpaRepository<LessonModel, Integer> {
    @Query("SELECT l FROM LessonModel l WHERE l.module.moduleId = :moduleId")
    List<LessonModel> findByModuleId(@Param("moduleId") Long moduleId);

}