package com.example.hospital_wed2.controller.patient;

import com.example.hospital_wed2.entity.AppointmentStatus;
import com.example.hospital_wed2.entity.User;
import com.example.hospital_wed2.repository.AppointmentRepository;
import com.example.hospital_wed2.repository.MedicalRecordRepository;
import com.example.hospital_wed2.repository.UserRepository;
import com.example.hospital_wed2.service.profile.ProfileService;
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

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final ProfileService profileService;
    @GetMapping
    public String dashboard(Model model,
                            Authentication auth,
                            HttpServletRequest request) {

        User patient = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var profile = profileService.getMyProfile(auth.getName());
        model.addAttribute("profile", profile);

        var confirmed = appointmentRepository.findByPatientAndStatus(patient, AppointmentStatus.CONFIRMED);
        var pending   = appointmentRepository.findByPatientAndStatus(patient, AppointmentStatus.PENDING);
        var allAppts  = appointmentRepository.findByPatientWithDetails(patient);
        var records   = medicalRecordRepository.findByPatientWithDetails(patient);

        model.addAttribute("upcomingAppointments", confirmed);
        model.addAttribute("pendingAppointments", pending);
        model.addAttribute("totalAppointments", allAppts.size());
        model.addAttribute("totalRecords", records.size());
        model.addAttribute("countConfirmed", confirmed.size());
        model.addAttribute("countPending", pending.size());

        // THÊM DÒNG NÀY
        model.addAttribute("currentUri", request.getRequestURI());

        return "patient/dashboard";
    }
}
