package com.example.hospital_wed2.service.admin.impl;

import com.example.hospital_wed2.dto.admin.MedicineDto;
import com.example.hospital_wed2.entity.Medicine;
import com.example.hospital_wed2.repository.MedicineRepository;
import com.example.hospital_wed2.service.admin.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;

    public List<MedicineDto> findAll() {
        return medicineRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public MedicineDto findById(Long id) {
        return medicineRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuốc"));
    }

    @Override
    @Transactional
    public MedicineDto save(MedicineDto dto) {
        // Kiểm tra tên thuốc trùng (bỏ qua chính nó khi edit)
        if (dto.getId() != null) {
            if (medicineRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), dto.getId())) {
                throw new RuntimeException("Tên thuốc \"" + dto.getName() + "\" đã tồn tại trong hệ thống");
            }
        } else {
            if (medicineRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new RuntimeException("Tên thuốc \"" + dto.getName() + "\" đã tồn tại trong hệ thống");
            }
        }
        Medicine medicine = toEntity(dto);
        Medicine saved = medicineRepository.save(medicine);
        return toDTO(saved);
    }

    // FIX: Dùng getActive() thay vì !medicine.getIsActive() để tránh NullPointerException
    public void disable(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuốc"));
        Boolean current = medicine.getActive();
        medicine.setActive(!Boolean.TRUE.equals(current));
        medicineRepository.save(medicine);
    }

    @Override
    public List<MedicineDto> findByName(String name) {
        return medicineRepository.findByNameContainingIgnoreCase(name)
                .stream().map(this::toDTO).toList();
    }

    @Override
    public List<MedicineDto> findByNameAndStatus(String keyword, Boolean active) {
        return medicineRepository.findByNameContainingIgnoreCaseAndIsActive(keyword, active)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<MedicineDto> findByStatus(Boolean active) {
        return medicineRepository.findByIsActive(active)
                .stream().map(this::toDTO).toList();
    }

    // Mapper
    private MedicineDto toDTO(Medicine medicine) {
        return new MedicineDto(
                medicine.getId(),
                medicine.getName(),
                medicine.getIngredient(),
                medicine.getUnit(),
                medicine.getPrice(),
                medicine.getStockQuantity(),
                medicine.getManufacturer(),
                medicine.getDescription(),
                medicine.getActive()
        );
    }

    private Medicine toEntity(MedicineDto dto) {
        Medicine medicine = new Medicine();
        medicine.setId(dto.getId());
        medicine.setName(dto.getName());
        medicine.setIngredient(dto.getIngredient());
        medicine.setUnit(dto.getUnit());
        medicine.setManufacturer(dto.getManufacturer());
        medicine.setDescription(dto.getDescription());
        medicine.setPrice(dto.getPrice() != null ? dto.getPrice() : 0.0);
        medicine.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0);
        medicine.setActive(dto.getActive() != null ? dto.getActive() : true);
        return medicine;
    }
}
