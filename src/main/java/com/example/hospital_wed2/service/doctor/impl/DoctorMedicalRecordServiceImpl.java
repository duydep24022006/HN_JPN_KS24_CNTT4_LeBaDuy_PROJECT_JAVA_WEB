package com.example.hospital_wed2.service.doctor.impl;

import com.example.hospital_wed2.dto.doctor.ExamineRequest;
import com.example.hospital_wed2.entity.*;
import com.example.hospital_wed2.exception.ResourceNotFoundException;
import com.example.hospital_wed2.exception.UnauthorizedException;
import com.example.hospital_wed2.repository.*;
import com.example.hospital_wed2.service.doctor.DoctorMedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorMedicalRecordServiceImpl implements DoctorMedicalRecordService {

    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicineRepository medicineRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecord> getMyMedicalRecords(String email) {
        Doctor doctor = getDoctor(email);
        return medicalRecordRepository.findByDoctor(doctor);
    }

    @Override
    @Transactional
    public void examine(String email, Long appointmentId, ExamineRequest request) {
        Appointment appointment = getAppointmentForExamine(email, appointmentId);

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ có thể khám lịch hẹn đã được xác nhận");
        }

        // Tạo Hồ sơ bệnh án
        MedicalRecord medicalRecord = MedicalRecord.builder()
                .appointment(appointment)
                .symptoms(request.getSymptoms())
                .diagnosis(request.getDiagnosis())
                .notes(request.getDoctorNote())
                .createdAt(LocalDateTime.now())
                .build();

        medicalRecordRepository.save(medicalRecord);

        // Tạo Đơn thuốc và chi tiết nếu có kê thuốc
        if (request.getMedicineIds() != null && !request.getMedicineIds().isEmpty()) {
            createPrescriptionWithDetails(medicalRecord, request);
        }

        // Hoàn tất lịch hẹn
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
    }

    private Appointment getAppointmentForExamine(String email, Long appointmentId) {
        Doctor doctor = getDoctor(email);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: " + appointmentId));

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new UnauthorizedException("Bạn không có quyền khám lịch hẹn này");
        }

        return appointment;
    }

    /**
     * Tạo đơn thuốc kèm chi tiết thuốc
     */
    private void createPrescriptionWithDetails(MedicalRecord medicalRecord, ExamineRequest request) {
        // Tạo Prescription
        Prescription prescription = Prescription.builder()
                .medicalRecord(medicalRecord)
                .status(PrescriptionStatus.PENDING)
                .notes("Đơn thuốc từ buổi khám ngày " + LocalDateTime.now())
                .build();

        Prescription savedPrescription = prescriptionRepository.save(prescription);

        // Tạo PrescriptionDetail cho từng thuốc
        for (Long medicineId : request.getMedicineIds()) {
            Medicine medicine = medicineRepository.findById(medicineId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thuốc với ID: " + medicineId));

            PrescriptionDetail detail = PrescriptionDetail.builder()
                    .prescription(savedPrescription)
                    .medicine(medicine)
                    .quantity(1)                    // Mặc định 1, sau có thể mở rộng
                    .dosage("1 viên/lần")           // Mặc định, sau sẽ lấy từ form
                    .instruction("Uống sau ăn")    // Mặc định
                    .build();

            // Lưu chi tiết đơn thuốc
            // prescriptionDetailRepository.save(detail);   // Nếu bạn có repository riêng
            // Hoặc nếu PrescriptionDetail có relationship Cascade, bạn có thể add vào list của Prescription
        }
    }

    /**
     * Lấy thông tin bác sĩ từ email
     */
    private Doctor getDoctor(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + email));

        return doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin bác sĩ cho email: " + email));
    }
}