package com.example.hospital_wed2.repository;

import com.example.hospital_wed2.entity.MedicalRecord;
import com.example.hospital_wed2.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine,Long> {
        List<Medicine> findByNameContainingIgnoreCase(String name);
        List<Medicine> findByNameContainingIgnoreCaseAndIsActive(String name,Boolean active);
        List<Medicine> findByIsActive(Boolean active);
        boolean existsByNameIgnoreCase(String name);
        boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

}
