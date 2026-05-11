package com.example.hospital_wed2.dto.profile;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class UpdateDoctorProfileRequest {
    private Long specialtyId;

    @NotBlank(message = "Số giấy phép hành nghề không được để trống")
    private String licenseNumber;

    @Min(value = 0, message = "Số năm kinh nghiệm không hợp lệ")
    @Max(value = 60, message = "Số năm kinh nghiệm không hợp lệ")
    private Integer experienceYears;

    @Size(max = 2000, message = "Mô tả không quá 2000 ký tự")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Phí khám phải lớn hơn 0")
    private BigDecimal consultationFee;

    // Getters & Setters
    public Long getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(Long specialtyId) {
        this.specialtyId = specialtyId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }
}
