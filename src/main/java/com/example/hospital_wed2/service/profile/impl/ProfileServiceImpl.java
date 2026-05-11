package com.example.hospital_wed2.service.profile.impl;

import com.example.hospital_wed2.dto.profile.*;
import com.example.hospital_wed2.entity.*;
import com.example.hospital_wed2.repository.DoctorRepository;
import com.example.hospital_wed2.repository.SpecialtyRepository;
import com.example.hospital_wed2.repository.UserProfileRepository;
import com.example.hospital_wed2.repository.UserRepository;
import com.example.hospital_wed2.service.profile.ProfileService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // Đánh dấu đây là Service để Spring quản lý
public class ProfileServiceImpl implements ProfileService {

    // Repository thao tác với bảng User
    private final UserRepository userRepository;

    // Repository thao tác với bảng UserProfile
    private final UserProfileRepository userProfileRepository;

    // Repository thao tác với bảng Doctor
    private final DoctorRepository doctorRepository;

    // Repository thao tác với bảng Specialty
    private final SpecialtyRepository specialtyRepository;

    // Dùng để mã hóa và kiểm tra mật khẩu
    private final PasswordEncoder passwordEncoder;

    // Constructor inject các dependency vào class
    public ProfileServiceImpl(UserRepository userRepository,
                              UserProfileRepository userProfileRepository,
                              DoctorRepository doctorRepository,
                              SpecialtyRepository specialtyRepository,
                              PasswordEncoder passwordEncoder) {

        // Gán UserRepository
        this.userRepository = userRepository;

        // Gán UserProfileRepository
        this.userProfileRepository = userProfileRepository;

        // Gán DoctorRepository
        this.doctorRepository = doctorRepository;

        // Gán SpecialtyRepository
        this.specialtyRepository = specialtyRepository;

        // Gán PasswordEncoder
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================================
    // CHUNG
    // =====================================================

    @Override
    @Transactional(readOnly = true) // Chỉ đọc dữ liệu
    public UserProfileResponse getMyProfile(String email) {

        // Tìm user theo email
        User user = findUserByEmail(email);

        // Lấy profile theo userId
        UserProfile profile = findProfileByUserId(user.getId());

        // Convert entity sang response DTO
        return mapToUserProfileResponse(user, profile);
    }

    @Override
    @Transactional // Có update dữ liệu
    public UserProfileResponse updateMyProfile(
            String email,
            UpdateProfileRequest request
    ) {

        // Tìm user theo email
        User user = findUserByEmail(email);

        // Tìm profile theo userId
        UserProfile profile = findProfileByUserId(user.getId());

        // Cập nhật họ tên
        profile.setFullName(request.getFullName());

        // Cập nhật số điện thoại
        profile.setPhoneNumber(request.getPhoneNumber());

        // Cập nhật ngày sinh
        profile.setDateOfBirth(request.getDateOfBirth());

        // Cập nhật giới tính
        profile.setGender(request.getGender());

        // Cập nhật địa chỉ
        profile.setAddress(request.getAddress());

        // Cập nhật CCCD/CMND
        profile.setIdentityCard(request.getIdentityCard());

        // Nếu có avatar mới thì cập nhật
        if (request.getAvatarUrl() != null) {

            // Set avatar mới
            profile.setAvatarUrl(request.getAvatarUrl());
        }

        // Lưu profile vào DB
        userProfileRepository.save(profile);

        // Trả dữ liệu profile mới
        return mapToUserProfileResponse(user, profile);
    }

    @Override
    @Transactional // Có update mật khẩu
    public void changePassword(
            String email,
            ChangePasswordRequest request
    ) {

        // Tìm user theo email
        User user = findUserByEmail(email);

        // Kiểm tra mật khẩu hiện tại có đúng không
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
        )) {

            // Báo lỗi nếu sai mật khẩu cũ
            throw new IllegalArgumentException(
                    "Mật khẩu hiện tại không đúng"
            );
        }

        // Kiểm tra xác nhận mật khẩu mới
        if (!request.getNewPassword()
                .equals(request.getConfirmNewPassword())) {

            // Báo lỗi nếu không khớp
            throw new IllegalArgumentException(
                    "Xác nhận mật khẩu mới không khớp"
            );
        }

