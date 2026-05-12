package com.example.hospital_wed2.service.admin;

import com.example.hospital_wed2.dto.admin.MedicineDto;
import java.util.List;

public interface AdminMedicineService {
    List<MedicineDto> findAll();
    List<MedicineDto> search(String keyword, Boolean active);
    MedicineDto findById(Long id);
    MedicineDto save(MedicineDto dto);
    void toggleStatus(Long id);
    void delete(Long id);
}