package com.example.hospital_wed2.service.profile;

import com.example.hospital_wed2.dto.profile.*;

public interface ProfileService {
    // ===== CHUNG (PATIENT + ADMIN + DOCTOR) =====

    /**
     * Lấy thông tin profile của user đang đăng nhập
     */
    UserProfileResponse getMyProfile(String email);

    /**
     * Cập nhật thông tin cá nhân (họ tên, sdt, địa chỉ, ...)
     */
    UserProfileResponse updateMyProfile(String username, UpdateProfileRequest request);

    /**
     * Đổi mật khẩu
     */
    void changePassword(String username, ChangePasswordRequest request);


    // ===== DOCTOR =====

    /**
     * Lấy thông tin chuyên môn của bác sĩ đang đăng nhập
     */
    DoctorProfileResponse getDoctorProfile(String username);

    /**
     * Bác sĩ cập nhật thông tin chuyên môn (chuyên khoa, kinh nghiệm, phí khám, ...)
     */
    DoctorProfileResponse updateDoctorProfile(String username, UpdateDoctorProfileRequest request);


    // ===== ADMIN =====

    /**
     * Admin xem profile của bất kỳ user nào theo userId
     */
    UserProfileResponse getProfileByUserId(Long userId);

    /**
     * Admin kích hoạt / vô hiệu hoá tài khoản
     */
    void toggleUserActive(Long userId);
}
