package com.example.hospital_wed2.service.admin;

import com.example.hospital_wed2.dto.admin.AdminUserDto;
import com.example.hospital_wed2.entity.Role;
import java.util.List;

public interface AdminUserService {
    List<AdminUserDto> getAllUsers();
    List<AdminUserDto> searchUsers(String keyword, Role role, Boolean active);
    AdminUserDto getUserById(Long id);
    void toggleUserStatus(Long id);
    void deleteUser(Long id);
}