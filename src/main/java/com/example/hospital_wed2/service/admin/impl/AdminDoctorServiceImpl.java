package com.example.hospital_wed2.service.admin.impl;

import com.example.hospital_wed2.dto.admin.AdminDoctorDto;
import com.example.hospital_wed2.entity.*;
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
    private final UserProfileRepository profileRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    // =====================================================
    // SAVE DOCTOR
    // =====================================================

    @Override
    public void saveDoctor(AdminDoctorDto dto) {
        if (dto.getId() != null) {
            updateDoctor(dto);
            return;
        }
        createDoctor(dto);
    }

    // =====================================================
    // CREATE
    // =====================================================

    private void createDoctor(AdminDoctorDto dto) {
        validateCreate(dto);
        Specialty specialty = getSpecialty(dto.getSpecialtyId());
        User user = createUser(dto);
        UserProfile profile = createProfile(dto, user);
        Doctor doctor = createDoctorEntity(dto, user, specialty);
        user.setProfile(profile);
        user.setDoctor(doctor);
    }

    // =====================================================
    // UPDATE
    // =====================================================

    private void updateDoctor(AdminDoctorDto dto) {
        Doctor doctor = doctorRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

        User user = doctor.getUser();
        UserProfile profile = user.getProfile();
        Specialty specialty = getSpecialty(dto.getSpecialtyId());

        // FIX: Validate email/phone trùng - dùng profileRepository thay vì userRepository cho phone
        validateUpdate(dto, user.getId(), profile != null ? profile.getId() : null);

        // USER
        user.setEmail(dto.getEmail());
        user.setActive(dto.getActive());

        // PROFILE
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
        }
        updateProfile(profile, dto);
        profileRepository.save(profile);

        // DOCTOR
        doctor.setLicenseNumber(dto.getLicenseNumber());
        doctor.setExperienceYears(dto.getExperienceYears());
        doctor.setDescription(dto.getDescription());
        doctor.setConsultationFee(dto.getConsultationFee());
        doctor.setSpecialty(specialty);
    }

    // =====================================================
    // VALIDATE CREATE
    // =====================================================

    private void validateCreate(AdminDoctorDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng trong hệ thống");
        }

        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {
            if (profileRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
                throw new RuntimeException("Số điện thoại này đã được sử dụng bởi tài khoản khác");
            }
        }

        if (doctorRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new RuntimeException("Số giấy phép hành nghề này đã tồn tại trong hệ thống");
        }
    }

    // =====================================================
    // VALIDATE UPDATE
    // =====================================================

    private void validateUpdate(AdminDoctorDto dto, Long userId, Long profileId) {
        if (userRepository.existsByEmailAndIdNot(dto.getEmail(), userId)) {
            throw new RuntimeException("Email này đã được sử dụng bởi tài khoản khác");
        }

        // FIX: Dùng profileRepository.existsByPhoneNumberAndIdNot thay vì userRepository
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {
            if (profileId != null) {
                if (profileRepository.existsByPhoneNumberAndIdNot(dto.getPhoneNumber(), profileId)) {
                    throw new RuntimeException("Số điện thoại này đã được sử dụng bởi tài khoản khác");
                }
            } else {
                if (profileRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
                    throw new RuntimeException("Số điện thoại này đã được sử dụng bởi tài khoản khác");
                }
            }
        }

        if (doctorRepository.existsByLicenseNumberAndIdNot(dto.getLicenseNumber(), dto.getId())) {
            throw new RuntimeException("Số giấy phép hành nghề này đã tồn tại trong hệ thống");
        }
    }

    // =====================================================
    // CREATE USER
    // =====================================================

    private User createUser(AdminDoctorDto dto) {
        User user = new User();
        String username = dto.getEmail().split("@")[0];
        // Đảm bảo username unique
        String baseUsername = username;
        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix++;
        }
        user.setUsername(username);
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole(Role.DOCTOR);
        user.setActive(dto.getActive() != null ? dto.getActive() : true);
        return userRepository.save(user);
    }

    // =====================================================
    // CREATE PROFILE
    // =====================================================

    private UserProfile createProfile(AdminDoctorDto dto, User user) {
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        updateProfile(profile, dto);
        return profileRepository.save(profile);
    }

    // =====================================================
    // CREATE DOCTOR
    // =====================================================

    private Doctor createDoctorEntity(AdminDoctorDto dto, User user, Specialty specialty) {
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setSpecialty(specialty);
        doctor.setLicenseNumber(dto.getLicenseNumber());
        doctor.setExperienceYears(dto.getExperienceYears());
        doctor.setDescription(dto.getDescription());
        doctor.setConsultationFee(dto.getConsultationFee());
        return doctorRepository.save(doctor);
    }

    // =====================================================
    // UPDATE PROFILE
    // =====================================================

    private void updateProfile(UserProfile profile, AdminDoctorDto dto) {
        profile.setFullName(dto.getFullName());
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setGender(dto.getGender());
        profile.setAddress(dto.getAddress());
        profile.setIdentityCard(dto.getIdentityCard());
        handleAvatarUpload(profile, dto);
    }

    // =====================================================
    // AVATAR
    // =====================================================

    private void handleAvatarUpload(UserProfile profile, AdminDoctorDto dto) {
        if (dto.getAvatarFile() == null || dto.getAvatarFile().isEmpty()) {
            return;
        }
        try {
            String fileName = fileStorageService.storeFile(dto.getAvatarFile());
            profile.setAvatarUrl(fileName);
        } catch (Exception e) {
            // Không làm gián đoạn save nếu upload ảnh thất bại
        }
    }

    // =====================================================
    // SPECIALTY
    // =====================================================

    private Specialty getSpecialty(Long id) {
        return specialtyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên khoa"));
    }

    // =====================================================
    // GET ALL
    // =====================================================

    @Override
    public List<AdminDoctorDto> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // =====================================================
    // SEARCH
    // =====================================================

    @Override
    public List<AdminDoctorDto> searchDoctors(String keyword, Long specialtyId, Boolean active, Gender gender) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword : null;
        return doctorRepository.findAll()
                .stream()
                .filter(d -> {
                    UserProfile p = d.getUser().getProfile();
                    User u = d.getUser();

                    if (kw != null) {
                        String name = p != null ? p.getFullName() : "";
                        String email = u.getEmail() != null ? u.getEmail() : "";
                        if (!name.toLowerCase().contains(kw.toLowerCase()) &&
                            !email.toLowerCase().contains(kw.toLowerCase())) {
                            return false;
                        }
                    }
                    if (specialtyId != null && !d.getSpecialty().getId().equals(specialtyId)) {
                        return false;
                    }
                    if (active != null && !u.getActive().equals(active)) {
                        return false;
                    }
                    if (gender != null && (p == null || p.getGender() != gender)) {
                        return false;
                    }
                    return true;
                })
                .map(this::mapToDto)
                .toList();
    }

    // =====================================================
    // GET BY ID
    // =====================================================

    @Override
    public AdminDoctorDto getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));
        return mapToDto(doctor);
    }

    // =====================================================
    // DELETE (soft delete - vô hiệu hóa)
    // =====================================================

    @Override
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));
        doctor.getUser().setActive(false);
    }

    // =====================================================
    // FIX: TOGGLE ACTIVE (flip trạng thái)
    // =====================================================

    @Override
    public void toggleDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));
        boolean current = Boolean.TRUE.equals(doctor.getUser().getActive());
        doctor.getUser().setActive(!current);
    }

    // =====================================================
    // MAP DTO
    // =====================================================

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
