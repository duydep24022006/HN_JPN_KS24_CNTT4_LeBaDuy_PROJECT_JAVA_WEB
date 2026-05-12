package com.example.hospital_wed2.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStats {

    private long totalUsers;
    private long totalDoctors;
    private long todayAppointments;
    private long pendingPrescriptions;
    private long totalAppointments;
    private long activeUsers;

    // Thêm nếu cần
    private long totalPatients;
    private long totalSpecialties;
}