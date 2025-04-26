package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.ICartItemRepository;
import com.dacs.quanlyhocvien.Repository.ICourseRepository;
import com.dacs.quanlyhocvien.Repository.IStudentRepository;
import com.dacs.quanlyhocvien.models.CartItemModel;
import com.dacs.quanlyhocvien.models.CourseModel;
import com.dacs.quanlyhocvien.models.StudentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final ICartItemRepository cartItemRepository;
    private final ICourseRepository courseRepository;
    private final IStudentRepository studentRepository;
    private final StudentService studentService;

    @Autowired
    public CartService(ICartItemRepository cartItemRepository,
                       ICourseRepository courseRepository,
                       IStudentRepository studentRepository, StudentService studentService) {
        this.cartItemRepository = cartItemRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.studentService = studentService;
    }

    /**
     * Lấy ID của học viên hiện tại đăng nhập
     * @return ID của học viên
     */

    /**
     * Lấy danh sách các item trong giỏ hàng của học viên hiện tại
     */
    public List<CartItemModel> getCartItems(String username) {
        Long studentId = studentService.getStudentByUserName(username).getStudentId();
        return cartItemRepository.findByStudent_StudentIdOrderByCreatedAtDesc(studentId);
    }

    /**
     * Thêm khóa học vào giỏ hàng
     * @param courseId ID của khóa học
     * @return true nếu thêm thành công
     */
    @Transactional
    public boolean addToCart(Long courseId,String username) {
        Long studentId = studentService.getStudentByUserName(username).getStudentId();

        // Kiểm tra xem khóa học đã có trong giỏ hàng chưa
        if (cartItemRepository.existsByStudent_StudentIdAndCourse_CourseId(studentId, courseId)) {
            // Nếu đã có, tăng số lượng lên 1
            return updateCartItemQuantity(courseId, 1,username);
        }

        // Nếu chưa có, tạo mới
        Optional<CourseModel> courseOpt = courseRepository.findById(courseId);
        Optional<StudentModel> studentOpt = studentRepository.findById(studentId);

        if (courseOpt.isPresent() && studentOpt.isPresent()) {
            CourseModel course = courseOpt.get();
            StudentModel student = studentOpt.get();

            CartItemModel cartItem = new CartItemModel(student, course);
            cartItem.setPrice(course.getPrice());
            cartItem.setDiscountPrice(course.getDiscountPrice());

            cartItemRepository.save(cartItem);
            return true;
        }

        return false;
    }

    /**
     * Cập nhật số lượng của một item trong giỏ hàng
     * @param courseId ID của khóa học
     * @param quantity Số lượng mới
     * @return true nếu cập nhật thành công
     */
    @Transactional
    public boolean updateCartItemQuantity(Long courseId, Integer quantity,String username) {
        if (quantity <= 0) {
            // Nếu số lượng <= 0, xóa khỏi giỏ hàng
            return removeFromCart(courseId,username);
        }

        Long studentId = studentService.getStudentByUserName(username).getStudentId();
        Optional<CartItemModel> cartItemOpt = cartItemRepository.findByStudent_StudentIdAndCourse_CourseId(studentId, courseId);

        if (cartItemOpt.isPresent()) {
            CartItemModel cartItem = cartItemOpt.get();
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
            return true;
        }

        return false;
    }

    /**
     * Xóa một khóa học khỏi giỏ hàng
     * @param courseId ID của khóa học
     * @return true nếu xóa thành công
     */
    @Transactional
    public boolean removeFromCart(Long courseId,String username) {
        Long studentId = studentService.getStudentByUserName(username).getStudentId();

        try {
            cartItemRepository.deleteByStudent_StudentIdAndCourse_CourseId(studentId, courseId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Xóa toàn bộ giỏ hàng
     * @return true nếu xóa thành công
     */
    @Transactional
    public boolean clearCart(String username) {
        Long studentId = studentService.getStudentByUserName(username).getStudentId();

        try {
            cartItemRepository.deleteByStudent_StudentId(studentId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Đếm số lượng item trong giỏ hàng
     */
    public Long getCartItemCount(String username) {
        Long studentId = studentService.getStudentByUserName(username).getStudentId();
        return cartItemRepository.countByStudentId(studentId);
    }

    /**
     * Tính tổng tiền giỏ hàng
     */
    public BigDecimal getCartTotal(String username) {
        List<CartItemModel> cartItems = getCartItems(username);
        return cartItems.stream()
                .map(CartItemModel::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}