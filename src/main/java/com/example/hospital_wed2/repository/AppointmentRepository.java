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

    /*
 Lấy toàn bộ lịch hẹn của bệnh nhân kèm doctor/user/profile/specialty
 JOIN FETCH giúp tránh lỗi lazy loading khi render Thymeleaf
*/
    @Query("""
        SELECT a
        FROM Appointment a
        JOIN FETCH a.doctor d
        JOIN FETCH d.user du
        LEFT JOIN FETCH du.profile
        JOIN FETCH d.specialty
        WHERE a.patient = :patient
        ORDER BY a.appointmentDate DESC, a.appointmentTime DESC
    """)
    List<Appointment> findByPatientWithDetails(
            @Param("patient") User patient
    );



    /*
     Lấy lịch hẹn theo bệnh nhân và trạng thái
     Sắp xếp ngày tăng dần để hiển thị lịch sắp tới
    */
    @Query("""
        SELECT a
        FROM Appointment a
        JOIN FETCH a.doctor d
        JOIN FETCH d.user du
        LEFT JOIN FETCH du.profile
        JOIN FETCH d.specialty
        WHERE a.patient = :patient
        AND a.status = :status
        ORDER BY a.appointmentDate ASC
    """)
    List<Appointment> findByPatientAndStatus(
            @Param("patient") User patient,
            @Param("status") AppointmentStatus status
    );



    /*
     Lấy lịch hẹn theo trạng thái và sắp xếp mới nhất trước
     Dùng cho lịch sử lịch hẹn
    */
    @Query("""
        SELECT a
        FROM Appointment a
        JOIN FETCH a.doctor d
        JOIN FETCH d.user du
        LEFT JOIN FETCH du.profile
        JOIN FETCH d.specialty
        WHERE a.patient = :patient
        AND a.status = :status
        ORDER BY a.appointmentDate DESC
    """)
    List<Appointment> findByPatientAndStatusDesc(
            @Param("patient") User patient,
            @Param("status") AppointmentStatus status
    );



    /*
     Kiểm tra bác sĩ có bị trùng lịch không
     True = đã có người đặt khung giờ đó
    */
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
            Long doctorId,
            java.time.LocalDate appointmentDate,
            java.time.LocalTime appointmentTime,
            AppointmentStatus status
    );



    // =====================================================
    // DOCTOR
    // =====================================================

    /*
     Lấy toàn bộ lịch khám của bác sĩ
     Sắp xếp theo ngày khám mới nhất
    */
    @Query("""
        SELECT a
        FROM Appointment a
        JOIN FETCH a.patient p
        LEFT JOIN FETCH p.profile pr
        WHERE a.doctor = :doctor
        ORDER BY a.appointmentDate DESC, a.appointmentTime DESC
    """)
    List<Appointment> findByDoctorOrderByDateDesc(
            @Param("doctor") Doctor doctor
    );



    /*
     Lấy lịch khám của bác sĩ theo trạng thái
     Ví dụ CONFIRMED hoặc PENDING
    */
    @Query("""
        SELECT a
        FROM Appointment a
        JOIN FETCH a.patient p
        LEFT JOIN FETCH p.profile pr
        WHERE a.doctor = :doctor
        AND a.status = :status
        ORDER BY a.appointmentDate DESC, a.appointmentTime DESC
    """)
    List<Appointment> findByDoctorAndStatus(
            @Param("doctor") Doctor doctor,
            @Param("status") AppointmentStatus status
    );



    /*
     Lấy lịch khám của bác sĩ trong 1 ngày cụ thể
     Dùng cho lịch làm việc trong ngày
    */
    @Query("""
        SELECT a
        FROM Appointment a
        JOIN FETCH a.patient p
        LEFT JOIN FETCH p.profile pr
        WHERE a.doctor = :doctor
        AND a.appointmentDate = :date
        ORDER BY a.appointmentTime ASC
    """)
    List<Appointment> findByDoctorAndDate(
            @Param("doctor") Doctor doctor,
            @Param("date") LocalDate date
    );



    /*
     Lấy lịch hẹn của bác sĩ theo thời gian tạo mới nhất
     Dùng cho dashboard hoặc thông báo
    */
    @Query("""
        SELECT a
        FROM Appointment a
        JOIN FETCH a.patient p
        LEFT JOIN FETCH p.profile pr
        WHERE a.doctor = :doctor
        ORDER BY a.createdAt DESC
    """)
    List<Appointment> findByDoctorOrderByCreatedAtDesc(
            @Param("doctor") Doctor doctor
    );



    /*
     Đếm số bệnh nhân khác nhau của bác sĩ
     DISTINCT tránh đếm trùng bệnh nhân
    */
    @Query("""
        SELECT COUNT(DISTINCT a.patient.id)
        FROM Appointment a
        WHERE a.doctor = :doctor
    """)
    long countDistinctPatientsByDoctor(
            @Param("doctor") Doctor doctor
    );



    /*
     Đếm số lịch hẹn theo trạng thái của bác sĩ
     Dùng cho thống kê dashboard
    */
    @Query("""
        SELECT COUNT(a)
        FROM Appointment a
        WHERE a.doctor = :doctor
        AND a.status = :status
    """)
    long countByDoctorAndStatus(
            @Param("doctor") Doctor doctor,
            @Param("status") AppointmentStatus status
    );



    /*
     Đếm số lịch khám trong ngày theo trạng thái
     Ví dụ số ca CONFIRMED hôm nay
    */
    @Query("""
        SELECT COUNT(a)
        FROM Appointment a
        WHERE a.doctor = :doctor
        AND a.appointmentDate = :date
        AND a.status = :status
    """)
    long countByDoctorAndDateAndStatus(
            @Param("doctor") Doctor doctor,
            @Param("date") LocalDate date,
            @Param("status") AppointmentStatus status
    );



    /*
     Tìm lịch hẹn theo id và bác sĩ
     Đảm bảo bác sĩ chỉ xem lịch của mình
    */
    @Query("""
        SELECT a
        FROM Appointment a
        JOIN FETCH a.patient p
        LEFT JOIN FETCH p.profile pr
        WHERE a.id = :id
        AND a.doctor = :doctor
    """)
    Optional<Appointment> findByIdAndDoctor(
            @Param("id") Long id,
            @Param("doctor") Doctor doctor
    );



    /*
     Đếm tổng lịch hẹn của toàn hệ thống theo ngày
     Dùng cho admin hoặc dashboard tổng
    */
    @Query("""
        SELECT COUNT(a)
        FROM Appointment a
        WHERE a.appointmentDate = :date
    """)
    long countByDate(
            @Param("date") java.time.LocalDate date
    );



    /*
     Lấy các khung giờ đã được đặt của bác sĩ
     Chỉ lấy PENDING và CONFIRMED
    */
    @Query("""
        SELECT a.appointmentTime
        FROM Appointment a
        WHERE a.doctor.id = :doctorId
        AND a.appointmentDate = :date
        AND a.status IN (
            com.example.hospital_wed2.entity.AppointmentStatus.PENDING,
            com.example.hospital_wed2.entity.AppointmentStatus.CONFIRMED
        )
    """)
    List<java.time.LocalTime> findBookedSlots(
            @Param("doctorId") Long doctorId,
            @Param("date") java.time.LocalDate date
    );



    /*
     Lấy 5 lịch hẹn mới nhất toàn hệ thống
     Dùng cho dashboard admin
    */
    @Query("""
        SELECT a
        FROM Appointment a
        JOIN FETCH a.patient p
        LEFT JOIN FETCH p.profile
        ORDER BY a.createdAt DESC
        LIMIT 5
    """)
    List<Appointment> findTop5ByOrderByCreatedAtDesc();
}