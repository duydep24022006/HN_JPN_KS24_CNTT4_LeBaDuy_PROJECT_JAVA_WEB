package com.example.hospital_wed2.service.profile.impl;

import com.example.hospital_wed2.dto.profile.*;
import com.example.hospital_wed2.entity.*;
import com.example.hospital_wed2.repository.DoctorRepository;
import com.example.hospital_wed2.repository.SpecialtyRepository;
import com.example.hospital_wed2.repository.UserProfileRepository;
import com.example.hospital_wed2.repository.UserRepository;
import com.example.hospital_wed2.service.profile.ProfileService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileServiceImpl(UserRepository userRepository,
                              UserProfileRepository userProfileRepository,
                              DoctorRepository doctorRepository,
                              SpecialtyRepository specialtyRepository,
                              PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.doctorRepository = doctorRepository;
        this.specialtyRepository = specialtyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ===== CHUNG =====

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(String email) {
        User user = findUserByEmail(email);
        UserProfile profile = findProfileByUserId(user.getId());
        return mapToUserProfileResponse(user, profile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(String email, UpdateProfileRequest request) {
        User user = findUserByEmail(email);
        UserProfile profile = findProfileByUserId(user.getId());

        profile.setFullName(request.getFullName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setAddress(request.getAddress());
        profile.setIdentityCard(request.getIdentityCard());

        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }

        userProfileRepository.save(profile);
        return mapToUserProfileResponse(user, profile);
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findUserByEmail(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("Xác nhận mật khẩu mới không khớp");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ===== DOCTOR =====

    @Override
    @Transactional(readOnly = true)
    public DoctorProfileResponse getDoctorProfile(String email) {
        User user = findUserByEmail(email);
        checkRole(user, Role.DOCTOR);

        UserProfile profile = findProfileByUserId(user.getId());
        Doctor doctor = findDoctorByUserId(user.getId());

        return mapToDoctorProfileResponse(user, profile, doctor);
    }

    @Override
    @Transactional
    public DoctorProfileResponse updateDoctorProfile(String email, UpdateDoctorProfileRequest request) {
        User user = findUserByEmail(email);
        checkRole(user, Role.DOCTOR);

        Doctor doctor = findDoctorByUserId(user.getId());

        if (doctorRepository.existsByLicenseNumberAndIdNot(request.getLicenseNumber(), doctor.getId())) {
            throw new IllegalArgumentException("Số giấy phép hành nghề đã được sử dụng");
        }

        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new IllegalArgumentException("Chuyên khoa không tồn tại"));

        doctor.setSpecialty(specialty);
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setDescription(request.getDescription());
        doctor.setConsultationFee(request.getConsultationFee());

        doctorRepository.save(doctor);

        UserProfile profile = findProfileByUserId(user.getId());
        return mapToDoctorProfileResponse(user, profile, doctor);
    }

    // ===== ADMIN =====

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        UserProfile profile = findProfileByUserId(userId);
        return mapToUserProfileResponse(user, profile);
    }

    @Override
    @Transactional
    public void toggleUserActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        user.setActive(!user.getActive());
        userRepository.save(user);
    }

    // ===== HELPER =====

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + email));
    }

    private UserProfile findProfileByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin hồ sơ"));
    }

    private Doctor findDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin bác sĩ"));
    }

    private void checkRole(User user, Role expectedRole) {
        if (user.getRole() != expectedRole) {
            throw new SecurityException("Bạn không có quyền thực hiện thao tác này");
        }
    }

    private UserProfileResponse mapToUserProfileResponse(User user, UserProfile profile) {
        UserProfileResponse res = new UserProfileResponse();
        res.setUserId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setRole(user.getRole());
        res.setActive(user.getActive());
        res.setCreatedAt(user.getCreatedAt());
        res.setFullName(profile.getFullName());
        res.setPhoneNumber(profile.getPhoneNumber());
        res.setDateOfBirth(profile.getDateOfBirth());
        res.setGender(profile.getGender());
        res.setAddress(profile.getAddress());
        res.setIdentityCard(profile.getIdentityCard());
        res.setAvatarUrl(profile.getAvatarUrl());
        return res;
    }

    private DoctorProfileResponse mapToDoctorProfileResponse(User user, UserProfile profile, Doctor doctor) {
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
        res.setSpecialtyId(doctor.getSpecialty().getId());
        res.setSpecialtyName(doctor.getSpecialty().getName());
        res.setLicenseNumber(doctor.getLicenseNumber());
        res.setExperienceYears(doctor.getExperienceYears());
        res.setDescription(doctor.getDescription());
        res.setConsultationFee(doctor.getConsultationFee());
        return res;
    }
}
