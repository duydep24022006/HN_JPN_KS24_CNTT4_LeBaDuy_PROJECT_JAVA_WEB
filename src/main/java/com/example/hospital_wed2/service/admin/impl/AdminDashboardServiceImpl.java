package com.example.hospital_wed2.service.admin.impl;

import com.example.hospital_wed2.dto.admin.AdminDashboardStats;
import com.example.hospital_wed2.entity.Appointment;
import com.example.hospital_wed2.entity.Prescription;
import com.example.hospital_wed2.entity.User;
import com.example.hospital_wed2.repository.AppointmentRepository;
import com.example.hospital_wed2.repository.DoctorRepository;
import com.example.hospital_wed2.repository.PrescriptionDetailRepository;
import com.example.hospital_wed2.repository.PrescriptionRepository;
import com.example.hospital_wed2.repository.UserRepository;
import com.example.hospital_wed2.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDetailRepository prescriptionDetailRepository;

    @Override
    public AdminDashboardStats getDashboardStats() {
        LocalDate today = LocalDate.now();

        return AdminDashboardStats.builder()
                .totalUsers(userRepository.count())
                .totalDoctors(doctorRepository.count())
                .todayAppointments(appointmentRepository.countByDate(today))
                .pendingPrescriptions(prescriptionRepository.countByStatusPending())
                .totalAppointments(appointmentRepository.count())
                .activeUsers(userRepository.countByActiveTrue())
                .build();
    }

    @Override
    public List<Appointment> getRecentAppointments() {
        return appointmentRepository.findTop5ByOrderByCreatedAtDesc();
    }

    @Override
    public List<Prescription> getRecentPrescriptions() {
        return prescriptionRepository.findTop5ByOrderByCreatedAtDesc();
    }

    @Override
    public List<User> getRecentUsers() {
        return userRepository.findTop5ByOrderByCreatedAtDesc();
    }

    @Override
    public List<Object[]> getTopDoctors() {
        List<Object[]> result = appointmentRepository.findTopDoctorsByCompletedAppointments();
        return result.size() > 5 ? result.subList(0, 5) : result;
    }

    @Override
    public List<Object[]> getTopMedicines() {
        List<Object[]> result = prescriptionDetailRepository.findTopMedicines();
        return result.size() > 5 ? result.subList(0, 5) : result;
    }

    @Override
    public List<Object[]> getMonthlyAppointments() {
        return appointmentRepository.countAppointmentsByMonth();
    }
}