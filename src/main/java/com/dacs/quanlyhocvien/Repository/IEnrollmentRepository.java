package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.EnrollmentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IEnrollmentRepository extends JpaRepository<EnrollmentModel, Long> {
    /**
     * Tìm đăng ký khóa học theo courseId và studentId
     */
    Optional<EnrollmentModel> findByCourse_CourseIdAndStudent_StudentId(Long courseId, Long studentId);

    /**
     * Kiểm tra xem học viên có đăng ký khóa học không
     */
    boolean existsByCourse_CourseIdAndStudent_StudentId(Long courseId, Long studentId);

    /**
     * Lấy danh sách đăng ký khóa học của một học viên
     */
    List<EnrollmentModel> findByStudent_StudentIdOrderByEnrollmentDateDesc(Long studentId);

    /**
     * Lấy danh sách học viên đã đăng ký một khóa học
     */
    List<EnrollmentModel> findByCourse_CourseIdOrderByEnrollmentDateDesc(Long courseId);

    /**
     * Đếm số lượng học viên đã đăng ký một khóa học
     */
    @Query("SELECT COUNT(e) FROM EnrollmentModel e WHERE e.course.courseId = :courseId")
    Integer countByCourseId(@Param("courseId") Long courseId);

    /**
     * Lấy danh sách đăng ký khóa học theo trạng thái thanh toán
     */
    List<EnrollmentModel> findByPaymentStatus(String paymentStatus);

    /**
     * Thống kê doanh thu theo khóa học
     */
    @Query("SELECT e.course.title AS courseName, COUNT(e) AS enrollmentCount, SUM(e.paymentAmount) AS revenue " +
            "FROM EnrollmentModel e WHERE e.paymentStatus = 'completed' " +
            "GROUP BY e.course.courseId, e.course.title ORDER BY revenue DESC")
    List<Object[]> getRevenueStatsByCourseName();
    @Query("SELECT e.course.courseId" +" FROM EnrollmentModel e WHERE e.student.studentId = :accountId")
    List<Long> findCourseIdsByAccountId(@Param("accountId") Long accountId);
    @Query("SELECT COUNT(e) FROM EnrollmentModel e WHERE e.student.studentId = :studentId")
    int countByStudentId(@Param("studentId") Long studentId);
}
