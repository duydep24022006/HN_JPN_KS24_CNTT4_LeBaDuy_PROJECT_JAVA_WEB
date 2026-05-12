package com.example.hospital_wed2.service.admin;

import com.example.hospital_wed2.dto.admin.AdminDashboardStats;
import com.example.hospital_wed2.entity.Appointment;
import com.example.hospital_wed2.entity.Prescription;
import com.example.hospital_wed2.entity.User;

import java.util.List;

public interface AdminDashboardService {

    AdminDashboardStats getDashboardStats();

    List<Appointment> getRecentAppointments();

    List<Prescription> getRecentPrescriptions();

    List<User> getRecentUsers();

    List<Object[]> getTopDoctors();

    List<Object[]> getTopMedicines();

    List<Object[]> getMonthlyAppointments();
}