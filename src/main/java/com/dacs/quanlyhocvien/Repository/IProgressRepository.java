package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.ProgressModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProgressRepository extends JpaRepository<ProgressModel, Long> {
    @Query("SELECT COUNT(p) FROM ProgressModel p WHERE p.enrollment.student.studentId = :studentId AND p.isCompleted = true")
    int countCompletedLessonsByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COALESCE(SUM(l.duration), 0) FROM ProgressModel p JOIN p.lesson l " +
            "WHERE p.enrollment.student.studentId = :studentId AND p.isCompleted = true")
    int calculateTotalLearningMinutesByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(p) FROM ProgressModel p " +
            "WHERE p.enrollment.student.studentId = :studentId " +
            "AND p.enrollment.course.courseId = :courseId " +
            "AND p.isCompleted = true")
    int countCompletedLessonsByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}