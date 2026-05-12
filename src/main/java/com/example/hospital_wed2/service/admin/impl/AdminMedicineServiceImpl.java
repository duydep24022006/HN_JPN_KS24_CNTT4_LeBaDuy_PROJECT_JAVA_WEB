package com.example.hospital_wed2.service.admin.impl;

import com.example.hospital_wed2.dto.admin.MedicineDto;
import com.example.hospital_wed2.entity.Medicine;
import com.example.hospital_wed2.exception.ResourceNotFoundException;
import com.example.hospital_wed2.repository.MedicineRepository;
import com.example.hospital_wed2.service.admin.AdminMedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminMedicineServiceImpl implements AdminMedicineService {

    private final MedicineRepository medicineRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MedicineDto> findAll() {
        return medicineRepository.findAll().stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineDto> search(String keyword, Boolean active) {
        List<Medicine> medicines;
        if (keyword != null && !keyword.trim().isEmpty()) {
            if (active != null) {
                medicines = medicineRepository.findByNameContainingIgnoreCaseAndIsActive(keyword, active);
            } else {
                medicines = medicineRepository.findByNameContainingIgnoreCase(keyword);
            }
        } else {
            if (active != null) {
                medicines = medicineRepository.findByIsActive(active);
            } else {
                medicines = medicineRepository.findAll();
            }
        }
        return medicines.stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineDto findById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thuốc"));
        return mapToDto(medicine);
    }

    @Override
    public MedicineDto save(MedicineDto dto) {
        Medicine medicine;
        if (dto.getId() != null) {
            medicine = medicineRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thuốc"));
        } else {
            medicine = new Medicine();
        }

        medicine.setName(dto.getName());
        medicine.setIngredient(dto.getIngredient());
        medicine.setUnit(dto.getUnit());
        medicine.setPrice(dto.getPrice());
        medicine.setStockQuantity(dto.getStockQuantity());
        medicine.setManufacturer(dto.getManufacturer());
        medicine.setDescription(dto.getDescription());
        medicine.setActive(dto.getActive() != null ? dto.getActive() : true);

        medicine = medicineRepository.save(medicine);
        return mapToDto(medicine);
    }

    @Override
    public void toggleStatus(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thuốc"));
        medicine.setActive(!Boolean.TRUE.equals(medicine.getActive()));
        medicineRepository.save(medicine);
    }

    @Override
    public void delete(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thuốc"));
        medicineRepository.delete(medicine);
    }

    private MedicineDto mapToDto(Medicine medicine) {
        MedicineDto dto = new MedicineDto();
        dto.setId(medicine.getId());
        dto.setName(medicine.getName());
        dto.setIngredient(medicine.getIngredient());
        dto.setUnit(medicine.getUnit());
        dto.setPrice(medicine.getPrice());
        dto.setStockQuantity(medicine.getStockQuantity());
        dto.setManufacturer(medicine.getManufacturer());
        dto.setDescription(medicine.getDescription());
        dto.setActive(medicine.getActive());
        return dto;
    }
}