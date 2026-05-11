package com.example.hospital_wed2.dto.profile;

import com.example.hospital_wed2.entity.Gender;
import com.example.hospital_wed2.entity.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class UserProfileResponse {
    private Long userId;
    private String username;
    private String email;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // Profile info
    private String fullName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String identityCard;
    private String avatarUrl;

    // Getters & Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Boolean getActive() { return isActive; }
    public void setActive(Boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getIdentityCard() { return identityCard; }
    public void setIdentityCard(String identityCard) { this.identityCard = identityCard; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }


}
