package com.example.hospital_wed2.repository;

import com.example.hospital_wed2.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpecialtyRepository extends JpaRepository<Specialty,Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<Specialty> findByNameContainingIgnoreCase(String name);

    @Query("SELECT s FROM Specialty s LEFT JOIN FETCH s.doctors d LEFT JOIN FETCH d.user LEFT JOIN FETCH d.user.profile ORDER BY s.name")
    List<Specialty> findAllWithDoctors();
}
