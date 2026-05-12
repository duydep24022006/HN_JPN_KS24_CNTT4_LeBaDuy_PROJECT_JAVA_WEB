package com.example.hospital_wed2.service.doctor.impl;

import com.example.hospital_wed2.dto.profile.doctor.DoctorProfileResponse;
import com.example.hospital_wed2.dto.profile.doctor.UpdateDoctorProfileRequest;
import com.example.hospital_wed2.dto.profile.shared.ChangePasswordRequest;
import com.example.hospital_wed2.entity.Doctor;
import com.example.hospital_wed2.entity.User;
import com.example.hospital_wed2.entity.UserProfile;
import com.example.hospital_wed2.exception.ResourceNotFoundException;
import com.example.hospital_wed2.repository.DoctorRepository;
import com.example.hospital_wed2.repository.SpecialtyRepository;
import com.example.hospital_wed2.repository.UserProfileRepository;
import com.example.hospital_wed2.repository.UserRepository;
import com.example.hospital_wed2.service.doctor.DoctorProfileService;
import com.example.hospital_wed2.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorProfileServiceImpl implements DoctorProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public DoctorProfileResponse getDoctorProfile(String email) {
        User user = getUser(email);
        UserProfile profile = getProfile(user.getId());
        Doctor doctor = getDoctor(user.getId());

        return mapToResponse(user, profile, doctor);
    }

    @Override
    @Transactional
    public DoctorProfileResponse updateDoctorProfile(String email, UpdateDoctorProfileRequest request) {
        User user = getUser(email);
        Doctor doctor = getDoctor(user.getId());
        UserProfile profile = getProfile(user.getId());

        // Cập nhật thông tin chuyên môn
        if (request.getSpecialtyId() != null) {
            doctor.setSpecialty(specialtyRepository.findById(request.getSpecialtyId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyên khoa")));
        }

        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setDescription(request.getDescription());
        doctor.setConsultationFee(request.getConsultationFee());

        doctorRepository.save(doctor);

        // Cập nhật avatar nếu có
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            profile.setAvatarUrl(request.getAvatarUrl());
            userProfileRepository.save(profile);
        }

        return mapToResponse(user, profile, doctor);
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = getUser(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ==================== HELPER ====================
    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    private UserProfile getProfile(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ"));
    }

    private Doctor getDoctor(Long userId) {
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin bác sĩ"));
    }

    private DoctorProfileResponse mapToResponse(User user, UserProfile profile, Doctor doctor) {
        DoctorProfileResponse res = new DoctorProfileResponse();

        // User fields
        res.setUserId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setRole(user.getRole());
        res.setActive(user.getActive());
        res.setCreatedAt(user.getCreatedAt());

        // Profile fields
        res.setFullName(profile.getFullName());
        res.setPhoneNumber(profile.getPhoneNumber());
        res.setDateOfBirth(profile.getDateOfBirth());
        res.setGender(profile.getGender());
        res.setAddress(profile.getAddress());
        res.setIdentityCard(profile.getIdentityCard());
        res.setAvatarUrl(profile.getAvatarUrl());

        // Doctor fields
        res.setDoctorId(doctor.getId());
        res.setSpecialtyId(doctor.getSpecialty() != null ? doctor.getSpecialty().getId() : null);
        res.setSpecialtyName(doctor.getSpecialty() != null ? doctor.getSpecialty().getName() : "");
        res.setLicenseNumber(doctor.getLicenseNumber());
        res.setExperienceYears(doctor.getExperienceYears());
        res.setDescription(doctor.getDescription());
        res.setConsultationFee(doctor.getConsultationFee());

        return res;
    }
}