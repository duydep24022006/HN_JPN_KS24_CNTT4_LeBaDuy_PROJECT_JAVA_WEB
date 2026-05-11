package com.example.hospital_wed2.service.admin.impl;

import com.example.hospital_wed2.entity.Role;
import com.example.hospital_wed2.entity.User;
import com.example.hospital_wed2.entity.UserProfile;
import com.example.hospital_wed2.repository.UserProfileRepository;
import com.example.hospital_wed2.repository.UserRepository;
import com.example.hospital_wed2.service.admin.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// Đánh dấu đây là class Service để Spring quản lý
@Service
public class UserServiceImpl implements UserService {

    // Repository thao tác với bảng User
    @Autowired
    private UserRepository userRepository;

    // Repository thao tác với bảng UserProfile
    @Autowired
    private UserProfileRepository userProfileRepository;

    // Dùng để mã hóa mật khẩu
    @Autowired
    private PasswordEncoder passwordEncoder;

    // =====================================================
    // TẠO TÀI KHOẢN BÁC SĨ
    // =====================================================

    @Override
    public User createDoctorAccount(
            String email,
            String fullName,
            Role role
    ) {

        // =================================================
        // KIỂM TRA EMAIL ĐÃ TỒN TẠI
        // =================================================

        // Nếu email đã tồn tại trong hệ thống
        if (userRepository.existsByEmail(email)) {

            // Báo lỗi không cho tạo
            throw new IllegalArgumentException(
                    "Email đã tồn tại: " + email
            );
        }

        // =================================================
        // TẠO USER
        // =================================================

        // Tạo object User mới
        User user = new User();

        // Sinh username từ email
        user.setUsername(generateUsername(email));

        // Gán email
        user.setEmail(email);

        // Gán role là DOCTOR
        user.setRole(Role.DOCTOR);

        // Kích hoạt tài khoản
        user.setActive(true);

        // Mã hóa mật khẩu mặc định
        user.setPassword(
                passwordEncoder.encode("123456789")
        );

        // =================================================
        // LƯU USER
        // =================================================

        // Save user vào DB
        User savedUser = userRepository.save(user);

        // =================================================
        // TẠO USER PROFILE
        // =================================================

        // Tạo profile mới
        UserProfile profile = new UserProfile();

        // Liên kết profile với user
        profile.setUser(savedUser);

        // Gán họ tên
        profile.setFullName(
                fullName != null ? fullName : ""
        );

        // Lưu profile vào DB
        userProfileRepository.save(profile);

        // =================================================
        // TRẢ VỀ USER ĐÃ TẠO
        // =================================================

        return savedUser;
    }

    // =====================================================
    // TẠO USERNAME TỪ EMAIL
    // =====================================================

    private String generateUsername(String email) {

        // Cắt phần trước dấu @ làm username
        return email.substring(
                0,
                email.indexOf("@")
        );
    }
}