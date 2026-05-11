package com.example.hospital_wed2.repository;

import com.example.hospital_wed2.entity.Prescription;
import com.example.hospital_wed2.entity.PrescriptionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription,Long> {

    @Override
    @EntityGraph(attributePaths = {
            "medicalRecord",
            "medicalRecord.appointment",
            "medicalRecord.appointment.patient",
            "medicalRecord.appointment.patient.profile",
            "medicalRecord.appointment.doctor",
            "medicalRecord.appointment.doctor.user",
            "medicalRecord.appointment.doctor.user.profile",
            "details",
            "details.medicine"
    })
    List<Prescription> findAll();

    List<Prescription> findByStatus(PrescriptionStatus status);

    @Query("SELECT p FROM Prescription p WHERE (:status IS NULL OR p.status = :status)")
    List<Prescription> findByStatusOptional(@Param("status") PrescriptionStatus status);

    @Query("SELECT p FROM Prescription p JOIN FETCH p.details d JOIN FETCH d.medicine WHERE p.medicalRecord.appointment.id = :appointmentId")
    Optional<Prescription> findByAppointmentId(@Param("appointmentId") Long appointmentId);

    // BUG-08: Đếm theo status (thay vì findAll() + stream)
    long countByStatus(PrescriptionStatus status);

    // BUG-17: findByStatus với EntityGraph để tránh N+1
    @EntityGraph(attributePaths = {
            "medicalRecord",
            "medicalRecord.appointment",
            "medicalRecord.appointment.patient",
            "medicalRecord.appointment.patient.profile",
            "medicalRecord.appointment.doctor",
            "medicalRecord.appointment.doctor.user",
            "medicalRecord.appointment.doctor.user.profile",
            "details",
            "details.medicine"
    })
    List<Prescription> findWithDetailsByStatus(PrescriptionStatus status);

    @Query("SELECT p FROM Prescription p ORDER BY p.createdAt DESC LIMIT 5")
    List<Prescription> findTop5ByOrderByCreatedAtDesc();
}