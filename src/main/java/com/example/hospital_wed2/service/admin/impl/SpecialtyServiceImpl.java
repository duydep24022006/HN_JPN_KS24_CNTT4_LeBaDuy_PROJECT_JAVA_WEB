package com.example.hospital_wed2.service.admin.impl;

import com.example.hospital_wed2.dto.admin.SpecialtyDto;
import com.example.hospital_wed2.entity.Specialty;
import com.example.hospital_wed2.repository.SpecialtyRepository;
import com.example.hospital_wed2.service.FileStorageService;
import com.example.hospital_wed2.service.admin.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final FileStorageService fileStorageService;

    @Override
    public List<SpecialtyDto> findAllSpecialties() {
        return specialtyRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<SpecialtyDto> searchSpecialties(String keyword) {
        return specialtyRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public SpecialtyDto findById(Long id) {
        return specialtyRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên khoa"));
    }

    @Override
    public void saveSpecialty(SpecialtyDto dto) {
        if (dto.getId() != null) {
            // UPDATE
            Specialty existing = specialtyRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên khoa"));

            if (specialtyRepository.existsByNameAndIdNot(dto.getName(), dto.getId())) {
                throw new RuntimeException("Tên chuyên khoa đã tồn tại");
            }

            existing.setName(dto.getName());
            existing.setDescription(dto.getDescription());

            if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
                String fileName = fileStorageService.storeFile(dto.getImageFile());
                existing.setImageUrl("/uploads/" + fileName);
            }

        } else {
            // CREATE
            if (specialtyRepository.existsByName(dto.getName())) {
                throw new RuntimeException("Tên chuyên khoa đã tồn tại");
            }

            Specialty specialty = new Specialty();
            specialty.setName(dto.getName());
            specialty.setDescription(dto.getDescription());

            if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
                String fileName = fileStorageService.storeFile(dto.getImageFile());
                specialty.setImageUrl("/uploads/" + fileName);
            }

            specialtyRepository.save(specialty);
        }
    }

    @Override
    public void deleteSpecialty(Long id) {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên khoa"));

        if (!specialty.getDoctors().isEmpty()) {
            throw new RuntimeException("Không thể xóa chuyên khoa đang có bác sĩ");
        }

        specialtyRepository.delete(specialty);
    }

    private SpecialtyDto toDTO(Specialty specialty) {
        SpecialtyDto dto = new SpecialtyDto();
        dto.setId(specialty.getId());
        dto.setName(specialty.getName());
        dto.setDescription(specialty.getDescription());
        dto.setImageUrl(specialty.getImageUrl());
        dto.setDoctorCount(specialty.getDoctors() != null ? specialty.getDoctors().size() : 0);
        return dto;
    }
}
