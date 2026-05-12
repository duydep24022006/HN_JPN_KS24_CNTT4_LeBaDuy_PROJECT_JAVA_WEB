package com.example.hospital_wed2.service.admin.impl;

import com.example.hospital_wed2.dto.profile.shared.ChangePasswordRequest;
import com.example.hospital_wed2.dto.profile.shared.UpdateProfileRequest;
import com.example.hospital_wed2.dto.profile.shared.UserProfileResponse;
import com.example.hospital_wed2.entity.User;
import com.example.hospital_wed2.entity.UserProfile;
import com.example.hospital_wed2.repository.UserProfileRepository;
import com.example.hospital_wed2.repository.UserRepository;
import com.example.hospital_wed2.service.admin.AdminProfileService;
import com.example.hospital_wed2.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProfileServiceImpl implements AdminProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserProfileResponse getMyProfile(String email) {
        User user = getUser(email);
        UserProfile profile = getProfile(user.getId());
        return mapToResponse(user, profile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(String email, UpdateProfileRequest request) {
        User user = getUser(email);
        UserProfile profile = getProfile(user.getId());

        // Cập nhật thông tin profile
        profile.setFullName(request.getFullName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setAddress(request.getAddress());
        profile.setIdentityCard(request.getIdentityCard());

        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }

        userProfileRepository.save(profile);
        return mapToResponse(user, profile);
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
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
    }

    private UserProfile getProfile(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ cá nhân"));
    }

    private UserProfileResponse mapToResponse(User user, UserProfile profile) {
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
}