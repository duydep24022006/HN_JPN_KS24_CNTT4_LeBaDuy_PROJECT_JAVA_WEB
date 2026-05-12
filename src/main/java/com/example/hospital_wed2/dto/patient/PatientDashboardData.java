package com.example.hospital_wed2.dto.patient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDashboardData {

    private long totalAppointments;
    private long confirmedAppointments;
    private long pendingAppointments;
    private long totalMedicalRecords;
    private long upcomingAppointmentsCount;

    // Thêm thông tin hữu ích cho dashboard
    private String patientName;
}