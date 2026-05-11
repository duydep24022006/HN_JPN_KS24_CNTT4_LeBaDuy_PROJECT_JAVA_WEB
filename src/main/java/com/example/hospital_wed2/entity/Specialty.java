package com.example.hospital_wed2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "specialties",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_specialties_name",
                        columnNames = "name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Specialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true
    )
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(
            mappedBy = "specialty",
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Doctor> doctors = new ArrayList<>();
}