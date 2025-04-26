package com.dacs.quanlyhocvien.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model đại diện cho mục trong giỏ hàng của học viên
 * @author thanhcong2001qn
 * @since 2025-04-25
 */
@Entity
@Data
@Table(name = "cart_item", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "course_id"})
})
public class CartItemModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentModel student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseModel course;

    @Column(name = "quantity")
    private Integer quantity = 1;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public CartItemModel() {
    }

    public CartItemModel(StudentModel student, CourseModel course) {
        this.student = student;
        this.course = course;
        this.price = course.getPrice();
        this.discountPrice = course.getDiscountPrice();
    }
    /**
     * Lấy giá hiện tại (có tính khuyến mãi nếu có)
     * @return Giá hiện tại của item trong giỏ hàng
     */
    public BigDecimal getCurrentPrice() {
        return discountPrice != null ? discountPrice : price;
    }

    /**
     * Tính tổng tiền cho item này
     * @return Tổng tiền = giá hiện tại * số lượng
     */
    public BigDecimal getSubtotal() {
        BigDecimal currentPrice = getCurrentPrice();
        return currentPrice != null ? currentPrice.multiply(new BigDecimal(quantity)) : BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "CartItemModel{" +
                "cartItemId=" + cartItemId +
                ", studentId=" + (student != null ? student.getStudentId() : null) +
                ", courseId=" + (course != null ? course.getCourseId() : null) +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}