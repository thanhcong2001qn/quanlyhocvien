package com.dacs.quanlyhocvien.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model cho bảng Enrollment - quản lý đăng ký khóa học
 * @author thanhcong2001qn
 * @since 2025-04-24
 */
@Entity
@Table(name = "enrollment", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"course_id", "student_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseModel course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentModel student;

    @Column(name = "enrollment_date")
    private LocalDateTime enrollmentDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "payment_status", length = 50)
    private String paymentStatus;

    @Column(name = "payment_amount", precision = 10, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Kiểm tra xem đăng ký này đã hết hạn chưa
     * @return true nếu hết hạn, false nếu còn hiệu lực
     */
    public boolean isExpired() {
        if (expiryDate == null) {
            return false; // Không có ngày hết hạn = không hết hạn
        }
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Kiểm tra xem thanh toán đã hoàn tất chưa
     * @return true nếu đã thanh toán, false nếu chưa
     */
    public boolean isPaymentCompleted() {
        return "completed".equalsIgnoreCase(paymentStatus);
    }

    /**
     * Kiểm tra xem đăng ký này có giá trị không
     * @return true nếu đăng ký hợp lệ, false nếu không
     */
    public boolean isValid() {
        return !isExpired() && isPaymentCompleted();
    }

    @PrePersist
    protected void onCreate() {
        if (enrollmentDate == null) {
            enrollmentDate = LocalDateTime.now();
        }
        if (paymentStatus == null) {
            paymentStatus = "pending";
        }
    }
}