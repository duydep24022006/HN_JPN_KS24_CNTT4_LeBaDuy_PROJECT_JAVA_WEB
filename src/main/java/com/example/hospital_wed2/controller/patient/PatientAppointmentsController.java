package com.example.hospital_wed2.controller.patient;

import com.example.hospital_wed2.entity.AppointmentStatus;
import com.example.hospital_wed2.entity.Doctor;
import com.example.hospital_wed2.repository.DoctorRepository;
import com.example.hospital_wed2.repository.SpecialtyRepository;
import com.example.hospital_wed2.service.patient.PatientAppointmentService;
import com.example.hospital_wed2.service.patient.PatientProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/patient/appointments")
@RequiredArgsConstructor
public class PatientAppointmentsController {

    private final PatientAppointmentService patientAppointmentService;
    private final PatientProfileService patientProfileService;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;

    private String getCurrentEmail(Authentication auth) {
        return auth.getName();
    }

    @GetMapping
    public String listAppointments(
            @RequestParam(required = false) String status,
            Model model,
            Authentication auth,
            HttpServletRequest request) {

        String email = getCurrentEmail(auth);
        model.addAttribute("profile", patientProfileService.getMyProfile(email));

        if (status != null && !status.isBlank()) {
            try {
                AppointmentStatus st = AppointmentStatus.valueOf(status);
                model.addAttribute("appointments", patientAppointmentService.getMyAppointmentsByStatus(email, st));
            } catch (IllegalArgumentException e) {
                model.addAttribute("appointments", patientAppointmentService.getMyAppointments(email));
                status = null;
            }
        } else {
            model.addAttribute("appointments", patientAppointmentService.getMyAppointments(email));
        }

        model.addAttribute("selectedStatus", status);
        model.addAttribute("currentUri", request.getRequestURI());
        return "patient/appointments";
    }

    @GetMapping("/booking")
    public String bookingForm(
            @RequestParam(required = false) Long specialtyId,
            Model model,
            Authentication auth,
            HttpServletRequest request) {

        String email = getCurrentEmail(auth);
        model.addAttribute("profile", patientProfileService.getMyProfile(email));
        model.addAttribute("specialties", specialtyRepository.findAll());

        List<Doctor> doctors;
        if (specialtyId != null) {
            doctors = doctorRepository.findAll().stream()
                    .filter(d -> d.getSpecialty().getId().equals(specialtyId))
                    .toList();
        } else {
            doctors = doctorRepository.findAll();
        }
        model.addAttribute("doctors", doctors);
        model.addAttribute("selectedSpecialtyId", specialtyId);
        model.addAttribute("today", LocalDate.now().toString());
        model.addAttribute("currentUri", request.getRequestURI());

        return "patient/appointment-booking";
    }

    @GetMapping("/booking/get-booked-slots")
    @ResponseBody
    public Map<String, Object> getBookedSlots(
            @RequestParam Long doctorId,
            @RequestParam String appointmentDate) {

        Map<String, Object> result = new HashMap<>();
        List<String> bookedTimes;

        try {
            LocalDate date = LocalDate.parse(appointmentDate);
            bookedTimes = patientAppointmentService.getBookedSlots(doctorId, date)
                    .stream()
                    .map(LocalTime::toString)
                    .toList();
        } catch (Exception e) {
            bookedTimes = List.of();
        }

        result.put("bookedSlots", bookedTimes);
        return result;
    }

    @PostMapping("/booking")
    public String submitBooking(
            @RequestParam Long doctorId,
            @RequestParam String appointmentDate,
            @RequestParam String appointmentTime,
            @RequestParam(required = false) String reason,
            Authentication auth,
            RedirectAttributes ra) {

        String email = getCurrentEmail(auth);

        try {
            LocalDate date = LocalDate.parse(appointmentDate);
            LocalTime time = LocalTime.parse(appointmentTime);
            patientAppointmentService.bookAppointment(email, doctorId, date, time, reason);
            ra.addFlashAttribute("successMessage", "Đặt lịch thành công! Lịch hẹn đang chờ xác nhận.");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("errorMessage",
                    "Khung giờ này vừa bị đặt bởi người khác. Vui lòng chọn giờ khác.");
            return "redirect:/patient/appointments/booking";
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/patient/appointments/booking";
        }

        return "redirect:/patient/appointments";
    }

    @PostMapping("/{id}/cancel")
    public String cancelAppointment(
            @PathVariable Long id,
            Authentication auth,
            RedirectAttributes ra) {

        String email = getCurrentEmail(auth);

        try {
            patientAppointmentService.cancelAppointment(email, id);
            ra.addFlashAttribute("successMessage", "Đã hủy lịch hẹn thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/patient/appointments";
    }
}