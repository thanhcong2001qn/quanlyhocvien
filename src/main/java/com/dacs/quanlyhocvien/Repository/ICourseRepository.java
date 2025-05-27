package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.CourseModel;
import com.dacs.quanlyhocvien.models.ModuleModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICourseRepository extends JpaRepository<CourseModel, Long> {

    Page<CourseModel> findAll(Specification<CourseModel> spec, Pageable pageable);

    @Query("SELECT m FROM ModuleModel m WHERE m.course.courseId = :courseId ORDER BY m.position ASC")
    List<ModuleModel> findModulesByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT c FROM CourseModel c WHERE c.category.categoryId = :categoryId AND c.courseId != :courseId AND c.isPublished = true ORDER BY c.totalStudents DESC, c.rating DESC")
    List<CourseModel> findRelatedCoursesByCategory(
            @Param("categoryId") Long categoryId,
            @Param("courseId") Long courseId,
            Pageable pageable);

    @Query("SELECT c FROM CourseModel c WHERE c.category.categoryId != :categoryId AND c.courseId != :courseId AND c.isPublished = true ORDER BY c.totalStudents DESC, c.rating DESC")
    List<CourseModel> findPopularCoursesExcludingCategoryAndCourse(
            @Param("categoryId") Long categoryId,
            @Param("courseId") Long courseId,
            Pageable pageable);
    @Query("SELECT c FROM CourseModel c WHERE c.isPublished = true ORDER BY c.totalStudents DESC")
    List<CourseModel> findPopularCourses(Pageable pageable);
    @Query("select count(*) from CourseModel c where c.category.categoryId = :categoryId")
    int countByCategoryId(
            @Param("categoryId") Long categoryId);

    long countByIsPublished(@Param("isPublished") boolean isPublished);

    long countByIsFeatured(@Param("isFeatured") boolean isFeatured);



}