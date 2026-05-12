package com.example.hospital_wed2.service.patient;

import com.example.hospital_wed2.dto.patient.PatientDashboardData;

public interface PatientDashboardService {
    PatientDashboardData getDashboardData(String email);
}