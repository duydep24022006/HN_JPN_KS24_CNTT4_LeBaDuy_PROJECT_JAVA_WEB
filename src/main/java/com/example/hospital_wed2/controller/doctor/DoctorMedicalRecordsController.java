package com.example.hospital_wed2.controller.doctor;

import com.example.hospital_wed2.entity.Doctor;
import com.example.hospital_wed2.entity.MedicalRecord;
import com.example.hospital_wed2.entity.PrescriptionStatus;
import com.example.hospital_wed2.entity.User;
import com.example.hospital_wed2.exception.ResourceNotFoundException;
import com.example.hospital_wed2.repository.DoctorRepository;
import com.example.hospital_wed2.repository.PrescriptionRepository;
import com.example.hospital_wed2.repository.UserRepository;
import com.example.hospital_wed2.service.doctor.DoctorMedicalRecordService;
import com.example.hospital_wed2.service.doctor.DoctorProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/doctor/medical-records")
@RequiredArgsConstructor
public class DoctorMedicalRecordsController {

    private final DoctorMedicalRecordService doctorMedicalRecordService;
    private final DoctorProfileService doctorProfileService;
    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @GetMapping
    public String listMedicalRecords(
            @RequestParam(required = false) String q,
            Model model,
            Authentication auth,
            HttpServletRequest request) {

        String email = auth.getName();

        // Lấy danh sách bệnh án
        List<MedicalRecord> records = doctorMedicalRecordService.getMyMedicalRecords(email);

        // Lọc theo từ khóa tìm kiếm nếu có
        if (q != null && !q.isBlank()) {
            String keyword = q.toLowerCase();
            records = records.stream()
                    .filter(r -> {
                        String patientName = (r.getAppointment() != null
                                && r.getAppointment().getPatient() != null
                                && r.getAppointment().getPatient().getProfile() != null)
                                ? r.getAppointment().getPatient().getProfile().getFullName()
                                : "";
                        String diagnosis = r.getDiagnosis() != null ? r.getDiagnosis() : "";
                        return patientName.toLowerCase().contains(keyword)
                                || diagnosis.toLowerCase().contains(keyword);
                    })
                    .toList();
        }

        // Tính thống kê đơn thuốc theo bác sĩ
        Doctor doctor = getDoctor(email);
        long pendingCount = prescriptionRepository.countByDoctorAndStatus(doctor, PrescriptionStatus.PENDING);
        long dispensedCount = prescriptionRepository.countByDoctorAndStatus(doctor, PrescriptionStatus.DISPENSED);

        model.addAttribute("profile", doctorProfileService.getDoctorProfile(email));
        model.addAttribute("medicalRecords", records);  // template dùng ${medicalRecords}
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("dispensedCount", dispensedCount);
        model.addAttribute("currentUri", request.getRequestURI());

        return "doctor/medical-records";
    }

    private Doctor getDoctor(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + email));
        return doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ: " + email));
    }
}