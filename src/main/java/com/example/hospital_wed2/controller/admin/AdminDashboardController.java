package com.example.hospital_wed2.controller.admin;

import com.example.hospital_wed2.dto.admin.AdminDashboardStats;
import com.example.hospital_wed2.service.admin.AdminDashboardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.time.LocalDate;

@Controller
@RequestMapping({"/admin/dashboard", "/admin/"})
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping
    public String dashboard(Model model, HttpServletRequest request) {
        AdminDashboardStats stats = dashboardService.getDashboardStats();

        model.addAttribute("stats", stats);
        model.addAttribute("totalUsers", stats.getTotalUsers());
        model.addAttribute("totalDoctors", stats.getTotalDoctors());
        model.addAttribute("todayAppointments", stats.getTodayAppointments());
        model.addAttribute("pendingPrescriptions", stats.getPendingPrescriptions());

        model.addAttribute("recentAppointments", dashboardService.getRecentAppointments());
        model.addAttribute("recentPrescriptions", dashboardService.getRecentPrescriptions());
        model.addAttribute("recentUsers", dashboardService.getRecentUsers());

        model.addAttribute("topDoctors", dashboardService.getTopDoctors());
        model.addAttribute("topMedicines", dashboardService.getTopMedicines());
        model.addAttribute("monthlyAppointments", dashboardService.getMonthlyAppointments());

        model.addAttribute("currentDate", LocalDate.now().toString());
        model.addAttribute("currentUri", request.getRequestURI());

        return "admin/dashboard";
    }
}