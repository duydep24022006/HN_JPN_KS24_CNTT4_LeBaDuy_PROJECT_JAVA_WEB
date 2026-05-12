package com.example.hospital_wed2.controller.doctor;

import com.example.hospital_wed2.dto.doctor.DoctorStatsDto;
import com.example.hospital_wed2.dto.profile.doctor.DoctorProfileResponse;
import com.example.hospital_wed2.entity.Appointment;
import com.example.hospital_wed2.service.doctor.DoctorDashboardService;
import com.example.hospital_wed2.service.doctor.DoctorProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorDashboardController {

    private final DoctorDashboardService doctorDashboardService;
    private final DoctorProfileService doctorProfileService;

    @GetMapping("/dashboard")
    public String dashboard(
            Model model,
            Authentication auth,
            HttpServletRequest request
    ) {

        String email = auth.getName();

        DoctorProfileResponse profile =
                doctorProfileService.getDoctorProfile(email);

        DoctorStatsDto stats =
                doctorDashboardService.getDashboardData(email);

        List<Appointment> todaySchedule =
                doctorDashboardService.getTodaySchedule(email);

        List<Appointment> recentAppointments =
                doctorDashboardService.getRecentAppointments(email);

        model.addAttribute("profile", profile);
        model.addAttribute("stats", stats);
        model.addAttribute("currentUri", request.getRequestURI());

        // CARD
        model.addAttribute("todayAppointments", todaySchedule.size());
        model.addAttribute("pendingCount", stats.getPendingCount());
        model.addAttribute("completedToday", stats.getCompletedToday());
        model.addAttribute("totalPatients", stats.getTotalPatients());

        // LIST
        model.addAttribute("todaySchedule", todaySchedule);
        model.addAttribute("recentAppointments", recentAppointments);

        return "doctor/dashboard";
    }
}