package com.example.hospital_wed2.service.patient.impl;

import com.example.hospital_wed2.entity.Appointment;
import com.example.hospital_wed2.entity.AppointmentStatus;
import com.example.hospital_wed2.entity.Doctor;
import com.example.hospital_wed2.entity.User;
import com.example.hospital_wed2.exception.ResourceNotFoundException;
import com.example.hospital_wed2.exception.UnauthorizedException;
import com.example.hospital_wed2.repository.AppointmentRepository;
import com.example.hospital_wed2.repository.DoctorRepository;
import com.example.hospital_wed2.repository.UserRepository;
import com.example.hospital_wed2.service.patient.PatientAppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientAppointmentServiceImpl implements PatientAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional
    public List<Appointment> getMyAppointments(String username) {
        User patient = getPatientByEmail(username);

        List<Appointment> appointments =
                appointmentRepository.findByPatientWithDetails(patient);

        appointments.forEach(this::updateExpiredAppointment);

        return appointments;
    }

    @Override
    @Transactional
    public List<Appointment> getMyAppointmentsByStatus(
            String username,
            AppointmentStatus status
    ) {
        User patient = getPatientByEmail(username);

        List<Appointment> appointments =
                appointmentRepository.findByPatientAndStatus(patient, status);

        appointments.forEach(this::updateExpiredAppointment);

        return appointments;
    }

    @Override
    @Transactional
    public List<Appointment> getTop5AppointmentsByStatus(
            String username,
            AppointmentStatus status
    ) {
        User patient = getPatientByEmail(username);

        List<Appointment> appointments =
                appointmentRepository.findTopAppointmentsByPatientAndStatus(
                        patient.getId(),
                        status.name()
                );

        appointments.forEach(this::updateExpiredAppointment);

        return appointments;
    }

    @Override
    @Transactional
    public Appointment getAppointment(String username, Long appointmentId) {
        User patient = getPatientByEmail(username);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy lịch hẹn"));

        if (appointment.getPatient() == null ||
                !appointment.getPatient().getId().equals(patient.getId())) {
            throw new UnauthorizedException("Bạn không có quyền xem lịch hẹn này");
        }

        updateExpiredAppointment(appointment);

        return appointment;
    }

    @Override
    @Transactional
    public void cancelAppointment(String username, Long appointmentId) {
        Appointment appointment = getAppointment(username, appointmentId);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Lịch hẹn này đã được hủy trước đó");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Không thể hủy lịch hẹn đã khám xong");
        }

        LocalDateTime appointmentDateTime = LocalDateTime.of(
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
        );

        if (LocalDateTime.now().isAfter(appointmentDateTime)) {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepository.save(appointment);
            throw new IllegalStateException("Lịch hẹn đã quá hạn và được chuyển sang trạng thái đã hủy");
        }

        LocalDateTime cancelDeadline = appointmentDateTime.minusHours(24);

        if (LocalDateTime.now().isAfter(cancelDeadline)) {
            throw new IllegalStateException("Chỉ có thể hủy lịch hẹn trước thời gian khám ít nhất 24 giờ");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment bookAppointment(
            String email,
            Long doctorId,
            LocalDate date,
            LocalTime time,
            String reason
    ) {
        User patient = getPatientByEmail(email);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy bác sĩ"));

        if (date.isBefore(LocalDate.now())) {
            throw new IllegalStateException("Không thể đặt lịch trong quá khứ");
        }

        if (date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            throw new IllegalStateException("Không thể đặt lịch trong thời gian đã qua");
        }

        if (isSlotTaken(doctorId, date, time)) {
            throw new IllegalStateException("Khung giờ này đã có người đặt");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(date)
                .appointmentTime(time)
                .reason(reason)
                .status(AppointmentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return appointmentRepository.save(appointment);
    }

    @Override
    public List<LocalTime> getBookedSlots(Long doctorId, LocalDate date) {
        return appointmentRepository.findBookedSlots(doctorId, date);
    }

    @Override
    public boolean isSlotTaken(Long doctorId, LocalDate date, LocalTime time) {
        boolean pendingTaken =
                appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
                        doctorId,
                        date,
                        time,
                        AppointmentStatus.PENDING
                );

        boolean confirmedTaken =
                appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
                        doctorId,
                        date,
                        time,
                        AppointmentStatus.CONFIRMED
                );

        return pendingTaken || confirmedTaken;
    }

    private void updateExpiredAppointment(Appointment appointment) {
        if (appointment == null) {
            return;
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED ||
                appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return;
        }

        LocalDateTime appointmentDateTime = LocalDateTime.of(
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
        );

        if (LocalDateTime.now().isAfter(appointmentDateTime)) {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepository.save(appointment);
        }
    }

    private User getPatientByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy bệnh nhân với email: " + email));
    }
}