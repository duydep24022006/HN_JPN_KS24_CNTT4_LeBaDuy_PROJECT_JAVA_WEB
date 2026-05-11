package com.example.hospital_wed2.controller.patient;

import com.example.hospital_wed2.entity.*;
import com.example.hospital_wed2.repository.AppointmentRepository;
import com.example.hospital_wed2.repository.DoctorRepository;
import com.example.hospital_wed2.repository.SpecialtyRepository;
import com.example.hospital_wed2.repository.UserRepository;
import com.example.hospital_wed2.service.profile.ProfileService;
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
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/patient/appointments")
@RequiredArgsConstructor
public class PatientAppointmentsController {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final UserRepository userRepository;
    private final ProfileService profileService;

    @GetMapping
    public String listAppointments(
            @RequestParam(required = false) String status,
            Model model,
            Authentication auth,
            HttpServletRequest request
            ) {

        User patient = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("profile", profileService.getMyProfile(auth.getName()));

        List<Appointment> appointments;
        if (status != null && !status.isBlank()) {
            try {
                AppointmentStatus st = AppointmentStatus.valueOf(status);
                appointments = appointmentRepository.findByPatientAndStatusDesc(patient, st);
            } catch (IllegalArgumentException e) {
                appointments = appointmentRepository.findByPatientWithDetails(patient);
                status = null;
            }
        } else {
            appointments = appointmentRepository.findByPatientWithDetails(patient);
        }

        model.addAttribute("appointments", appointments);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("currentUri", request.getRequestURI());
        return "patient/appointments";
    }

    @GetMapping("/booking")
    public String bookingForm(
            @RequestParam(required = false) Long specialtyId,
            Model model, Authentication auth, HttpServletRequest request) {

        model.addAttribute("profile", profileService.getMyProfile(auth.getName()));
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

    // BUG-10: Dùng query đích thực thay vì findAll() + stream filter
    @GetMapping("/booking/get-booked-slots")
    @ResponseBody
    public Map<String, Object> getBookedSlots(
            @RequestParam Long doctorId,
            @RequestParam String appointmentDate) {

        Map<String, Object> result = new HashMap<>();
        List<String> bookedTimes = new java.util.ArrayList<>();

        try {
            LocalDate date = LocalDate.parse(appointmentDate);
            bookedTimes = appointmentRepository
                    .findBookedSlots(doctorId, date)
                    .stream()
                    .map(LocalTime::toString)
                    .toList();
        } catch (Exception e) {
            // Nếu lỗi parse date, trả về rỗng
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

        User patient = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        LocalDate date = LocalDate.parse(appointmentDate);
        LocalTime time = LocalTime.parse(appointmentTime);

        // BUG-06: Backend validation - không cho đặt lịch quá khứ
        if (date.isBefore(LocalDate.now())) {
            ra.addFlashAttribute("errorMessage", "Không thể đặt lịch vào ngày trong quá khứ");
            return "redirect:/patient/appointments/booking";
        }

        // BUG-09: Kiểm tra trùng lịch
        boolean existsPending = appointmentRepository
                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
                        doctorId, date, time, AppointmentStatus.PENDING);
        boolean existsConfirmed = appointmentRepository
                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
                        doctorId, date, time, AppointmentStatus.CONFIRMED);

        if (existsPending || existsConfirmed) {
            ra.addFlashAttribute("errorMessage", "Khung giờ này đã được đặt. Vui lòng chọn giờ khác.");
            return "redirect:/patient/appointments/booking";
        }

        Appointment appt = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(date)
                .appointmentTime(time)
                .reason(reason)
                .status(AppointmentStatus.PENDING)
                .build();

        try {
            appointmentRepository.save(appt);
        } catch (DataIntegrityViolationException e) {
            // BUG-09: Bắt race condition - unique constraint vi phạm
            ra.addFlashAttribute("errorMessage",
                    "Khung giờ này vừa bị đặt bởi người khác. Vui lòng chọn giờ khác.");
            return "redirect:/patient/appointments/booking";
        }

        ra.addFlashAttribute("successMessage", "Đặt lịch thành công! Lịch hẹn đang chờ xác nhận.");
        return "redirect:/patient/appointments";
    }

    @PostMapping("/{id}/cancel")
    public String cancelAppointment(
            @PathVariable Long id,
            Authentication auth,
            RedirectAttributes ra) {

        User patient = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appt.getPatient().getId().equals(patient.getId())) {
            ra.addFlashAttribute("errorMessage", "Bạn không có quyền hủy lịch hẹn này.");
            return "redirect:/patient/appointments";
        }
        if (appt.getStatus() == AppointmentStatus.COMPLETED || appt.getStatus() == AppointmentStatus.CANCELLED) {
            ra.addFlashAttribute("errorMessage", "Không thể hủy lịch hẹn đã hoàn thành hoặc đã hủy.");
            return "redirect:/patient/appointments";
        }

        appt.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appt);
        ra.addFlashAttribute("successMessage", "Đã hủy lịch hẹn thành công.");
        return "redirect:/patient/appointments";
    }
}
