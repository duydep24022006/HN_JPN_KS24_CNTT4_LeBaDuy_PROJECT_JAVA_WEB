package com.example.hospital_wed2.controller.doctor;

import com.example.hospital_wed2.dto.doctor.DoctorStatsDto;
import com.example.hospital_wed2.dto.doctor.ExamineRequest;
import com.example.hospital_wed2.dto.profile.ChangePasswordRequest;
import com.example.hospital_wed2.dto.profile.DoctorProfileResponse;
import com.example.hospital_wed2.dto.profile.UpdateDoctorProfileRequest;
import com.example.hospital_wed2.entity.Appointment;
import com.example.hospital_wed2.entity.AppointmentStatus;
import com.example.hospital_wed2.entity.MedicalRecord;
import com.example.hospital_wed2.entity.Prescription;
import com.example.hospital_wed2.service.FileStorageService;
import com.example.hospital_wed2.service.doctor.DoctorService;
import com.example.hospital_wed2.service.profile.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final ProfileService profileService;
    private final FileStorageService fileStorageService;

    public DoctorController(DoctorService doctorService, ProfileService profileService, FileStorageService fileStorageService) {
        this.doctorService = doctorService;
        this.profileService = profileService;
        this.fileStorageService = fileStorageService;
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    private void addProfile(Model model) {
        DoctorProfileResponse profile = profileService.getDoctorProfile(currentUsername());
        model.addAttribute("profile", profile);
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        String username = currentUsername();
        addProfile(model);

        DoctorStatsDto stats = doctorService.getStats(username);
        model.addAttribute("todayAppointments", stats.getTodayAppointments());
        model.addAttribute("pendingCount", stats.getPendingCount());
        model.addAttribute("confirmedCount", stats.getConfirmedCount());
        model.addAttribute("completedToday", stats.getCompletedToday());
        model.addAttribute("totalPatients", stats.getTotalPatients());
        model.addAttribute("totalCompleted", stats.getTotalCompleted());
        model.addAttribute("totalCancelled", stats.getTotalCancelled());

        model.addAttribute("todaySchedule", doctorService.getTodaySchedule(username));
        model.addAttribute("recentAppointments", doctorService.getRecentAppointments(username));
        return "doctor/dashboard";
    }

    // =========================================================
    // APPOINTMENTS
    // =========================================================

    @GetMapping("/appointments")
    public String appointments(@RequestParam(required = false) String status, Model model) {
        String username = currentUsername();
        addProfile(model);

        List<Appointment> appointments = doctorService.getAppointments(username, status);
        model.addAttribute("appointments", appointments);
        model.addAttribute("selectedStatus", status);

        // Counts for filter tabs
        model.addAttribute("totalCount", doctorService.getAppointments(username, null).size());
        model.addAttribute("pendingCount", doctorService.countByStatus(username, "PENDING"));
        model.addAttribute("confirmedCount", doctorService.countByStatus(username, "CONFIRMED"));
        return "doctor/appointments";
    }

    @GetMapping("/appointments/{id}")
    public String appointmentDetail(@PathVariable Long id, Model model) {
        String username = currentUsername();
        addProfile(model);

        Appointment appointment = doctorService.getAppointment(username, id);
        model.addAttribute("appointment", appointment);

        // If completed, load medical record and prescription
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            MedicalRecord record = doctorService.getMedicalRecord(id);
            model.addAttribute("medicalRecord", record);
            model.addAttribute("prescription", doctorService.getPrescription(id));
        }

        // If confirmed, load medicines for prescription form
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            model.addAttribute("medicines", doctorService.getActiveMedicines());
            model.addAttribute("examineRequest", new ExamineRequest());
        }

        return "doctor/appointment-detail";
    }

    @PostMapping("/appointments/{id}/confirm")
    public String confirmAppointment(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            doctorService.confirmAppointment(currentUsername(), id);
            redirectAttrs.addFlashAttribute("successMessage", "Đã xác nhận lịch hẹn thành công!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/doctor/appointments/" + id;
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            doctorService.cancelAppointment(currentUsername(), id);
            redirectAttrs.addFlashAttribute("successMessage", "Đã hủy lịch hẹn.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/doctor/appointments";
    }

    @PostMapping("/appointments/{id}/examine")
    public String examine(@PathVariable Long id,
                          @Valid @ModelAttribute ExamineRequest request,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttrs) {
        String username = currentUsername();

        if (bindingResult.hasErrors()) {
            addProfile(model);
            Appointment appointment = doctorService.getAppointment(username, id);
            model.addAttribute("appointment", appointment);
            model.addAttribute("medicines", doctorService.getActiveMedicines());
            model.addAttribute("examineRequest", request);
            // Collect validation error messages
            String errors = bindingResult.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Dữ liệu không hợp lệ");
            model.addAttribute("errorMessage", errors);
            return "doctor/appointment-detail";
        }

        try {
            doctorService.examine(username, id, request);
            redirectAttrs.addFlashAttribute("successMessage", "Hoàn tất khám bệnh! Bệnh án đã được lưu.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/doctor/appointments/" + id;
        }
        return "redirect:/doctor/appointments/" + id;
    }

    // =========================================================
    // MEDICAL RECORDS
    // =========================================================

    @GetMapping("/medical-records")
    public String medicalRecords(Model model) {
        String username = currentUsername();
        addProfile(model);
        model.addAttribute("medicalRecords", doctorService.getMedicalRecords(username));
        return "doctor/medical-records";
    }

    // =========================================================
    // PROFILE
    // =========================================================

    @GetMapping("/profile")
    public String profile(Model model) {
        addProfile(model);
        return "doctor/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @Valid @ModelAttribute("profile") UpdateDoctorProfileRequest request,
            BindingResult bindingResult,
            @RequestParam(required = false) MultipartFile avatarFile,
            Model model,
            RedirectAttributes redirectAttrs
    ){
        if (bindingResult.hasErrors()) {
            addProfile(model);
            String errors = bindingResult.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Dữ liệu không hợp lệ");
            model.addAttribute("errorMessage", errors);
            return "doctor/profile";
        }
        if (avatarFile != null && !avatarFile.isEmpty()) {

            String fileName = fileStorageService.storeFile(avatarFile);

            request.setAvatarUrl(fileName);
        }
        try {
            profileService.updateDoctorProfile(currentUsername(), request);
            redirectAttrs.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/doctor/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@Valid @ModelAttribute ChangePasswordRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Dữ liệu không hợp lệ");
            redirectAttrs.addFlashAttribute("errorMessage", errors);
            return "redirect:/doctor/profile#password";
        }

        try {
            profileService.changePassword(currentUsername(), request);
            redirectAttrs.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/doctor/profile#password";
    }

    // =========================================================
    // STATISTICS
    // =========================================================

    @GetMapping("/statistics")
    public String statistics(Model model) {
        String username = currentUsername();
        addProfile(model);

        DoctorStatsDto stats = doctorService.getStats(username);
        model.addAttribute("stats", stats);
        model.addAttribute("medicalRecords", doctorService.getMedicalRecords(username));
        model.addAttribute("recentAppointments", doctorService.getRecentAppointments(username));
        return "doctor/statistics";
    }
}