package com.example.hospital_wed2.dto.admin;

import com.example.hospital_wed2.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDto {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private Boolean active;
    private LocalDateTime createdAt;

    // Profile info
    private String fullName;
    private String phoneNumber;
    private String address;
    private String identityCard;
    private String avatarUrl;
}