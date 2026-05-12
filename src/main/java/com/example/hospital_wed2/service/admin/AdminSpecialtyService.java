package com.example.hospital_wed2.service.admin;

import com.example.hospital_wed2.dto.admin.SpecialtyDto;
import java.util.List;

public interface AdminSpecialtyService {

    List<SpecialtyDto> findAll();

    List<SpecialtyDto> search(String keyword);

    SpecialtyDto findById(Long id);

    void save(SpecialtyDto dto);

    void delete(Long id);
}