package com.dacs.quanlyhocvien.Controllers.admin;

import com.dacs.quanlyhocvien.Services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/home")
    public String viewDashboard(Model model) {
        try {
            model.addAttribute("totalStudents", dashboardService.getTotalStudents());
            model.addAttribute("totalRevenue", dashboardService.getTotalRevenue());
            model.addAttribute("recentTransactions", dashboardService.getRecentTransactions());

            Map<String, BigDecimal> monthlyRevenue = dashboardService.getMonthlyRevenue();
            // Đảm bảo có dữ liệu
            if (monthlyRevenue == null) {
                monthlyRevenue = new LinkedHashMap<>();
            }

            // Tạo danh sách các tháng để hiển thị trên chart
            List<String> sortedMonths = new ArrayList<>(monthlyRevenue.keySet());
            // (Không cần sắp xếp vì đã sắp xếp trong query và sử dụng LinkedHashMap)

            // Tạo danh sách các giá trị doanh thu tương ứng
            List<BigDecimal> revenueValues = sortedMonths.stream()
                    .map(monthlyRevenue::get)
                    .collect(Collectors.toList());

            model.addAttribute("monthlyRevenueLabels", sortedMonths);
            model.addAttribute("monthlyRevenueValues", revenueValues);

            model.addAttribute("totalCourses", dashboardService.getTotalCourses());
            model.addAttribute("issuedCertificates", dashboardService.getIssuedCertificates());

            Map<String, Long> transactionStats = dashboardService.getTransactionStatusStats();
            if (transactionStats == null) {
                transactionStats = new LinkedHashMap<>();
            }

            model.addAttribute("transactionLabels", transactionStats.keySet());
            model.addAttribute("transactionValues", transactionStats.values());

            return "views/admin/Dashboard";
        } catch (Exception e) {
            // Log lỗi và hiển thị trang lỗi hoặc trang dashboard với thông báo
            // logger.error("Lỗi khi hiển thị dashboard: " + e.getMessage(), e);
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi tải dữ liệu dashboard. Vui lòng thử lại sau.");
            return "views/admin/Dashboard"; // Hoặc return "error";
        }
    }
}

