package com.example.hospital_wed2.controller.doctor;

import com.example.hospital_wed2.dto.doctor.ExamineRequest;
import com.example.hospital_wed2.entity.Appointment;
import com.example.hospital_wed2.service.admin.AdminMedicineService;
import com.example.hospital_wed2.service.doctor.DoctorAppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/doctor/appointments")
@RequiredArgsConstructor
public class DoctorAppointmentsController {

    private final DoctorAppointmentService doctorAppointmentService;
    private final AdminMedicineService medicineService;

    @GetMapping
    public String appointments(
            @RequestParam(required = false) String status,
            Authentication auth,
            Model model
    ) {
        List<Appointment> appointments =
                doctorAppointmentService.getMyAppointments(getCurrentEmail(auth), status);

        model.addAttribute("appointments", appointments);
        model.addAttribute("selectedStatus", status);

        return "doctor/appointments";
    }

    @GetMapping("/{id}")
    public String appointmentDetail(
            @PathVariable Long id,
            Authentication auth,
            Model model
    ) {
        Appointment appointment =
                doctorAppointmentService.getAppointment(getCurrentEmail(auth), id);

        model.addAttribute("appointment", appointment);
        model.addAttribute("examineRequest", new ExamineRequest());
        model.addAttribute("medicines", medicineService.findAll());

        return "doctor/appointment-detail";
    }

    @PostMapping("/{id}/confirm")
    public String confirmAppointment(
            @PathVariable Long id,
            Authentication auth,
            RedirectAttributes ra
    ) {
        try {
            doctorAppointmentService.confirmAppointment(getCurrentEmail(auth), id);
            ra.addFlashAttribute("successMessage", "Đã xác nhận lịch hẹn!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/doctor/appointments";
    }

    @PostMapping("/{id}/cancel")
    public String cancelAppointment(
            @PathVariable Long id,
            Authentication auth,
            RedirectAttributes ra
    ) {
        try {
            doctorAppointmentService.cancelAppointment(getCurrentEmail(auth), id);
            ra.addFlashAttribute("successMessage", "Đã hủy lịch hẹn!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/doctor/appointments";
    }

    @PostMapping("/{id}/examine")
    public String examineAppointment(
            @PathVariable Long id,
            @ModelAttribute ExamineRequest examineRequest,
            Authentication auth,
            RedirectAttributes ra
    ) {
        try {
            doctorAppointmentService.examine(getCurrentEmail(auth), id, examineRequest);
            ra.addFlashAttribute("successMessage", "Khám bệnh thành công!");
            return "redirect:/doctor/appointments";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi khi lưu bệnh án: " + e.getMessage());
            return "redirect:/doctor/appointments/" + id;
        }
    }

    private String getCurrentEmail(Authentication auth) {
        return auth.getName();
    }
}