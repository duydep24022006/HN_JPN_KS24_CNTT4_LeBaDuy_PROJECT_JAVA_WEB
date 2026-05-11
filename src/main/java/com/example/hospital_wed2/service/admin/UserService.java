package com.example.hospital_wed2.service.admin;

import com.example.hospital_wed2.entity.Role;
import com.example.hospital_wed2.entity.User;

public interface UserService {
    User createDoctorAccount(String email, String fullName, Role role);
}
