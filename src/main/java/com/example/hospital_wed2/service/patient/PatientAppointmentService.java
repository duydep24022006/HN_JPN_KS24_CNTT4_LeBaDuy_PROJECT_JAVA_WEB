package com.example.hospital_wed2.service.patient;

import com.example.hospital_wed2.entity.Appointment;
import com.example.hospital_wed2.entity.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface PatientAppointmentService {

    List<Appointment> getMyAppointments(String username);

    List<Appointment> getMyAppointmentsByStatus(String username, AppointmentStatus status);

    Appointment getAppointment(String username, Long appointmentId);

    void cancelAppointment(String username, Long appointmentId);

    Appointment bookAppointment(String email, Long doctorId, LocalDate date, LocalTime time, String reason);

    List<LocalTime> getBookedSlots(Long doctorId, LocalDate date);

    boolean isSlotTaken(Long doctorId, LocalDate date, LocalTime time);
    List<Appointment> getTop5AppointmentsByStatus(String username, AppointmentStatus status);
}