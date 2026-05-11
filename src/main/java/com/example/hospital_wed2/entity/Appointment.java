package com.example.hospital_wed2.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_appointment_slot",
               columnNames = {"doctor_id", "appointment_date", "appointment_time"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @Column(name = "appointment_time")
    private LocalTime appointmentTime;

    private String reason;

    private String note;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}