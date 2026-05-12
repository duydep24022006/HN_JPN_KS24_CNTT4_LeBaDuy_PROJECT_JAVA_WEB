package com.example.hospital_wed2.service.doctor;

import com.example.hospital_wed2.dto.doctor.DoctorStatsDto;
import com.example.hospital_wed2.entity.Appointment;

import java.util.List;

public interface DoctorDashboardService {
    DoctorStatsDto getDashboardData(String email);
    /** Lịch hẹn của bác sĩ trong ngày hôm nay (PENDING + CONFIRMED) */
    List<Appointment> getTodaySchedule(String email);

    /** 5 lịch hẹn gần nhất (tất cả trạng thái) */
    List<Appointment> getRecentAppointments(String email);
}