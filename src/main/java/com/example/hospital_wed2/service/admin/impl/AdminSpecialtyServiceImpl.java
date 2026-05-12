package com.example.hospital_wed2.service.admin.impl;

import com.example.hospital_wed2.dto.admin.SpecialtyDto;
import com.example.hospital_wed2.entity.Specialty;
import com.example.hospital_wed2.exception.ResourceNotFoundException;
import com.example.hospital_wed2.repository.SpecialtyRepository;
import com.example.hospital_wed2.service.FileStorageService;
import com.example.hospital_wed2.service.admin.AdminSpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminSpecialtyServiceImpl implements AdminSpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<SpecialtyDto> findAll() {
        return specialtyRepository.findAll().stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialtyDto> search(String keyword) {
        return specialtyRepository.findByNameContainingIgnoreCase(keyword)
                .stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SpecialtyDto findById(Long id) {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa"));
        return mapToDto(specialty);
    }

    @Override
    public void save(SpecialtyDto dto) {
        if (dto.getId() != null) {
            update(dto);
        } else {
            create(dto);
        }
    }

    private void create(SpecialtyDto dto) {
        if (specialtyRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Tên chuyên khoa đã tồn tại");
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

    private void update(SpecialtyDto dto) {
        Specialty specialty = specialtyRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa"));

        if (!specialty.getName().equals(dto.getName()) &&
                specialtyRepository.existsByNameAndIdNot(dto.getName(), dto.getId())) {
            throw new IllegalArgumentException("Tên chuyên khoa đã tồn tại");
        }

        specialty.setName(dto.getName());
        specialty.setDescription(dto.getDescription());

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String fileName = fileStorageService.storeFile(dto.getImageFile());
            specialty.setImageUrl("/uploads/" + fileName);
        }

        specialtyRepository.save(specialty);
    }

    @Override
    public void delete(Long id) {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa"));

        if (!specialty.getDoctors().isEmpty()) {
            throw new IllegalArgumentException("Không thể xóa chuyên khoa đang có bác sĩ");
        }

        specialtyRepository.delete(specialty);
    }

    private SpecialtyDto mapToDto(Specialty specialty) {
        SpecialtyDto dto = new SpecialtyDto();
        dto.setId(specialty.getId());
        dto.setName(specialty.getName());
        dto.setDescription(specialty.getDescription());
        dto.setImageUrl(specialty.getImageUrl());
        dto.setDoctorCount(specialty.getDoctors() != null ? specialty.getDoctors().size() : 0);
        return dto;
    }
}