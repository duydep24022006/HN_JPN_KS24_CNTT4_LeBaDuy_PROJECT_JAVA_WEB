package com.example.hospital_wed2.service.admin.impl;

import com.example.hospital_wed2.entity.*;
import com.example.hospital_wed2.exception.ResourceNotFoundException;
import com.example.hospital_wed2.repository.MedicineRepository;
import com.example.hospital_wed2.repository.PrescriptionRepository;
import com.example.hospital_wed2.service.admin.AdminPrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPrescriptionServiceImpl implements AdminPrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicineRepository medicineRepository;

    // Giữ nguyên logic project cũ: chỉ PENDING mới được chuyển sang DISPENSED/CANCELLED.
    private static final Map<PrescriptionStatus, Set<PrescriptionStatus>> VALID_TRANSITIONS = Map.of(
            PrescriptionStatus.PENDING, Set.of(PrescriptionStatus.DISPENSED, PrescriptionStatus.CANCELLED),
            PrescriptionStatus.DISPENSED, Set.of(),
            PrescriptionStatus.CANCELLED, Set.of()
    );

    @Override
    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAll();
    }

    @Override
    public List<Prescription> getByStatus(PrescriptionStatus status) {
        return prescriptionRepository.findByStatus(status);
    }

    @Override
    public List<Prescription> search(PrescriptionStatus status, String keyword) {
        List<Prescription> prescriptions = status != null ? getByStatus(status) : getAllPrescriptions();

        if (keyword == null || keyword.isBlank()) {
            return prescriptions;
        }

        String kw = keyword.toLowerCase().trim();
        return prescriptions.stream()
                .filter(p -> {
                    try {
                        var patientProfile = p.getMedicalRecord().getAppointment().getPatient().getProfile();
                        var doctorProfile = p.getMedicalRecord().getAppointment().getDoctor().getUser().getProfile();
                        String patientName = patientProfile != null ? patientProfile.getFullName() : "";
                        String doctorName = doctorProfile != null ? doctorProfile.getFullName() : "";
                        return patientName.toLowerCase().contains(kw) || doctorName.toLowerCase().contains(kw);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .toList();
    }

    @Override
    public Prescription getById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuốc"));
    }

    @Override
    @Transactional
    public void updateStatus(Long id, PrescriptionStatus newStatus, String note) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuốc"));

        PrescriptionStatus currentStatus = prescription.getStatus();
        Set<PrescriptionStatus> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException("Không thể chuyển trạng thái từ " + currentStatus + " sang " + newStatus);
        }

        if (newStatus == PrescriptionStatus.DISPENSED) {
            List<PrescriptionDetail> details = prescription.getDetails();
            if (details != null) {
                for (PrescriptionDetail detail : details) {
                    Medicine medicine = detail.getMedicine();
                    int needed = detail.getQuantity() != null ? detail.getQuantity() : 0;
                    int currentStock = medicine.getStockQuantity() != null ? medicine.getStockQuantity() : 0;

                    if (currentStock < needed) {
                        throw new IllegalStateException(
                                "Không đủ tồn kho cho thuốc \"" + medicine.getName() +
                                        "\". Tồn kho hiện tại: " + currentStock + ", cần: " + needed);
                    }
                }

                for (PrescriptionDetail detail : details) {
                    Medicine medicine = detail.getMedicine();
                    int newStock = medicine.getStockQuantity() - detail.getQuantity();
                    medicine.setStockQuantity(newStock);
                    medicineRepository.save(medicine);
                }
            }
            prescription.setDispensedAt(LocalDateTime.now());
        }

        prescription.setStatus(newStatus);
        if (note != null && !note.trim().isEmpty()) {
            prescription.setNote(note);
        }
        prescriptionRepository.save(prescription);
    }
}
