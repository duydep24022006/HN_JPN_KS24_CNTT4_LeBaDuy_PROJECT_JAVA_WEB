package com.example.hospital_wed2.repository;

import com.example.hospital_wed2.entity.PrescriptionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PrescriptionDetailRepository extends JpaRepository<PrescriptionDetail, Long> {

    @Query("""
        SELECT pd.medicine.name, SUM(pd.quantity)
        FROM PrescriptionDetail pd
        GROUP BY pd.medicine.id, pd.medicine.name
        ORDER BY SUM(pd.quantity) DESC
    """)
    List<Object[]> findTopMedicines();
}