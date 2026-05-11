package com.example.hospital_wed2.dto.profile;

import com.example.hospital_wed2.entity.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class UpdateProfileRequest {
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ tên không quá 100 ký tự")
    private String fullName;

    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;


    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    private Gender gender;

    @Size(max = 500, message = "Địa chỉ không quá 500 ký tự")
    private String address;

    @Size(max = 20, message = "Số CCCD không hợp lệ")
    private String identityCard;

    private String avatarUrl;

    // Getters & Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdentityCard() {
        return identityCard;
    }

    public void setIdentityCard(String identityCard) {
        this.identityCard = identityCard;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
