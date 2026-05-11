package com.example.hospital_wed2.dto.admin;

import com.example.hospital_wed2.entity.Gender;
import com.example.hospital_wed2.entity.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDoctorDto {

    // =====================================================
    // USER
    // =====================================================

    private Long id;

    private Long userId;

    @Size(max = 50, message = "Tên đăng nhập tối đa 50 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    private Role role;

    private Boolean active = true;

    private String password;

    // =====================================================
    // USER PROFILE
    // =====================================================

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    @Pattern(
            regexp = "^(\\+84|0)[0-9]{9,10}$",
            message = "Số điện thoại không hợp lệ"
    )
    private String phoneNumber;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past(message = "Ngày sinh phải ở trong quá khứ")
    private LocalDate dateOfBirth;

    @NotNull(message = "Giới tính không được để trống")
    private Gender gender;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String address;

    @Size(max = 20, message = "CCCD tối đa 20 ký tự")
    private String identityCard;

    private String avatarUrl;

    private MultipartFile avatarFile;

    // =====================================================
    // SPECIALTY
    // =====================================================

    @NotNull(message = "Chuyên khoa không được để trống")
    private Long specialtyId;

    private String specialtyName;

    private String specialtyDescription;

    private String specialtyImageUrl;

    // =====================================================
    // DOCTOR
    // =====================================================

    @NotBlank(message = "Số giấy phép không được để trống")
    @Size(max = 50, message = "Số giấy phép tối đa 50 ký tự")
    private String licenseNumber;

    @Min(value = 0, message = "Số năm kinh nghiệm phải >= 0")
    private Integer experienceYears;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;

    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Phí khám phải >= 0"
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Phí khám không hợp lệ"
    )
    private BigDecimal consultationFee;
}