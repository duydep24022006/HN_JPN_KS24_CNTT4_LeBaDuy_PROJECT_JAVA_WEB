package com.example.hospital_wed2.service.doctor.impl;

import com.example.hospital_wed2.dto.doctor.ExamineRequest;
import com.example.hospital_wed2.entity.*;
import com.example.hospital_wed2.exception.ResourceNotFoundException;
import com.example.hospital_wed2.repository.*;
import com.example.hospital_wed2.service.doctor.DoctorAppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorAppointmentServiceImpl implements DoctorAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicineRepository medicineRepository;
    private final PrescriptionDetailRepository prescriptionDetailRepository;

    @Override
    public List<Appointment> getMyAppointments(String email, String status) {
        Doctor doctor = getDoctorByEmail(email);

        if (status == null || status.isBlank()) {
            return appointmentRepository.findByDoctorOrderByDateDesc(doctor);
        }

        AppointmentStatus statusEnum = AppointmentStatus.valueOf(status.toUpperCase());

        return appointmentRepository.findByDoctorAndStatus(doctor, statusEnum);
    }

    @Override
    public Appointment getAppointment(String email, Long id) {
        Doctor doctor = getDoctorByEmail(email);

        return appointmentRepository.findByIdAndDoctor(id, doctor)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn"));
    }

    @Override
    @Transactional
    public void confirmAppointment(String email, Long id) {
        Appointment appointment = getAppointment(email, id);

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể xác nhận lịch hẹn đang chờ");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public void cancelAppointment(String email, Long id) {
        Appointment appointment = getAppointment(email, id);

        if (appointment.getStatus() == AppointmentStatus.COMPLETED ||
                appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Không thể hủy lịch hẹn này");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public void examine(String email, Long appointmentId, ExamineRequest request) {
        Appointment appointment = getAppointment(email, appointmentId);

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Lịch hẹn này đã được khám và lưu bệnh án trước đó");
        }

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ có thể khám lịch hẹn đã được xác nhận");
        }

        if (medicalRecordRepository.existsByAppointmentId(appointmentId)) {
            throw new IllegalStateException("Lịch hẹn này đã có bệnh án, không thể tạo thêm");
        }

        MedicalRecord medicalRecord = MedicalRecord.builder()
                .appointment(appointment)
                .symptoms(request.getSymptoms())
                .diagnosis(request.getDiagnosis())
                .notes(request.getDoctorNote())
                .createdAt(LocalDateTime.now())
                .build();

        MedicalRecord savedMedicalRecord = medicalRecordRepository.save(medicalRecord);

        if (request.getMedicineIds() != null && !request.getMedicineIds().isEmpty()) {
            createPrescription(savedMedicalRecord, request);
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
    }

    private void createPrescription(MedicalRecord medicalRecord, ExamineRequest request) {
        Prescription prescription = Prescription.builder()
                .medicalRecord(medicalRecord)
                .status(PrescriptionStatus.PENDING)
                .notes("Đơn thuốc từ buổi khám")
                .build();

        Prescription savedPrescription =
                prescriptionRepository.save(prescription);

        for (int i = 0; i < request.getMedicineIds().size(); i++) {
            Long medicineId = request.getMedicineIds().get(i);

            Medicine medicine = medicineRepository.findById(medicineId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Không tìm thấy thuốc ID: " + medicineId));

            Integer quantity = 1;
            if (request.getQuantities() != null
                    && i < request.getQuantities().size()
                    && request.getQuantities().get(i) != null) {
                quantity = request.getQuantities().get(i);
            }

            String dosage = "Theo chỉ định";
            if (request.getDosages() != null
                    && i < request.getDosages().size()
                    && request.getDosages().get(i) != null
                    && !request.getDosages().get(i).isBlank()) {
                dosage = request.getDosages().get(i);
            }

            PrescriptionDetail detail = PrescriptionDetail.builder()
                    .prescription(savedPrescription)
                    .medicine(medicine)
                    .quantity(quantity)
                    .dosage(dosage)
                    .instruction(dosage)
                    .build();

            prescriptionDetailRepository.save(detail);
        }
    }

    private Doctor getDoctorByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy người dùng"));

        return doctorRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy thông tin bác sĩ"));
    }
}