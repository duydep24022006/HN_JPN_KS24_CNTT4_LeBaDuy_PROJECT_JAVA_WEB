package com.example.hospital_wed2.dto.admin;

import com.example.hospital_wed2.entity.PrescriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDto {

    private Long id;
    private Long medicalRecordId;
    private String patientName;
    private String doctorName;
    private String diagnosis;
    private PrescriptionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime dispensedAt;
    private String note;

    // Số lượng thuốc trong đơn
    private int totalItems;
    private Double totalPrice;
}