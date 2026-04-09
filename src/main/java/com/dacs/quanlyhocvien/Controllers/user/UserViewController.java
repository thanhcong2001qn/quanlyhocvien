package com.dacs.quanlyhocvien.Controllers.user;

import com.dacs.quanlyhocvien.DTO.Response.CourseDetailResponeDTO;
import com.dacs.quanlyhocvien.DTO.Response.CourseResponseDTO;
import com.dacs.quanlyhocvien.DTO.Response.LessonResponseDTO;
import com.dacs.quanlyhocvien.Services.*;
import com.dacs.quanlyhocvien.models.AccountModel;
import com.dacs.quanlyhocvien.models.StudentModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@PreAuthorize("hasRole('ROLE_STUDENT')")
@RequestMapping(value = "/user")
public class UserViewController {
    private final AccountService accountService;
    private final CourseService courseService;
    private final LessonService lessonService;
    private final StudentService studentService;
    private final EnrollmentService enrollmentService;

    public UserViewController(AccountService accountService, CourseService courseService, LessonService lessonService, StudentService studentService, EnrollmentService enrollmentService) {
        this.accountService = accountService;
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.studentService = studentService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping(value = "/profile")
    public String profile(){
        return "views/user/profile";
    }
    @GetMapping(value = "/all-course")
    public String allCourse(){
        return "views/user/course/all-course";
    }
    @GetMapping(value = "/course-detail/{courseId}")
    public String courseDetail(@PathVariable Long courseId, Model model, HttpServletRequest request) {
        try {
            // Lấy thông tin người dùng hiện tại (nếu đã đăng nhập)
            AccountModel currentUser = null;
            boolean isEnrolled = false;
            int progressPercentage = 0;
            HttpSession session = request.getSession();
            String username = (String) session.getAttribute("username");
            StudentModel student = null;
            if (username != null) {
                currentUser = accountService.getAccountByUsername(username);
                if (currentUser != null) {
                    // Giả sử StudentModel liên kết với AccountModel
                    student = studentService.getStudentByUserName(username);
                    if (student != null) {
                        // Kiểm tra trong repository nếu học viên đã đăng ký khóa học
                        isEnrolled = enrollmentService.checkEnrollment(courseId, student.getStudentId());
                    }
                }
            }

            // Lấy thông tin chi tiết khóa học
            CourseDetailResponeDTO courseDetail = courseService.getCourseDetail(courseId);

            if (courseDetail == null) {
                // Trả về trang lỗi nếu không tìm thấy khóa học
                return "error/404";
            }

            // Tính tiến độ học nếu học viên đã đăng ký
            if (isEnrolled && student != null) {
                int totalLessons = courseDetail.getTotalLessons() != null ? courseDetail.getTotalLessons() : 0;
                progressPercentage = enrollmentService.getProgressPercentage(courseId, student.getStudentId(), totalLessons);
            }

            // Lấy danh sách bài học của khóa học (đã bao gồm thông tin video)
            List<LessonResponseDTO> lessons = lessonService.getLessonsByCourseId(courseId);

            // Lấy các khóa học liên quan cùng danh mục
            List<CourseResponseDTO> relatedCourses = courseService.getRelatedCourses(courseId,
                    courseDetail.getCategory().getCategoryId(), 3);

            // Pass dữ liệu vào model
            model.addAttribute("course", courseDetail);
            model.addAttribute("lessons", lessons);
            model.addAttribute("relatedCourses", relatedCourses);
            model.addAttribute("isEnrolled", isEnrolled);
            model.addAttribute("progressPercentage", progressPercentage);

            return "views/user/course/course-detail";
        } catch (Exception e) {
            // Thêm thông báo lỗi vào model
            model.addAttribute("errorMessage", "Không thể lấy thông tin khóa học. Vui lòng thử lại sau!");

            return "error/general";
        }
    }
    @GetMapping(value = "/cart")
    public String cart(){
        return "views/user/cart";
    }

}
