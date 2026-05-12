package com.example.hospital_wed2.service.doctor.impl;

import com.example.hospital_wed2.dto.doctor.DoctorStatsDto;
import com.example.hospital_wed2.entity.Appointment;
import com.example.hospital_wed2.entity.AppointmentStatus;
import com.example.hospital_wed2.entity.Doctor;
import com.example.hospital_wed2.entity.User;
import com.example.hospital_wed2.exception.ResourceNotFoundException;
import com.example.hospital_wed2.repository.AppointmentRepository;
import com.example.hospital_wed2.repository.DoctorRepository;
import com.example.hospital_wed2.repository.UserRepository;
import com.example.hospital_wed2.service.doctor.DoctorDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorDashboardServiceImpl implements DoctorDashboardService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public DoctorStatsDto getDashboardData(String email) {
        Doctor doctor = getDoctor(email);
        LocalDate today = LocalDate.now();

        DoctorStatsDto stats = new DoctorStatsDto();
        stats.setPendingCount(appointmentRepository.countByDoctorAndStatus(doctor, AppointmentStatus.PENDING));
        stats.setConfirmedCount(appointmentRepository.countByDoctorAndStatus(doctor, AppointmentStatus.CONFIRMED));
        stats.setCompletedToday(appointmentRepository.countByDoctorAndDateAndStatus(doctor, today, AppointmentStatus.COMPLETED));
        stats.setTotalPatients(appointmentRepository.countDistinctPatientsByDoctor(doctor));
        stats.setTotalCompleted(appointmentRepository.countByDoctorAndStatus(doctor, AppointmentStatus.COMPLETED));
        stats.setTotalCancelled(appointmentRepository.countByDoctorAndStatus(doctor, AppointmentStatus.CANCELLED));

        return stats;
    }

    @Override
    public List<Appointment> getTodaySchedule(String email) {

        Doctor doctor = getDoctor(email);

        List<Appointment> appointments =
                appointmentRepository.findByDoctorAndDate(
                        doctor,
                        LocalDate.now()
                );

        appointments.forEach(a -> {
            System.out.println(
                    "Appointment ID = " + a.getId()
                            + " | patient = " +
                            (a.getPatient() != null
                                    ? a.getPatient().getUsername()
                                    : "null")
            );
        });

        return appointments;
    }

    @Override
    public List<Appointment> getRecentAppointments(String email) {
        Doctor doctor = getDoctor(email);
        // Lấy 5 lịch hẹn mới nhất theo thời gian tạo
        List<Appointment> all = appointmentRepository.findByDoctorOrderByCreatedAtDesc(doctor);
        return all.size() > 5 ? all.subList(0, 5) : all;
    }

    private Doctor getDoctor(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + email));
        return doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin bác sĩ cho email: " + email));
    }
}