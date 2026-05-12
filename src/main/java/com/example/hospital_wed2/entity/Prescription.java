package com.example.hospital_wed2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "prescriptions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_prescriptions_medical_record", columnNames = "medical_record_id")
        },
        indexes = {
                @Index(name = "idx_prescriptions_status", columnList = "status")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Setter
@Getter
@Builder
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name="medical_record_id", nullable=false)
    private MedicalRecord medicalRecord;

    @Enumerated(EnumType.STRING)
    private PrescriptionStatus status;

    private String notes;
    @Column(name = "dispensed_at")
    private LocalDateTime dispensedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy="prescription", cascade=CascadeType.ALL)
    private List<PrescriptionDetail> details;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MedicalRecord getMedicalRecord() {
        return medicalRecord;
    }

    public void setMedicalRecord(MedicalRecord medicalRecord) {
        this.medicalRecord = medicalRecord;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }

    public String getNote() {
        return notes;
    }

    public void setNote(String note) {
        this.notes = note;
    }

    public LocalDateTime getDispensedAt() {
        return dispensedAt;
    }

    public void setDispensedAt(LocalDateTime dispensedAt) {
        this.dispensedAt = dispensedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<PrescriptionDetail> getDetails() {
        return details;
    }

    public void setDetails(List<PrescriptionDetail> details) {
        this.details = details;
    }
}
