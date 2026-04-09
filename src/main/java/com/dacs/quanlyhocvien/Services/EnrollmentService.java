package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.ICourseRepository;
import com.dacs.quanlyhocvien.Repository.IEnrollmentRepository;
import com.dacs.quanlyhocvien.Repository.IProgressRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentService {

    private final IEnrollmentRepository enrollmentRepository;
    private final ICourseRepository courseRepository;
    private final IStudentRepository studentRepository;
    private final StudentService studentService;
    private final IProgressRepository progressRepository;

    @Autowired
    public EnrollmentService(IEnrollmentRepository enrollmentRepository,
                             ICourseRepository courseRepository,
                             IStudentRepository studentRepository,
                             StudentService studentService,
                             IProgressRepository progressRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.studentService = studentService;
        this.progressRepository = progressRepository;
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

            course.setTotalStudents(course.getTotalStudents() + 1);
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

    /**
     * Thanh toán nhiều khóa học cùng lúc
     */
    @Transactional
    public boolean checkoutMultipleCourses(List<Long> courseIds, String username) {
        try {
            Long studentId = studentService.getStudentByUserName(username).getStudentId();
            Optional<StudentModel> studentOpt = studentRepository.findById(studentId);

            if (!studentOpt.isPresent()) {
                return false;
            }

            StudentModel student = studentOpt.get();
            List<CourseModel> coursesToEnroll = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            // Validate all courses and calculate total amount
            for (Long courseId : courseIds) {
                // Skip if already enrolled
                if (enrollmentRepository.existsByCourse_CourseIdAndStudent_StudentId(courseId, studentId)) {
                    continue;
                }

                Optional<CourseModel> courseOpt = courseRepository.findById(courseId);
                if (!courseOpt.isPresent()) {
                    // If any course is invalid, roll back transaction
                    return false;
                }

                CourseModel course = courseOpt.get();
                coursesToEnroll.add(course);

                // Calculate price (considering discounts if applicable)
                BigDecimal coursePrice;
                if (course.getDiscountPrice() != null && course.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0) {
                    coursePrice = course.getDiscountPrice();
                } else {
                    coursePrice = course.getPrice();
                }

                totalAmount = totalAmount.add(coursePrice);
            }

            // If no valid courses to enroll (all already enrolled or invalid), return true
            if (coursesToEnroll.isEmpty()) {
                return true;
            }

            // Create enrollment records for each course
            for (CourseModel course : coursesToEnroll) {
                EnrollmentModel enrollment = new EnrollmentModel();
                enrollment.setCourse(course);
                enrollment.setStudent(student);
                enrollment.setEnrollmentDate(LocalDateTime.now());

                // Determine the course price
                BigDecimal coursePrice;
                if (course.getDiscountPrice() != null && course.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0) {
                    coursePrice = course.getDiscountPrice();
                } else {
                    coursePrice = course.getPrice();
                }

                // For paid courses, set payment status to 'pending'
                if (coursePrice.compareTo(BigDecimal.ZERO) > 0) {
                    enrollment.setPaymentStatus("pending");
                    enrollment.setPaymentAmount(coursePrice);
                    // Payment method will be set after checkout
                } else {
                    // Free courses are marked as completed right away
                    enrollment.setPaymentStatus("completed");
                    enrollment.setPaymentAmount(BigDecimal.ZERO);
                    enrollment.setPaymentMethod("free");
                    enrollment.setPaymentDate(LocalDateTime.now());

                    // Update total students count for free courses
                    course.setTotalStudents(course.getTotalStudents() + 1);
                    courseRepository.save(course);
                }

                // Save enrollment
                enrollmentRepository.save(enrollment);
            }

            // Here you could also create an order record with the total amount
            // and link the enrollments to it

            return true;
        } catch (Exception e) {
            // Transaction will be rolled back due to @Transactional annotation
            return false;
        }
    }
    /**
     * Kiểm tra xem học viên đã đăng ký khóa học hay chưa
     * @param courseId ID của khóa học
     * @param studentId ID của học viên
     * @return true nếu đã đăng ký, false nếu chưa
     */
    public boolean checkEnrollment(Long courseId, Long studentId) {
        return enrollmentRepository.existsByCourse_CourseIdAndStudent_StudentId(courseId, studentId);
    }

    /**
     * Tính phần trăm hoàn thành khóa học của học viên
     * @param courseId ID của khóa học
     * @param studentId ID của học viên
     * @param totalLessons tổng số bài học trong khóa học
     * @return phần trăm hoàn thành (0-100)
     */
    public int getProgressPercentage(Long courseId, Long studentId, int totalLessons) {
        if (totalLessons == 0) {
            return 0;
        }
        int completed = progressRepository.countCompletedLessonsByStudentIdAndCourseId(studentId, courseId);
        return (int) Math.round(((double) completed * 100) / totalLessons);
    }
}