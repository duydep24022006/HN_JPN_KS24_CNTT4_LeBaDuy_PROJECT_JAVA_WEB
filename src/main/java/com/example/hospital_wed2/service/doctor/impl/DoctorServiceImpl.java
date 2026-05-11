package com.example.hospital_wed2.service.doctor.impl;

import com.example.hospital_wed2.dto.doctor.DoctorStatsDto;
import com.example.hospital_wed2.dto.doctor.ExamineRequest;
import com.example.hospital_wed2.entity.*;
import com.example.hospital_wed2.repository.*;
import com.example.hospital_wed2.service.doctor.DoctorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicineRepository medicineRepository;

    public DoctorServiceImpl(UserRepository userRepository,
                             DoctorRepository doctorRepository,
                             AppointmentRepository appointmentRepository,
                             MedicalRecordRepository medicalRecordRepository,
                             PrescriptionRepository prescriptionRepository,
                             MedicineRepository medicineRepository) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.medicineRepository = medicineRepository;
    }

    // ===== STATS =====

    @Override
    @Transactional(readOnly = true)
    public DoctorStatsDto getStats(String username) {
        Doctor doctor = findDoctorByUsername(username);
        LocalDate today = LocalDate.now();

        DoctorStatsDto stats = new DoctorStatsDto();
        stats.setTodayAppointments(
                appointmentRepository.findByDoctorAndDate(doctor, today).size()
        );
        stats.setPendingCount(
                appointmentRepository.countByDoctorAndStatus(doctor, AppointmentStatus.PENDING)
        );
        stats.setConfirmedCount(
                appointmentRepository.countByDoctorAndStatus(doctor, AppointmentStatus.CONFIRMED)
        );
        stats.setCompletedToday(
                appointmentRepository.countByDoctorAndDateAndStatus(doctor, today, AppointmentStatus.COMPLETED)
        );
        stats.setTotalPatients(
                appointmentRepository.countDistinctPatientsByDoctor(doctor)
        );
        stats.setTotalCompleted(
                appointmentRepository.countByDoctorAndStatus(doctor, AppointmentStatus.COMPLETED)
        );
        stats.setTotalCancelled(
                appointmentRepository.countByDoctorAndStatus(doctor, AppointmentStatus.CANCELLED)
        );
        return stats;
    }

    // ===== APPOINTMENTS =====

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointments(String username, String status) {
        Doctor doctor = findDoctorByUsername(username);
        if (status == null || status.isBlank()) {
            return appointmentRepository.findByDoctorOrderByDateDesc(doctor);
        }
        AppointmentStatus statusEnum = AppointmentStatus.valueOf(status.toUpperCase());
        return appointmentRepository.findByDoctorAndStatus(doctor, statusEnum);
    }

    @Override
    @Transactional(readOnly = true)
    public Appointment getAppointment(String username, Long appointmentId) {
        Doctor doctor = findDoctorByUsername(username);
        return appointmentRepository.findByIdAndDoctor(appointmentId, doctor)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));
    }

    @Override
    @Transactional
    public void confirmAppointment(String username, Long appointmentId) {
        Appointment appointment = getAppointment(username, appointmentId);
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể xác nhận lịch hẹn đang chờ");
        }
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public void cancelAppointment(String username, Long appointmentId) {
        Appointment appointment = getAppointment(username, appointmentId);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Không thể hủy lịch hẹn này");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public void examine(String username, Long appointmentId, ExamineRequest request) {
        Appointment appointment = getAppointment(username, appointmentId);
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ có thể khám lịch hẹn đã xác nhận");
        }

        // Validate medicine/quantity lists are same length
        List<Long> medicineIds = request.getMedicineIds();
        List<Integer> quantities = request.getQuantities();
        List<String> dosages = request.getDosages();

        if (medicineIds == null || medicineIds.isEmpty()) {
            throw new IllegalArgumentException("Phải kê ít nhất một loại thuốc");
        }
        if (quantities == null || quantities.size() != medicineIds.size()) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }

        // 1. Create MedicalRecord
        MedicalRecord record = MedicalRecord.builder()
                .appointment(appointment)
                .symptoms(request.getSymptoms())
                .diagnosis(request.getDiagnosis())
                .notes(request.getDoctorNote())
                .build();
        medicalRecordRepository.save(record);

        // 2. Create Prescription
        Prescription prescription = Prescription.builder()
                .medicalRecord(record)
                .status(PrescriptionStatus.PENDING)
                .build();
        prescriptionRepository.save(prescription);

        // 3. Create PrescriptionDetails
        List<PrescriptionDetail> details = new ArrayList<>();
        for (int i = 0; i < medicineIds.size(); i++) {
            Long medId = medicineIds.get(i);
            if (medId == null) continue;

            Medicine medicine = medicineRepository.findById(medId)
                    .orElseThrow(() -> new IllegalArgumentException("Thuốc không tồn tại"));

            Integer qty = quantities.get(i);
            if (qty == null || qty < 1) {
                throw new IllegalArgumentException("Số lượng thuốc phải ít nhất là 1");
            }

            String dosage = (dosages != null && i < dosages.size()) ? dosages.get(i) : null;

            PrescriptionDetail detail = PrescriptionDetail.builder()
                    .prescription(prescription)
                    .medicine(medicine)
                    .quantity(qty)
                    .dosage(dosage)
                    .build();
            details.add(detail);
        }
        prescription.setDetails(details);
        prescriptionRepository.save(prescription);

        // 4. Mark appointment COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
    }

    // ===== MEDICAL RECORDS =====

    @Override
    @Transactional(readOnly = true)
    public MedicalRecord getMedicalRecord(Long appointmentId) {
        return medicalRecordRepository.findByAppointmentId(appointmentId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Prescription getPrescription(Long appointmentId) {
        return prescriptionRepository.findByAppointmentId(appointmentId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecord> getMedicalRecords(String username) {
        Doctor doctor = findDoctorByUsername(username);
        return medicalRecordRepository.findByDoctor(doctor);
    }

    // ===== MEDICINES =====

    @Override
    @Transactional(readOnly = true)
    public List<Medicine> getActiveMedicines() {
        return medicineRepository.findByIsActive(true);
    }

    // ===== DASHBOARD =====

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getTodaySchedule(String username) {
        Doctor doctor = findDoctorByUsername(username);
        return appointmentRepository.findByDoctorAndDate(doctor, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getRecentAppointments(String username) {
        Doctor doctor = findDoctorByUsername(username);
        List<Appointment> all = appointmentRepository.findByDoctorOrderByCreatedAtDesc(doctor);
        return all.stream().limit(5).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(String username, String status) {
        Doctor doctor = findDoctorByUsername(username);
        AppointmentStatus statusEnum = AppointmentStatus.valueOf(status.toUpperCase());
        return appointmentRepository.countByDoctorAndStatus(doctor, statusEnum);
    }

    // ===== HELPER =====

    private Doctor findDoctorByUsername(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));
        return doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ"));
    }
}