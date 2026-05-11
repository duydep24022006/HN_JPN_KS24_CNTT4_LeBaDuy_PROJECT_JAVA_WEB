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

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public User createDoctorAccount(String email, String fullName, Role role) {

        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã tồn tại: " + email);
        }

        User user = new User();
        user.setUsername(generateUsername(email));
        user.setEmail(email);
        user.setRole(Role.DOCTOR);
        user.setActive(true);
        user.setPassword(passwordEncoder.encode("123456789"));  // Mật khẩu mặc định

        User savedUser = userRepository.save(user);

        // Tạo UserProfile
        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        profile.setFullName(fullName != null ? fullName : "");
        userProfileRepository.save(profile);

        return savedUser;
    }

    private String generateUsername(String email) {
        return email.substring(0, email.indexOf("@"));
    }
}
