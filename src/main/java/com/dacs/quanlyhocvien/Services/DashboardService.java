package com.dacs.quanlyhocvien.Services;

import com.dacs.quanlyhocvien.Repository.*;
import com.dacs.quanlyhocvien.models.EnrollmentModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IStudentRepository studentRepository;
    private final ICourseRepository courseRepository;
    private final ICertificateRepository certificateRepository;
    private final IEnrollmentRepository iEnrollmentRepository;

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
        try {
            List<Object[]> result = iEnrollmentRepository.getRevenuePerMonth();
            if (result == null || result.isEmpty()) {
                return new LinkedHashMap<>(); // Trả về map rỗng nếu không có dữ liệu
            }

            // Sử dụng LinkedHashMap để đảm bảo thứ tự của các tháng
            Map<String, BigDecimal> revenueMap = new LinkedHashMap<>();

            for (Object[] row : result) {
                if (row[0] != null && row[1] != null) {
                    String month = row[0].toString();
                    BigDecimal amount = (BigDecimal) row[1];
                    revenueMap.put(month, amount);
                }
            }

            return revenueMap;
        } catch (Exception e) {
            // Log lỗi và trả về map rỗng
            // logger.error("Lỗi khi lấy dữ liệu doanh thu theo tháng: " + e.getMessage(), e);
            return new LinkedHashMap<>();
        }
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = iEnrollmentRepository.getTotalRevenue();
        System.out.println("✅ Total revenue from DB: " + revenue);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public Map<String, Long> getTransactionStatusStats() {
        List<Object[]> data = iEnrollmentRepository.getTransactionStatusStats();
        return data.stream().collect(Collectors.toMap(
                row -> row[0].toString(),  // PENDING, COMPLETED, etc.
                row -> (Long) row[1]
        ));
    }
    public List<EnrollmentModel> getRecentTransactions() {
        return iEnrollmentRepository.findTop5RecentWithJoins(PageRequest.of(0, 5));
    }
}
