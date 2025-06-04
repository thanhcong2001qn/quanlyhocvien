package com.dacs.quanlyhocvien.Controllers.user;

import com.dacs.quanlyhocvien.DTO.Response.CourseDetailResponeDTO;
import com.dacs.quanlyhocvien.DTO.Response.CourseResponseDTO;
import com.dacs.quanlyhocvien.DTO.Response.LessonResponseDTO;
import com.dacs.quanlyhocvien.Services.AccountService;
import com.dacs.quanlyhocvien.Services.CourseService;
import com.dacs.quanlyhocvien.Services.LessonService;
import com.dacs.quanlyhocvien.models.AccountModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(value = "/user")
public class UserViewController {
    private final AccountService accountService;
    private final CourseService courseService;
    private final LessonService lessonService;

    public UserViewController(AccountService accountService, CourseService courseService, LessonService lessonService) {
        this.accountService = accountService;
        this.courseService = courseService;
        this.lessonService = lessonService;
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
    public String courseDetail(@PathVariable Long courseId, Model model, Principal principal) {
        try {
            // Lấy thông tin người dùng hiện tại (nếu đã đăng nhập)
            AccountModel currentUser = null;
            boolean isEnrolled = false;

            if (principal != null) {
                String username = principal.getName();
                currentUser = accountService.getAccountByUsername(username);
            }

            // Lấy thông tin chi tiết khóa học
            CourseDetailResponeDTO courseDetail = courseService.getCourseDetail(courseId);

            if (courseDetail == null) {
                // Trả về trang lỗi nếu không tìm thấy khóa học
                return "error/404";
            }

            // Lấy danh sách bài học của khóa học
            List<LessonResponseDTO> lessons = lessonService.getLessonsByCourseId(courseId);

            // Lấy các khóa học liên quan cùng danh mục
            List<CourseResponseDTO> relatedCourses = courseService.getRelatedCourses(courseId,
                    courseDetail.getCategory().getCategoryId(), 3);

//            // Lấy đánh giá của khóa học
//            Page<ReviewDTO> reviews = reviewService.getCourseReviews(courseId, PageRequest.of(0, 5));

            // Pass dữ liệu vào model
            model.addAttribute("course", courseDetail);
            model.addAttribute("lessons", lessons);
            model.addAttribute("relatedCourses", relatedCourses);
//            model.addAttribute("reviews", reviews);
            model.addAttribute("isEnrolled", isEnrolled);

            // Nếu user đã đăng nhập, kiểm tra xem họ đã đánh giá khóa học này chưa
//            if (currentUser != null) {
//                ReviewDTO userReview = reviewService.getUserReviewForCourse(currentUser.getAccountId(), courseId);
//                model.addAttribute("userReview", userReview);
//                model.addAttribute("currentUser", currentUser);
//            }

            return "views/user/course/course-detail";
        } catch (Exception e) {
            // Log lỗi
//            logger.error("Error retrieving course details for ID: " + courseId, e);

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
