package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.ModuleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IModuleRepository extends JpaRepository<ModuleModel,Long> {

    int countByCourse_CourseId(Long courseCourseId);

    List<ModuleModel> findByCourse_CourseIdOrderByPosition(Long courseCourseId);

    int countLessonsByModuleId(Long moduleId);

    @Query("SELECT COALESCE(MAX(m.position), 0) FROM ModuleModel m WHERE m.course.courseId = :courseId")
    int findMaxPositionByCourse_CourseId(@Param("courseId") Long courseId);

    List<ModuleModel> findByCourse_CourseId(Long courseCourseId);
    @Modifying
    @Query("UPDATE ModuleModel m SET m.position = :position WHERE m.moduleId = :moduleId")
    void updateModulePosition(@Param("moduleId") Long moduleId, @Param("position") Integer position);

    ModuleModel findByModuleId(Long moduleId);
}
