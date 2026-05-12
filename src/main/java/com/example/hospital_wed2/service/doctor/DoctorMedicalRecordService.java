package com.example.hospital_wed2.service.doctor;

import com.example.hospital_wed2.entity.MedicalRecord;
import com.example.hospital_wed2.dto.doctor.ExamineRequest;
import java.util.List;

public interface DoctorMedicalRecordService {
    List<MedicalRecord> getMyMedicalRecords(String email);
    void examine(String email, Long appointmentId, ExamineRequest request);
}