        // Mã hóa mật khẩu mới
        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        // Lưu user mới
        userRepository.save(user);
    }

    // =====================================================
    // DOCTOR
    // =====================================================

    @Override
    @Transactional(readOnly = true) // Chỉ đọc dữ liệu
    public DoctorProfileResponse getDoctorProfile(String email) {

        // Tìm user theo email
        User user = findUserByEmail(email);

        // Kiểm tra role có phải DOCTOR không
        checkRole(user, Role.DOCTOR);

        // Tìm profile theo userId
        UserProfile profile = findProfileByUserId(user.getId());

        // Tìm doctor theo userId
        Doctor doctor = findDoctorByUserId(user.getId());

        // Convert sang DTO response
        return mapToDoctorProfileResponse(
                user,
                profile,
                doctor
        );
    }

    @Override
    @Transactional // Có update dữ liệu
    public DoctorProfileResponse updateDoctorProfile(
            String email,
            UpdateDoctorProfileRequest request
    ) {

        // Tìm user theo email
        User user = findUserByEmail(email);

        // Kiểm tra role doctor
        checkRole(user, Role.DOCTOR);

        // Lấy thông tin doctor
        Doctor doctor = findDoctorByUserId(user.getId());

        // Kiểm tra licenseNumber có bị trùng không
        if (doctorRepository.existsByLicenseNumberAndIdNot(
                request.getLicenseNumber(),
                doctor.getId()
        )) {

            // Báo lỗi nếu bị trùng
            throw new IllegalArgumentException(
                    "Số giấy phép hành nghề đã được sử dụng"
            );
        }

        // Tìm chuyên khoa theo id
        Specialty specialty = specialtyRepository
                .findById(request.getSpecialtyId())

                // Báo lỗi nếu không tồn tại
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Chuyên khoa không tồn tại"
                        )
                );

        // Cập nhật chuyên khoa
        doctor.setSpecialty(specialty);

        // Cập nhật license number
        doctor.setLicenseNumber(request.getLicenseNumber());

        // Cập nhật số năm kinh nghiệm
        doctor.setExperienceYears(request.getExperienceYears());

        // Cập nhật mô tả
        doctor.setDescription(request.getDescription());

        // Cập nhật phí khám
        doctor.setConsultationFee(request.getConsultationFee());

        // Lưu doctor mới
        doctorRepository.save(doctor);

        // Lấy profile của doctor
        UserProfile profile = findProfileByUserId(user.getId());

        // Nếu có avatar mới thì cập nhật
        if (request.getAvatarUrl() != null) {

            // Set avatar
            profile.setAvatarUrl(request.getAvatarUrl());

            // Lưu profile
            userProfileRepository.save(profile);
        }

        // Trả dữ liệu doctor profile mới
        return mapToDoctorProfileResponse(
                user,
                profile,
                doctor
        );
    }

    // =====================================================
    // ADMIN
    // =====================================================

    @Override
    @Transactional(readOnly = true) // Chỉ đọc dữ liệu
    public UserProfileResponse getProfileByUserId(Long userId) {

        // Tìm user theo id
        User user = userRepository.findById(userId)

                // Báo lỗi nếu không tồn tại
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy người dùng"
                        )
                );

        // Tìm profile theo userId
        UserProfile profile = findProfileByUserId(userId);

        // Convert sang DTO response
        return mapToUserProfileResponse(user, profile);
    }

    @Override
    @Transactional // Có update trạng thái
    public void toggleUserActive(Long userId) {

        // Tìm user theo id
        User user = userRepository.findById(userId)

                // Báo lỗi nếu không tồn tại
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy người dùng"
                        )
                );

        // Đảo trạng thái active
        user.setActive(!user.getActive());

        // Lưu user mới
        userRepository.save(user);
    }

    // =====================================================
    // HELPER
    // =====================================================

    // Hàm tìm user theo email
    private User findUserByEmail(String email) {

        // Query user theo email
        return userRepository.findByEmail(email)

                // Báo lỗi nếu không tìm thấy
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy người dùng: " + email
                        )
                );
    }

    // Hàm tìm profile theo userId
    private UserProfile findProfileByUserId(Long userId) {

        // Query profile theo userId
        return userProfileRepository.findByUserId(userId)

                // Báo lỗi nếu không có profile
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy thông tin hồ sơ"
                        )
                );
    }

    // Hàm tìm doctor theo userId
    private Doctor findDoctorByUserId(Long userId) {

        // Query doctor theo userId
        return doctorRepository.findByUserId(userId)

                // Báo lỗi nếu không tìm thấy doctor
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy thông tin bác sĩ"
                        )
                );
    }

    // Hàm kiểm tra role của user
    private void checkRole(User user, Role expectedRole) {

        // Nếu role không đúng
        if (user.getRole() != expectedRole) {

            // Báo lỗi không có quyền
            throw new SecurityException(
                    "Bạn không có quyền thực hiện thao tác này"
            );
        }
    }

    // Convert User + Profile -> UserProfileResponse
    private UserProfileResponse mapToUserProfileResponse(
            User user,
            UserProfile profile
    ) {

        // Tạo response object
        UserProfileResponse res = new UserProfileResponse();

        // Set userId
        res.setUserId(user.getId());

        // Set username
        res.setUsername(user.getUsername());

        // Set email
        res.setEmail(user.getEmail());

        // Set role
        res.setRole(user.getRole());

        // Set trạng thái active
        res.setActive(user.getActive());

        // Set ngày tạo tài khoản
        res.setCreatedAt(user.getCreatedAt());

        // Set họ tên
        res.setFullName(profile.getFullName());

        // Set số điện thoại
        res.setPhoneNumber(profile.getPhoneNumber());

        // Set ngày sinh
        res.setDateOfBirth(profile.getDateOfBirth());

        // Set giới tính
        res.setGender(profile.getGender());

        // Set địa chỉ
        res.setAddress(profile.getAddress());

        // Set CCCD/CMND
        res.setIdentityCard(profile.getIdentityCard());

        // Set avatar
        res.setAvatarUrl(profile.getAvatarUrl());

        // Trả response
        return res;
    }

    // Convert User + Profile + Doctor -> DoctorProfileResponse
    private DoctorProfileResponse mapToDoctorProfileResponse(
            User user,
            UserProfile profile,
            Doctor doctor
    ) {

        // Tạo response object
        DoctorProfileResponse res = new DoctorProfileResponse();

        // =====================================================
        // USER FIELDS
        // =====================================================

        // Set userId
        res.setUserId(user.getId());

        // Set username
        res.setUsername(user.getUsername());

        // Set email
        res.setEmail(user.getEmail());

        // Set role
        res.setRole(user.getRole());

        // Set active
        res.setActive(user.getActive());

        // Set ngày tạo
        res.setCreatedAt(user.getCreatedAt());

        // =====================================================
        // PROFILE FIELDS
        // =====================================================

        // Set họ tên
        res.setFullName(profile.getFullName());

        // Set số điện thoại
        res.setPhoneNumber(profile.getPhoneNumber());

        // Set ngày sinh
        res.setDateOfBirth(profile.getDateOfBirth());

        // Set giới tính
        res.setGender(profile.getGender());

        // Set địa chỉ
        res.setAddress(profile.getAddress());

        // Set CCCD/CMND
        res.setIdentityCard(profile.getIdentityCard());

        // Set avatar
        res.setAvatarUrl(profile.getAvatarUrl());

        // =====================================================
        // DOCTOR FIELDS
        // =====================================================

        // Set doctorId
        res.setDoctorId(doctor.getId());

        // Set specialtyId
        res.setSpecialtyId(doctor.getSpecialty().getId());

        // Set tên chuyên khoa
        res.setSpecialtyName(
                doctor.getSpecialty().getName()
        );

        // Set license number
        res.setLicenseNumber(
                doctor.getLicenseNumber()
        );

        // Set số năm kinh nghiệm
        res.setExperienceYears(
                doctor.getExperienceYears()
        );

        // Set mô tả
        res.setDescription(
                doctor.getDescription()
        );

        // Set phí khám
        res.setConsultationFee(
                doctor.getConsultationFee()
        );

        // Trả response
        return res;
    }
}