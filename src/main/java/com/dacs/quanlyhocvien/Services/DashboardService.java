package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.*;
import com.dacs.quanlyhocvien.models.StudentCourseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IStudentRepository studentRepository;
    private final IDashboardRepository dashboardRepository;
    private final ICourseRepository courseRepository;
    private final ICertificateRepository certificateRepository;

    public long getTotalCourses() {
        return courseRepository.count();
    }

    public long getIssuedCertificates() {
        return certificateRepository.count();
    }

    public long getTotalStudents() {
        long total = studentRepository.count();
        System.out.println("✅ Total students: " + total);
        return total;
    }

    public Map<String, BigDecimal> getMonthlyRevenue() {
        List<Object[]> result = dashboardRepository.getRevenuePerMonth();
        return result.stream().collect(Collectors.toMap(
                row -> row[0].toString(), // tháng: '2025-06'
                row -> (BigDecimal) row[1]
        ));
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = dashboardRepository.getTotalRevenue();
        System.out.println("✅ Total revenue from DB: " + revenue);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public Map<String, Long> getTransactionStatusStats() {
        List<Object[]> data = dashboardRepository.getTransactionStatusStats();
        return data.stream().collect(Collectors.toMap(
                row -> row[0].toString(),  // PENDING, COMPLETED, etc.
                row -> (Long) row[1]
        ));
    }
    public List<StudentCourseModel> getRecentTransactions() {
        return dashboardRepository.findTop5RecentWithJoins(PageRequest.of(0, 5));
    }
}
