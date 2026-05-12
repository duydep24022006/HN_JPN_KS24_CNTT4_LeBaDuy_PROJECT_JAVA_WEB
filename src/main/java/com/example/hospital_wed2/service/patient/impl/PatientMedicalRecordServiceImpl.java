package com.example.hospital_wed2.service.patient.impl;

import com.example.hospital_wed2.entity.MedicalRecord;
import com.example.hospital_wed2.entity.User;
import com.example.hospital_wed2.repository.MedicalRecordRepository;
import com.example.hospital_wed2.repository.UserRepository;
import com.example.hospital_wed2.service.patient.PatientMedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientMedicalRecordServiceImpl implements PatientMedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;

    @Override
    public List<MedicalRecord> getMyMedicalRecords(String email) {
        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bệnh nhân"));

        return medicalRecordRepository.findByPatientWithDetails(patient);
    }
}