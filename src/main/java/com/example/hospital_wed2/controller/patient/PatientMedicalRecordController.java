package com.example.hospital_wed2.controller.patient;

import com.example.hospital_wed2.service.patient.PatientMedicalRecordService;
import com.example.hospital_wed2.service.patient.PatientProfileService;
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

    private final PatientMedicalRecordService patientMedicalRecordService;
    private final PatientProfileService patientProfileService;

    @GetMapping
    public String listRecords(Model model, Authentication auth, HttpServletRequest request) {
        String email = auth.getName();

        model.addAttribute("profile", patientProfileService.getMyProfile(email));
        model.addAttribute("records", patientMedicalRecordService.getMyMedicalRecords(email));
        model.addAttribute("currentUri", request.getRequestURI());

        return "patient/medical-records";
    }
}