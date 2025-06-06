package com.dacs.quanlyhocvien.Controllers.admin;

import com.dacs.quanlyhocvien.Services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/home")
    public String viewDashboard(Model model) {
        model.addAttribute("totalStudents", dashboardService.getTotalStudents());
        model.addAttribute("totalRevenue", dashboardService.getTotalRevenue());
        model.addAttribute("recentTransactions", dashboardService.getRecentTransactions());

        Map<String, BigDecimal> monthlyRevenue = dashboardService.getMonthlyRevenue();
        model.addAttribute("monthlyRevenueLabels", monthlyRevenue.keySet());
        model.addAttribute("monthlyRevenueValues", monthlyRevenue.values());

        model.addAttribute("totalCourses", dashboardService.getTotalCourses());
        model.addAttribute("issuedCertificates", dashboardService.getIssuedCertificates());

        Map<String, Long> transactionStats = dashboardService.getTransactionStatusStats();
        if (transactionStats == null) transactionStats = new HashMap<>();
        model.addAttribute("transactionLabels", transactionStats.keySet());
        model.addAttribute("transactionValues", transactionStats.values());
        return "views/admin/dashboard";
    }
}

