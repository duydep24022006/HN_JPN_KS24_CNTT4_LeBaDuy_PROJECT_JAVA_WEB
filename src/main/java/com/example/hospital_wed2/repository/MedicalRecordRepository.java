package com.example.hospital_wed2.repository;

import com.example.hospital_wed2.entity.Doctor;
import com.example.hospital_wed2.entity.MedicalRecord;
import com.example.hospital_wed2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    // Lấy danh sách bệnh án của bệnh nhân kèm đầy đủ thông tin liên quan
    // Join doctor, user, profile, specialty để tránh Lazy Loading
    @Query("SELECT mr FROM MedicalRecord mr JOIN FETCH mr.appointment a JOIN FETCH a.doctor d JOIN FETCH d.user du LEFT JOIN FETCH du.profile JOIN FETCH d.specialty WHERE a.patient = :patient ORDER BY mr.createdAt DESC")
    List<MedicalRecord> findByPatientWithDetails(@Param("patient") User patient);

    // Lấy toàn bộ bệnh án của bác sĩ theo danh sách bệnh nhân đã khám
    // Join profile bệnh nhân để hiển thị thông tin nhanh hơn
    @Query("SELECT mr FROM MedicalRecord mr JOIN FETCH mr.appointment a JOIN FETCH a.patient p LEFT JOIN FETCH p.profile WHERE a.doctor = :doctor ORDER BY mr.createdAt DESC")
    List<MedicalRecord> findByDoctor(@Param("doctor") Doctor doctor);

    // Tìm bệnh án theo appointmentId
    // Dùng để kiểm tra lịch hẹn đã có bệnh án chưa
    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);

    @Query("SELECT COUNT(mr) FROM MedicalRecord mr JOIN mr.appointment a WHERE a.patient = :patient")
    long countByPatient(@Param("patient") User patient);



    boolean existsByAppointmentId(Long appointmentId);
}