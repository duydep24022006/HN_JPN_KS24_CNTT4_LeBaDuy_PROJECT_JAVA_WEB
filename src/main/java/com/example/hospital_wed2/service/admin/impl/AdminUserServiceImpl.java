package com.example.hospital_wed2.service.admin.impl;

import com.example.hospital_wed2.dto.admin.AdminUserDto;
import com.example.hospital_wed2.entity.Role;
import com.example.hospital_wed2.entity.User;
import com.example.hospital_wed2.entity.UserProfile;
import com.example.hospital_wed2.exception.ResourceNotFoundException;
import com.example.hospital_wed2.repository.UserProfileRepository;
import com.example.hospital_wed2.repository.UserRepository;
import com.example.hospital_wed2.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserDto> getAllUsers() {
        return userRepository.findAllWithProfile().stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserDto> searchUsers(String keyword, Role role, Boolean active) {
        return userRepository.searchUsers(role, active, keyword).stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        return mapToDto(user);
    }

    @Override
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        user.setActive(false); // soft delete
        userRepository.save(user);
    }


    private AdminUserDto mapToDto(User user) {
        AdminUserDto dto = new AdminUserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.getActive());
        dto.setCreatedAt(user.getCreatedAt());

        UserProfile profile = user.getProfile();
        if (profile != null) {
            dto.setFullName(profile.getFullName());
            dto.setPhoneNumber(profile.getPhoneNumber());
        }
        return dto;
    }
}