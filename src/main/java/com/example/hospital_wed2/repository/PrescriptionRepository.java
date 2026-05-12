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

    // Override findAll để lấy toàn bộ đơn thuốc kèm dữ liệu liên quan
// EntityGraph giúp load sẵn medicalRecord, appointment, patient, doctor, thuốc...
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

    // Lấy danh sách đơn thuốc theo trạng thái
// Ví dụ: PENDING, COMPLETED, CANCELLED...
    @Query("SELECT p FROM Prescription p WHERE p.status = :status")
    List<Prescription> findByStatus(PrescriptionStatus status);

    // Query tìm đơn thuốc theo status nhưng cho phép status = null
// Nếu null sẽ lấy tất cả đơn thuốc
    @Query("SELECT p FROM Prescription p WHERE (:status IS NULL OR p.status = :status)")
    List<Prescription> findByStatusOptional(@Param("status") PrescriptionStatus status);

    // Tìm đơn thuốc theo appointmentId
// Join FETCH details và medicine để lấy luôn chi tiết thuốc
    @Query("SELECT p FROM Prescription p JOIN FETCH p.details d JOIN FETCH d.medicine WHERE p.medicalRecord.appointment.id = :appointmentId")
    Optional<Prescription> findByAppointmentId(@Param("appointmentId") Long appointmentId);

    // Đếm số lượng đơn thuốc theo trạng thái
    // Tối ưu hơn so với findAll rồi stream/filter bằng Java
    long countByStatus(PrescriptionStatus status);

    // Lấy danh sách đơn thuốc theo status kèm đầy đủ dữ liệu liên quan
    // Dùng EntityGraph để tránh lỗi N+1 query
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

    // Lấy 5 đơn thuốc mới nhất theo thời gian tạo
// Dùng cho dashboard hoặc thống kê gần đây
    @Query("SELECT p FROM Prescription p ORDER BY p.createdAt DESC LIMIT 5")
    List<Prescription> findTop5ByOrderByCreatedAtDesc();


    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.status = com.example.hospital_wed2.entity.PrescriptionStatus.PENDING")
    long countByStatusPending();

    // Đếm đơn thuốc theo bác sĩ và trạng thái (dùng cho trang medical-records của bác sĩ)
    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.medicalRecord.appointment.doctor = :doctor AND p.status = :status")
    long countByDoctorAndStatus(
            @Param("doctor") com.example.hospital_wed2.entity.Doctor doctor,
            @Param("status") PrescriptionStatus status);
}