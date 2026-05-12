package com.example.hospital_wed2.service.admin.impl;

import com.example.hospital_wed2.dto.admin.AdminDoctorDto;
import com.example.hospital_wed2.entity.*;
import com.example.hospital_wed2.exception.ResourceNotFoundException;
import com.example.hospital_wed2.repository.*;
import com.example.hospital_wed2.service.FileStorageService;
import com.example.hospital_wed2.service.admin.AdminDoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminDoctorServiceImpl implements AdminDoctorService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<AdminDoctorDto> getAllDoctors() {
        return doctorRepository.findAll().stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminDoctorDto> searchDoctors(String keyword, Long specialtyId, Boolean active, Gender gender) {
        // FIX: Dùng query DB có sẵn thay vì in-memory filter
        return doctorRepository.searchDoctors(keyword, specialtyId, active, gender)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDoctorDto getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ"));
        return mapToDto(doctor);
    }

    @Override
    public void saveDoctor(AdminDoctorDto dto) {
        if (dto.getId() != null) {
            updateDoctor(dto);
        } else {
            createDoctor(dto);
        }
    }

    private void createDoctor(AdminDoctorDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại trong hệ thống");
        }
        if (doctorRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new IllegalArgumentException("Số giấy phép hành nghề đã tồn tại");
        }

        User user = new User();
        user.setUsername(generateUniqueUsername(dto.getEmail()));
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole(Role.DOCTOR);
        user.setActive(true);
        user = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        updateProfile(profile, dto);
        userProfileRepository.save(profile);

        Specialty specialty = specialtyRepository.findById(dto.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa"));

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setSpecialty(specialty);
        doctor.setLicenseNumber(dto.getLicenseNumber());
        doctor.setExperienceYears(dto.getExperienceYears());
        doctor.setDescription(dto.getDescription());
        doctor.setConsultationFee(dto.getConsultationFee());
        doctorRepository.save(doctor);
    }

    private void updateDoctor(AdminDoctorDto dto) {
        Doctor doctor = doctorRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ"));

        User user = doctor.getUser();
        UserProfile profile = user.getProfile() != null ? user.getProfile() : new UserProfile();

        if (profile.getId() == null) {
            profile.setUser(user);
        }

        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmailAndIdNot(dto.getEmail(), user.getId())) {
            throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác");
        }

        if (!doctor.getLicenseNumber().equals(dto.getLicenseNumber()) &&
                doctorRepository.existsByLicenseNumberAndIdNot(dto.getLicenseNumber(), doctor.getId())) {
            throw new IllegalArgumentException("Số giấy phép hành nghề đã tồn tại");
        }

        updateProfile(profile, dto);
        user.setEmail(dto.getEmail());
        user.setActive(dto.getActive() != null ? dto.getActive() : user.getActive());

        userRepository.save(user);
        userProfileRepository.save(profile);

        Specialty specialty = specialtyRepository.findById(dto.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa"));

        doctor.setSpecialty(specialty);
        doctor.setLicenseNumber(dto.getLicenseNumber());
        doctor.setExperienceYears(dto.getExperienceYears());
        doctor.setDescription(dto.getDescription());
        doctor.setConsultationFee(dto.getConsultationFee());

        doctorRepository.save(doctor);
    }

    @Override
    public void toggleDoctorStatus(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ"));
        doctor.getUser().setActive(!Boolean.TRUE.equals(doctor.getUser().getActive()));
        userRepository.save(doctor.getUser());
    }

    // FIX: Thêm alias method để tương thích với old controller pattern
    public void toggleDoctor(Long id) {
        toggleDoctorStatus(id);
    }

    @Override
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ"));
        doctor.getUser().setActive(false);
        userRepository.save(doctor.getUser());
    }

    private void updateProfile(UserProfile profile, AdminDoctorDto dto) {
        profile.setFullName(dto.getFullName());
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setGender(dto.getGender());
        profile.setAddress(dto.getAddress());
        profile.setIdentityCard(dto.getIdentityCard());
    }

    private String generateUniqueUsername(String email) {
        String base = email.split("@")[0];
        String username = base;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = base + counter++;
        }
        return username;
    }

    private AdminDoctorDto mapToDto(Doctor doctor) {
        AdminDoctorDto dto = new AdminDoctorDto();
        User user = doctor.getUser();
        UserProfile profile = user.getProfile();

        dto.setId(doctor.getId());
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.getActive());

        if (profile != null) {
            dto.setFullName(profile.getFullName());
            dto.setPhoneNumber(profile.getPhoneNumber());
            dto.setDateOfBirth(profile.getDateOfBirth());
            dto.setGender(profile.getGender());
            dto.setAddress(profile.getAddress());
            dto.setIdentityCard(profile.getIdentityCard());
            dto.setAvatarUrl(profile.getAvatarUrl());
        }

        dto.setSpecialtyId(doctor.getSpecialty().getId());
        dto.setSpecialtyName(doctor.getSpecialty().getName());
        dto.setLicenseNumber(doctor.getLicenseNumber());
        dto.setExperienceYears(doctor.getExperienceYears());
        dto.setDescription(doctor.getDescription());
        dto.setConsultationFee(doctor.getConsultationFee());

        return dto;
    }
}