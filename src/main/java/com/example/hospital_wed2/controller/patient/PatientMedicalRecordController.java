package com.example.hospital_wed2.controller.patient;

import com.example.hospital_wed2.entity.User;
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
@RequestMapping("/patient/medical-records")
@RequiredArgsConstructor
public class PatientMedicalRecordController {

    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;
    private final ProfileService profileService;

    @GetMapping
    public String listRecords(Model model, Authentication auth, HttpServletRequest request) {
        User patient = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var records = medicalRecordRepository.findByPatientWithDetails(patient);
        model.addAttribute("profile", profileService.getMyProfile(auth.getName()));
        model.addAttribute("records", records);
        model.addAttribute("totalRecords", records.size());
        model.addAttribute("currentUri", request.getRequestURI());
        return "patient/medical-records";
    }
}
