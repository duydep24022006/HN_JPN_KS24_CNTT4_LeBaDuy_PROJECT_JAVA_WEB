package com.example.hospital_wed2.controller.admin;

import com.example.hospital_wed2.entity.PrescriptionStatus;
import com.example.hospital_wed2.repository.AppointmentRepository;
import com.example.hospital_wed2.repository.DoctorRepository;
import com.example.hospital_wed2.repository.PrescriptionRepository;
import com.example.hospital_wed2.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping({"/admin/dashboard","/admin/"})
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;

    public AdminDashboardController(UserRepository userRepository,
                                    DoctorRepository doctorRepository,
                                    AppointmentRepository appointmentRepository,
                                    PrescriptionRepository prescriptionRepository) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    @GetMapping
    public String dashboard(Model model, HttpServletRequest request) {
        long totalUsers = userRepository.count();
        long totalDoctors = doctorRepository.count();

        // BUG-08: Dùng query database thay vì findAll() + stream filter
        long todayAppointments = appointmentRepository.countByDate(LocalDate.now());
        long pendingPrescriptions = prescriptionRepository.countByStatus(PrescriptionStatus.PENDING);

        var recentAppointments = appointmentRepository.findTop5ByOrderByCreatedAtDesc();
        var recentPrescriptions = prescriptionRepository.findTop5ByOrderByCreatedAtDesc();
        var recentUsers = userRepository.findTop5ByOrderByCreatedAtDesc();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalDoctors", totalDoctors);
        model.addAttribute("todayAppointments", todayAppointments);
        model.addAttribute("pendingPrescriptions", pendingPrescriptions);
        model.addAttribute("recentAppointments", recentAppointments);
        model.addAttribute("recentPrescriptions", recentPrescriptions);
        model.addAttribute("recentUsers", recentUsers);
        model.addAttribute("currentDate", java.time.LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("currentUri",
                request.getRequestURI());

        return "admin/dashboard";
    }
}
