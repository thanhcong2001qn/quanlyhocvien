package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.CartItemModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICartItemRepository extends JpaRepository<CartItemModel, Long> {

    /**
     * Tìm tất cả các item trong giỏ hàng của một học viên
     */
    List<CartItemModel> findByStudent_StudentIdOrderByCreatedAtDesc(Long studentId);

    /**
     * Tìm một item trong giỏ hàng dựa trên studentId và courseId
     */
    Optional<CartItemModel> findByStudent_StudentIdAndCourse_CourseId(Long studentId, Long courseId);

    /**
     * Kiểm tra xem khóa học đã có trong giỏ hàng của học viên chưa
     */
    boolean existsByStudent_StudentIdAndCourse_CourseId(Long studentId, Long courseId);

    /**
     * Đếm số lượng item trong giỏ hàng của một học viên
     */
    @Query("SELECT COUNT(c) FROM CartItemModel c WHERE c.student.studentId = :studentId")
    Long countByStudentId(@Param("studentId") Long studentId);

    /**
     * Xóa một item khỏi giỏ hàng dựa trên studentId và courseId
     */
    void deleteByStudent_StudentIdAndCourse_CourseId(Long studentId, Long courseId);

    /**
     * Xóa tất cả các item trong giỏ hàng của một học viên
     */
    void deleteByStudent_StudentId(Long studentId);
}