package com.example.hospital_wed2.service.patient;

import com.example.hospital_wed2.entity.MedicalRecord;
import java.util.List;

public interface PatientMedicalRecordService {
    List<MedicalRecord> getMyMedicalRecords(String email);
}