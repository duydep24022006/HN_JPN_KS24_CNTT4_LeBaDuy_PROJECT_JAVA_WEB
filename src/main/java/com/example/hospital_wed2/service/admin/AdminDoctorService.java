package com.example.hospital_wed2.service.admin;

import com.example.hospital_wed2.dto.admin.AdminDoctorDto;
import com.example.hospital_wed2.entity.Gender;

import java.util.List;

public interface AdminDoctorService {
    void saveDoctor(AdminDoctorDto dto);
    List<AdminDoctorDto> getAllDoctors();
    List<AdminDoctorDto> searchDoctors(String keyword, Long specialtyId, Boolean active, Gender gender);
    AdminDoctorDto getDoctorById(Long id);
    void deleteDoctor(Long id);
    void toggleDoctor(Long id);  // FIX: toggle active/inactive
}
