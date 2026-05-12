package com.example.hospital_wed2.service.doctor;

import com.example.hospital_wed2.dto.doctor.ExamineRequest;
import com.example.hospital_wed2.entity.Appointment;
import java.util.List;

public interface DoctorAppointmentService {
    List<Appointment> getMyAppointments(String email, String status);
    Appointment getAppointment(String email, Long id);
    void confirmAppointment(String email, Long id);
    void cancelAppointment(String email, Long id);
    void examine(String email, Long appointmentId, ExamineRequest request);
}