package com.example.hospital_wed2.service.admin;

import com.example.hospital_wed2.dto.admin.SpecialtyDto;

import java.util.List;

public interface SpecialtyService {
    List<SpecialtyDto> findAllSpecialties();
    List<SpecialtyDto> searchSpecialties(String keyword);
    SpecialtyDto findById(Long id);
    void saveSpecialty(SpecialtyDto dto);
    void deleteSpecialty(Long id);
}
