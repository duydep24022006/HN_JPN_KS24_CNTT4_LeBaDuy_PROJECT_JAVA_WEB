package com.example.hospital_wed2.controller.patient;

import com.example.hospital_wed2.dto.patient.PatientDashboardData;
import com.example.hospital_wed2.entity.AppointmentStatus;
import com.example.hospital_wed2.service.patient.PatientAppointmentService;
import com.example.hospital_wed2.service.patient.PatientDashboardService;
import com.example.hospital_wed2.service.patient.PatientProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/patient/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final PatientDashboardService patientDashboardService;
    private final PatientProfileService patientProfileService;
    private final PatientAppointmentService patientAppointmentService;

    @GetMapping
    public String dashboard(Model model, Authentication auth, HttpServletRequest request) {
        String email = auth.getName();

        PatientDashboardData data = patientDashboardService.getDashboardData(email);

        // Flat attributes cho template
        model.addAttribute("profile",               patientProfileService.getMyProfile(email));
        model.addAttribute("totalAppointments",     data.getTotalAppointments());
        model.addAttribute("countConfirmed",        data.getConfirmedAppointments());
        model.addAttribute("countPending",          data.getPendingAppointments());
        model.addAttribute("totalRecords",          data.getTotalMedicalRecords());

        // Danh sách lịch hẹn cho widget "Lịch hẹn sắp tới"
        model.addAttribute("upcomingAppointments",
                patientAppointmentService.getMyAppointmentsByStatus(email, AppointmentStatus.CONFIRMED));
        model.addAttribute("pendingAppointments",
                patientAppointmentService.getMyAppointmentsByStatus(email, AppointmentStatus.PENDING));

        model.addAttribute("currentUri", request.getRequestURI());

        return "patient/dashboard";
    }
}