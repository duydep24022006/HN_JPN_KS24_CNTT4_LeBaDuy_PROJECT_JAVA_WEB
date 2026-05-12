package com.example.hospital_wed2.service.patient.impl;

import com.example.hospital_wed2.dto.patient.PatientDashboardData;
import com.example.hospital_wed2.entity.AppointmentStatus;
import com.example.hospital_wed2.entity.User;
import com.example.hospital_wed2.repository.AppointmentRepository;
import com.example.hospital_wed2.repository.MedicalRecordRepository;
import com.example.hospital_wed2.repository.UserRepository;
import com.example.hospital_wed2.service.patient.PatientDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientDashboardServiceImpl implements PatientDashboardService {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    @Override
    public PatientDashboardData getDashboardData(String email) {
        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bệnh nhân"));

        long totalAppointments = appointmentRepository.countByPatient(patient);
        long confirmed = appointmentRepository.countByPatientAndStatus(patient, AppointmentStatus.CONFIRMED);
        long pending = appointmentRepository.countByPatientAndStatus(patient, AppointmentStatus.PENDING);
        long totalMedicalRecords = medicalRecordRepository.countByPatient(patient);

        long upcoming = appointmentRepository.countUpcomingAppointments(
                patient, LocalDate.now(), AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);

        return PatientDashboardData.builder()
                .totalAppointments(totalAppointments)
                .confirmedAppointments(confirmed)
                .pendingAppointments(pending)
                .totalMedicalRecords(totalMedicalRecords)
                .upcomingAppointmentsCount(upcoming)
                .patientName(patient.getProfile() != null ?
                        patient.getProfile().getFullName() : patient.getUsername())
                .build();
    }
}