package com.example.hospital_wed2.service.doctor;

import com.example.hospital_wed2.dto.profile.doctor.DoctorProfileResponse;
import com.example.hospital_wed2.dto.profile.doctor.UpdateDoctorProfileRequest;
import com.example.hospital_wed2.dto.profile.shared.ChangePasswordRequest;

public interface DoctorProfileService {

    DoctorProfileResponse getDoctorProfile(String email);

    DoctorProfileResponse updateDoctorProfile(
            String email,
            UpdateDoctorProfileRequest request
    );
    void changePassword(String email, ChangePasswordRequest request);
}