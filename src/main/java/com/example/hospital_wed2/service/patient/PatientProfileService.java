package com.example.hospital_wed2.service.patient;

import com.example.hospital_wed2.dto.profile.shared.ChangePasswordRequest;
import com.example.hospital_wed2.dto.profile.shared.UpdateProfileRequest;
import com.example.hospital_wed2.dto.profile.shared.UserProfileResponse;

public interface PatientProfileService {

    UserProfileResponse getMyProfile(String email);

    UserProfileResponse updateMyProfile(
            String email,
            UpdateProfileRequest request
    );

    void changePassword(
            String email,
            ChangePasswordRequest request
    );
}