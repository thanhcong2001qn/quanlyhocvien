package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.LessonModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ILessonRepository extends JpaRepository<LessonModel, Long> {
    @Query("SELECT l FROM LessonModel l WHERE l.module.moduleId = :moduleId")
    List<LessonModel> findByModuleId(@Param("moduleId") Long moduleId);

    void deleteByModule_ModuleId(Long moduleModuleId);

    int countLessonsByModule_ModuleId(Long moduleModuleId);

    List<LessonModel> findByModule_ModuleIdOrderByPosition(Long moduleModuleId);
    @Query("SELECT COALESCE(MAX(l.position), 0) FROM LessonModel l WHERE l.module.moduleId = :moduleId")
    int findMaxPositionByModuleId(@Param("moduleId") Long moduleId);

    @Modifying
    @Query("UPDATE LessonModel l SET l.position = :position WHERE l.lessonId = :lessonId")
    void updateLessonPosition(@Param("lessonId") Long lessonId, @Param("position") Integer position);

    @Query("SELECT SUM(l.duration) FROM LessonModel l JOIN ModuleModel m ON l.module.moduleId = m.moduleId WHERE m.course.courseId = :courseId")
    Integer calculateCourseDuration(@Param("courseId") Long courseId);

    LessonModel findByLessonId(Long lessonId);
}