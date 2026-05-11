package com.example.hospital_wed2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "doctors",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_doctors_user_id",
                        columnNames = "user_id"
                ),
                @UniqueConstraint(
                        name = "uk_doctors_license_number",
                        columnNames = "license_number"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // RELATIONSHIPS
    // =====================================================

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "specialty_id",
            nullable = false
    )
    private Specialty specialty;

    // =====================================================
    // DOCTOR INFO
    // =====================================================

    @Column(
            name = "license_number",
            nullable = false,
            length = 50
    )
    private String licenseNumber;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            name = "consultation_fee",
            precision = 10,
            scale = 2
    )
    private BigDecimal consultationFee;
}