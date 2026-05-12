package com.example.hospital_wed2.service.auth;

import com.example.hospital_wed2.dto.auth.LoginDTO;
import com.example.hospital_wed2.dto.auth.RegisterDTO;
import com.example.hospital_wed2.entity.Role;
import com.example.hospital_wed2.entity.User;
import com.example.hospital_wed2.entity.UserProfile;
import com.example.hospital_wed2.repository.UserProfileRepository;
import com.example.hospital_wed2.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterDTO dto) {
        // Kiểm tra username hoặc email đã tồn tại
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isEmpty()) {
            if (userProfileRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
                throw new RuntimeException("Số điện thoại đã được sử dụng");  // tạm giữ để dễ test
            }
        }

        // Tạo User
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.PATIENT);
        user.setActive(true);

        // Set thời gian
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        user = userRepository.save(user);

        // Tạo UserProfile
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFullName(dto.getFullName());
        profile.setPhoneNumber(dto.getPhoneNumber());

        if (dto.getDateOfBirth() != null) {
            profile.setDateOfBirth(dto.getDateOfBirth());
        }
        if (dto.getGender() != null) {
            profile.setGender(dto.getGender());
        }
        if (dto.getAddress() != null) {
            profile.setAddress(dto.getAddress());
        }

        userProfileRepository.save(profile);
    }

    public User login(LoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email hoặc mật khẩu không đúng"));

        if (!user.getActive()) {
            throw new IllegalArgumentException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Email hoặc mật khẩu không đúng");
        }

        return user;
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }
}