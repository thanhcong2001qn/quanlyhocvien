package com.dacs.quanlyhocvien.Repository;

import com.dacs.quanlyhocvien.models.StudentCourseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

public interface IDashboardRepository extends JpaRepository<StudentCourseModel, Long> {

    @Query("SELECT SUM(sc.amountPaid) FROM StudentCourseModel sc WHERE sc.paymentStatus = 'COMPLETED'")
    BigDecimal getTotalRevenue();

    List<StudentCourseModel> findTop5ByOrderByPurchaseDateDesc();

    @Query("SELECT FUNCTION('DATE_FORMAT', sc.purchaseDate, '%Y-%m') AS month, SUM(sc.amountPaid) " +
            "FROM StudentCourseModel sc WHERE sc.paymentStatus = 'COMPLETED' " +
            "GROUP BY FUNCTION('DATE_FORMAT', sc.purchaseDate, '%Y-%m') ORDER BY month ASC")
    List<Object[]> getRevenuePerMonth();

    @Query("SELECT sc.paymentStatus, COUNT(sc) FROM StudentCourseModel sc GROUP BY sc.paymentStatus")
    List<Object[]> getTransactionStatusStats();


    @Query("""
    SELECT sc FROM StudentCourseModel sc
    JOIN FETCH sc.student s
    JOIN FETCH s.account a
    JOIN FETCH sc.course c
    ORDER BY sc.purchaseDate DESC
""")
    List<StudentCourseModel> findTop5RecentWithJoins(Pageable pageable);

}
