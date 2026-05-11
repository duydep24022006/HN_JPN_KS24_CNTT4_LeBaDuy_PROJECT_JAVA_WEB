package com.example.hospital_wed2.service.doctor;

import com.example.hospital_wed2.dto.doctor.DoctorStatsDto;
import com.example.hospital_wed2.dto.doctor.ExamineRequest;
import com.example.hospital_wed2.entity.Appointment;
import com.example.hospital_wed2.entity.MedicalRecord;
import com.example.hospital_wed2.entity.Medicine;
import com.example.hospital_wed2.entity.Prescription;

import java.util.List;

public interface DoctorService {

    /** Lấy thống kê dashboard theo username bác sĩ */
    DoctorStatsDto getStats(String username);

    /** Lấy danh sách lịch hẹn của bác sĩ, có thể lọc theo status */
    List<Appointment> getAppointments(String username, String status);

    /** Lấy chi tiết một lịch hẹn (chỉ của bác sĩ đó) */
    Appointment getAppointment(String username, Long appointmentId);

    /** Xác nhận lịch hẹn PENDING → CONFIRMED */
    void confirmAppointment(String username, Long appointmentId);

    /** Hủy lịch hẹn PENDING/CONFIRMED → CANCELLED */
    void cancelAppointment(String username, Long appointmentId);

    /** Hoàn tất khám: tạo bệnh án + đơn thuốc, chuyển CONFIRMED → COMPLETED */
    void examine(String username, Long appointmentId, ExamineRequest request);

    /** Lấy bệnh án của lịch hẹn (sau khi COMPLETED) */
    MedicalRecord getMedicalRecord(Long appointmentId);

    /** Lấy đơn thuốc từ bệnh án */
    Prescription getPrescription(Long appointmentId);

    /** Tất cả bệnh án của bác sĩ (cho trang medical-records) */
    List<MedicalRecord> getMedicalRecords(String username);

    /** Tất cả thuốc đang hoạt động (cho form kê đơn) */
    List<Medicine> getActiveMedicines();

    /** Lịch khám hôm nay của bác sĩ */
    List<Appointment> getTodaySchedule(String username);

    /** 5 lịch hẹn gần nhất */
    List<Appointment> getRecentAppointments(String username);

    /** Đếm số trạng thái lịch hẹn (cho filter tabs) */
    long countByStatus(String username, String status);
}