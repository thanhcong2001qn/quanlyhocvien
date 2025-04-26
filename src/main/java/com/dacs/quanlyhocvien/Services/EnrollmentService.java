package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.ICourseRepository;
import com.dacs.quanlyhocvien.Repository.IEnrollmentRepository;
import com.dacs.quanlyhocvien.Repository.IStudentRepository;
import com.dacs.quanlyhocvien.models.CourseModel;
import com.dacs.quanlyhocvien.models.EnrollmentModel;
import com.dacs.quanlyhocvien.models.StudentModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentService {

    private final IEnrollmentRepository enrollmentRepository;
    private final ICourseRepository courseRepository;
    private final IStudentRepository studentRepository;
    private final StudentService studentService;

    @Autowired
    public EnrollmentService(IEnrollmentRepository enrollmentRepository,
                             ICourseRepository courseRepository,
                             IStudentRepository studentRepository, StudentService studentService) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.studentService = studentService;
    }

    /**
     * Lấy ID của học viên hiện tại đăng nhập
     * @return ID của học viên
     */


    /**
     * Kiểm tra học viên hiện tại đã đăng ký khóa học chưa
     * @param courseId ID của khóa học cần kiểm tra
     * @return true nếu đã đăng ký, false nếu chưa
     */
    public boolean isUserEnrolled(Long courseId,String username) {
        Long studentId = studentService.getStudentByUserName(username).getStudentId();
        return enrollmentRepository.existsByCourse_CourseIdAndStudent_StudentId(courseId, studentId);
    }

    /**
     * Đăng ký khóa học cho học viên hiện tại
     * @param courseId ID của khóa học muốn đăng ký
     * @return true nếu đăng ký thành công
     */
    @Transactional
    public boolean enrollCourse(Long courseId,String username) {
        Long studentId = studentService.getStudentByUserName(username).getStudentId();

        // Kiểm tra xem đã đăng ký chưa
        if (enrollmentRepository.existsByCourse_CourseIdAndStudent_StudentId(courseId, studentId)) {
            return true; // Đã đăng ký rồi
        }

        // Lấy thông tin khóa học và học viên
        Optional<CourseModel> courseOpt = courseRepository.findById(courseId);
        Optional<StudentModel> studentOpt = studentRepository.findById(studentId);

        if (courseOpt.isPresent() && studentOpt.isPresent()) {
            CourseModel course = courseOpt.get();
            StudentModel student = studentOpt.get();

            // Tạo đăng ký mới
            EnrollmentModel enrollment = new EnrollmentModel();
            enrollment.setCourse(course);
            enrollment.setStudent(student);
            enrollment.setEnrollmentDate(LocalDateTime.now());

            // Nếu là khóa học miễn phí, đánh dấu thanh toán thành công luôn
            if (BigDecimal.ZERO.compareTo(course.getPrice()) == 0) {
                enrollment.setPaymentStatus("completed");
                enrollment.setPaymentAmount(BigDecimal.ZERO);
                enrollment.setPaymentMethod("free");
                enrollment.setPaymentDate(LocalDateTime.now());
            }

            // Lưu vào database
            enrollmentRepository.save(enrollment);
            return true;
        }

        return false;
    }

    /**
     * Lấy danh sách khóa học đã đăng ký của học viên hiện tại
     */
    public List<EnrollmentModel> getMyEnrollments(String username) {
        Long studentId = studentService.getStudentByUserName(username).getStudentId();
        return enrollmentRepository.findByStudent_StudentIdOrderByEnrollmentDateDesc(studentId);
    }

    /**
     * Hoàn thành thanh toán cho đăng ký khóa học
     */
//    @Transactional
//    public boolean completePayment(Long enrollmentId, BigDecimal amount, String method, String transactionId) {
//        Optional<EnrollmentModel> enrollmentOpt = enrollmentRepository.findById(enrollmentId);
//
//        if (enrollmentOpt.isPresent()) {
//            EnrollmentModel enrollment = enrollmentOpt.get();
//            enrollment.markAsPaid(amount, method, transactionId);
//            enrollmentRepository.save(enrollment);
//            return true;
//        }
//
//        return false;
//    }

    /**
     * Hoàn tiền cho đăng ký khóa học
     */
//    @Transactional
//    public boolean refundEnrollment(Integer enrollmentId, String reason) {
//        Optional<EnrollmentModel> enrollmentOpt = enrollmentRepository.findById(enrollmentId);
//
//        if (enrollmentOpt.isPresent()) {
//            EnrollmentModel enrollment = enrollmentOpt.get();
//            enrollment.refund(reason);
//            enrollmentRepository.save(enrollment);
//            return true;
//        }
//
//        return false;
//    }
}