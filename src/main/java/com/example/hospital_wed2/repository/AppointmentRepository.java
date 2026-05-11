package com.example.hospital_wed2.repository;

import com.example.hospital_wed2.entity.Appointment;
import com.example.hospital_wed2.entity.AppointmentStatus;
import com.example.hospital_wed2.entity.Doctor;
import com.example.hospital_wed2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // ===== PATIENT =====

    @Query("SELECT a FROM Appointment a JOIN FETCH a.doctor d JOIN FETCH d.user du LEFT JOIN FETCH du.profile JOIN FETCH d.specialty WHERE a.patient = :patient ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<Appointment> findByPatientWithDetails(@Param("patient") User patient);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.doctor d JOIN FETCH d.user du LEFT JOIN FETCH du.profile JOIN FETCH d.specialty WHERE a.patient = :patient AND a.status = :status ORDER BY a.appointmentDate ASC")
    List<Appointment> findByPatientAndStatus(@Param("patient") User patient, @Param("status") AppointmentStatus status);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.doctor d JOIN FETCH d.user du LEFT JOIN FETCH du.profile JOIN FETCH d.specialty WHERE a.patient = :patient AND a.status = :status ORDER BY a.appointmentDate DESC")
    List<Appointment> findByPatientAndStatusDesc(@Param("patient") User patient, @Param("status") AppointmentStatus status);

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
            Long doctorId,
            java.time.LocalDate appointmentDate,
            java.time.LocalTime appointmentTime,
            AppointmentStatus status
    );

    // ===== DOCTOR =====

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p LEFT JOIN FETCH p.profile pr WHERE a.doctor = :doctor ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<Appointment> findByDoctorOrderByDateDesc(@Param("doctor") Doctor doctor);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p LEFT JOIN FETCH p.profile pr WHERE a.doctor = :doctor AND a.status = :status ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<Appointment> findByDoctorAndStatus(@Param("doctor") Doctor doctor, @Param("status") AppointmentStatus status);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p LEFT JOIN FETCH p.profile pr WHERE a.doctor = :doctor AND a.appointmentDate = :date ORDER BY a.appointmentTime ASC")
    List<Appointment> findByDoctorAndDate(@Param("doctor") Doctor doctor, @Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p LEFT JOIN FETCH p.profile pr WHERE a.doctor = :doctor ORDER BY a.createdAt DESC")
    List<Appointment> findByDoctorOrderByCreatedAtDesc(@Param("doctor") Doctor doctor);

    @Query("SELECT COUNT(DISTINCT a.patient.id) FROM Appointment a WHERE a.doctor = :doctor")
    long countDistinctPatientsByDoctor(@Param("doctor") Doctor doctor);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor = :doctor AND a.status = :status")
    long countByDoctorAndStatus(@Param("doctor") Doctor doctor, @Param("status") AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor = :doctor AND a.appointmentDate = :date AND a.status = :status")
    long countByDoctorAndDateAndStatus(@Param("doctor") Doctor doctor, @Param("date") LocalDate date, @Param("status") AppointmentStatus status);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p LEFT JOIN FETCH p.profile pr WHERE a.id = :id AND a.doctor = :doctor")
    Optional<Appointment> findByIdAndDoctor(@Param("id") Long id, @Param("doctor") Doctor doctor);

    // BUG-08: Queries cho dashboard
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate = :date")
    long countByDate(@Param("date") java.time.LocalDate date);

    // BUG-10: Query booked slots không dùng findAll()
    @Query("SELECT a.appointmentTime FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.status IN (com.example.hospital_wed2.entity.AppointmentStatus.PENDING, com.example.hospital_wed2.entity.AppointmentStatus.CONFIRMED)")
    List<java.time.LocalTime> findBookedSlots(@Param("doctorId") Long doctorId, @Param("date") java.time.LocalDate date);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p LEFT JOIN FETCH p.profile ORDER BY a.createdAt DESC LIMIT 5")
    List<Appointment> findTop5ByOrderByCreatedAtDesc();
}