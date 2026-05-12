package com.example.hospital_wed2.service.admin;

import com.example.hospital_wed2.dto.admin.AdminDoctorDto;
import com.example.hospital_wed2.entity.Gender;
import java.util.List;

public interface AdminDoctorService {
    List<AdminDoctorDto> getAllDoctors();
    List<AdminDoctorDto> searchDoctors(String keyword, Long specialtyId, Boolean active, Gender gender);
    AdminDoctorDto getDoctorById(Long id);
    void saveDoctor(AdminDoctorDto dto);
    void toggleDoctorStatus(Long id);
    void deleteDoctor(Long id);
}