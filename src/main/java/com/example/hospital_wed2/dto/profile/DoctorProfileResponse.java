package com.example.hospital_wed2.dto.profile;

import java.math.BigDecimal;

public class DoctorProfileResponse extends UserProfileResponse {
    private Long doctorId;
    private Long specialtyId;
    private String specialtyName;
    private String licenseNumber;
    private Integer experienceYears;
    private String description;
    private BigDecimal consultationFee;

    // Getters & Setters
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getSpecialtyId() { return specialtyId; }
    public void setSpecialtyId(Long specialtyId) { this.specialtyId = specialtyId; }

    public String getSpecialtyName() { return specialtyName; }
    public void setSpecialtyName(String specialtyName) { this.specialtyName = specialtyName; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }
}
