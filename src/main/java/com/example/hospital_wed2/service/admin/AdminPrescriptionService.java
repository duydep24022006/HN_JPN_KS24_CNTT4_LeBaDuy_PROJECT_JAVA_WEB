package com.example.hospital_wed2.service.admin;

import com.example.hospital_wed2.entity.Prescription;
import com.example.hospital_wed2.entity.PrescriptionStatus;

import java.util.List;

public interface AdminPrescriptionService {

    List<Prescription> getAllPrescriptions();

    List<Prescription> getByStatus(PrescriptionStatus status);

    List<Prescription> search(PrescriptionStatus status, String keyword);

    Prescription getById(Long id);

    void updateStatus(Long id, PrescriptionStatus status, String note);
}
