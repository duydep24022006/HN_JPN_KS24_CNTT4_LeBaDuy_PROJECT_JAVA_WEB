package com.example.hospital_wed2.service.admin.impl;

import com.example.hospital_wed2.dto.admin.AdminDoctorDto;
import com.example.hospital_wed2.entity.*;
import com.example.hospital_wed2.repository.*;
import com.example.hospital_wed2.service.FileStorageService;
import com.example.hospital_wed2.service.admin.AdminDoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service // Đánh dấu đây là Service xử lý nghiệp vụ
@RequiredArgsConstructor // Lombok tự tạo constructor cho các biến final
@Transactional // Nếu lỗi xảy ra sẽ rollback transaction
public class AdminDoctorServiceImpl implements AdminDoctorService {

    // Repository thao tác bảng User
    private final UserRepository userRepository;

    // Repository thao tác bảng UserProfile
    private final UserProfileRepository profileRepository;

    // Repository thao tác bảng Doctor
    private final DoctorRepository doctorRepository;

    // Repository thao tác bảng Specialty
    private final SpecialtyRepository specialtyRepository;

    // Dùng mã hóa mật khẩu
    private final PasswordEncoder passwordEncoder;

    // Service upload/lưu file
    private final FileStorageService fileStorageService;

    // =====================================================
    // SAVE DOCTOR
    // =====================================================

    @Override
    public void saveDoctor(AdminDoctorDto dto) {

        // Nếu có id nghĩa là update bác sĩ cũ
        if (dto.getId() != null) {
            updateDoctor(dto);
            return;
        }

        // Nếu chưa có id thì tạo mới
        createDoctor(dto);
    }

    // =====================================================
    // CREATE
    // =====================================================

    private void createDoctor(AdminDoctorDto dto) {

        // Validate dữ liệu tạo mới
        validateCreate(dto);

        // Lấy chuyên khoa theo id
        Specialty specialty = getSpecialty(dto.getSpecialtyId());

        // Tạo tài khoản user
        User user = createUser(dto);

        // Tạo hồ sơ cá nhân
        UserProfile profile = createProfile(dto, user);

        // Tạo entity doctor
        Doctor doctor = createDoctorEntity(dto, user, specialty);

        // Gắn liên kết profile vào user
        user.setProfile(profile);

        // Gắn liên kết doctor vào user
        user.setDoctor(doctor);
    }

    // =====================================================
    // UPDATE
    // =====================================================

    private void updateDoctor(AdminDoctorDto dto) {

        // Tìm bác sĩ theo id
        Doctor doctor = doctorRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

        // Lấy user của bác sĩ
        User user = doctor.getUser();

        // Lấy profile của user
        UserProfile profile = user.getProfile();

        // Lấy chuyên khoa
        Specialty specialty = getSpecialty(dto.getSpecialtyId());

        // Validate email/phone/license khi update
        validateUpdate(dto, user.getId(), profile != null ? profile.getId() : null);

        // ================= USER =================

        // Cập nhật email
        user.setEmail(dto.getEmail());

        // Cập nhật trạng thái hoạt động
        user.setActive(dto.getActive());

        // ================= PROFILE =================

        // Nếu chưa có profile thì tạo mới
        if (profile == null) {
            profile = new UserProfile();

            // Gắn user cho profile
            profile.setUser(user);
        }

        // Update dữ liệu profile
        updateProfile(profile, dto);

        // Lưu profile
        profileRepository.save(profile);

        // ================= DOCTOR =================

        // Cập nhật license
        doctor.setLicenseNumber(dto.getLicenseNumber());

        // Cập nhật số năm kinh nghiệm
        doctor.setExperienceYears(dto.getExperienceYears());

        // Cập nhật mô tả
        doctor.setDescription(dto.getDescription());

        // Cập nhật phí khám
        doctor.setConsultationFee(dto.getConsultationFee());

        // Cập nhật chuyên khoa
        doctor.setSpecialty(specialty);
    }

    // =====================================================
    // VALIDATE CREATE
    // =====================================================

    private void validateCreate(AdminDoctorDto dto) {

        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng trong hệ thống");
        }

        // Nếu có nhập phone thì kiểm tra trùng
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {

            // Kiểm tra số điện thoại đã tồn tại chưa
            if (profileRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
                throw new RuntimeException("Số điện thoại này đã được sử dụng bởi tài khoản khác");
            }
        }

        // Kiểm tra giấy phép hành nghề có bị trùng không
        if (doctorRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new RuntimeException("Số giấy phép hành nghề này đã tồn tại trong hệ thống");
        }
    }

    // =====================================================
    // VALIDATE UPDATE
    // =====================================================

    private void validateUpdate(AdminDoctorDto dto, Long userId, Long profileId) {

        // Kiểm tra email trùng ngoại trừ user hiện tại
        if (userRepository.existsByEmailAndIdNot(dto.getEmail(), userId)) {
            throw new RuntimeException("Email này đã được sử dụng bởi tài khoản khác");
        }

        // Nếu có nhập phone thì kiểm tra trùng
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {

            // Nếu profile đã tồn tại
            if (profileId != null) {

                // Kiểm tra phone trùng ngoại trừ profile hiện tại
                if (profileRepository.existsByPhoneNumberAndIdNot(dto.getPhoneNumber(), profileId)) {
                    throw new RuntimeException("Số điện thoại này đã được sử dụng bởi tài khoản khác");
                }

            } else {

                // Nếu chưa có profile thì check bình thường
                if (profileRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
                    throw new RuntimeException("Số điện thoại này đã được sử dụng bởi tài khoản khác");
                }
            }
        }

        // Kiểm tra license trùng ngoại trừ doctor hiện tại
        if (doctorRepository.existsByLicenseNumberAndIdNot(dto.getLicenseNumber(), dto.getId())) {
            throw new RuntimeException("Số giấy phép hành nghề này đã tồn tại trong hệ thống");
        }
    }

    // =====================================================
    // CREATE USER
    // =====================================================

    private User createUser(AdminDoctorDto dto) {

        // Khởi tạo user mới
        User user = new User();

        // Lấy phần trước @ làm username
        String username = dto.getEmail().split("@")[0];

        // Lưu username gốc
        String baseUsername = username;

        // Biến tăng số thứ tự
        int suffix = 1;

        // Nếu username bị trùng thì thêm số phía sau
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix++;
        }

        // Set username
        user.setUsername(username);

        // Set email
        user.setEmail(dto.getEmail());

        // Set password mặc định đã mã hóa
        user.setPassword(passwordEncoder.encode("123456"));

        // Gán role DOCTOR
        user.setRole(Role.DOCTOR);

        // Nếu active null thì mặc định true
        user.setActive(dto.getActive() != null ? dto.getActive() : true);

        // Lưu user
        return userRepository.save(user);
    }

    // =====================================================
    // CREATE PROFILE
    // =====================================================

    private UserProfile createProfile(AdminDoctorDto dto, User user) {

        // Tạo profile mới
        UserProfile profile = new UserProfile();

        // Gắn user cho profile
        profile.setUser(user);

        // Đổ dữ liệu từ dto vào profile
        updateProfile(profile, dto);

        // Lưu profile
        return profileRepository.save(profile);
    }

    // =====================================================
    // CREATE DOCTOR
    // =====================================================

    private Doctor createDoctorEntity(AdminDoctorDto dto, User user, Specialty specialty) {

        // Tạo doctor mới
        Doctor doctor = new Doctor();

        // Gắn user
        doctor.setUser(user);

        // Gắn chuyên khoa
        doctor.setSpecialty(specialty);

        // Set license
        doctor.setLicenseNumber(dto.getLicenseNumber());

        // Set số năm kinh nghiệm
        doctor.setExperienceYears(dto.getExperienceYears());

        // Set mô tả
        doctor.setDescription(dto.getDescription());

        // Set phí khám
        doctor.setConsultationFee(dto.getConsultationFee());

        // Lưu doctor
        return doctorRepository.save(doctor);
    }

    // =====================================================
    // UPDATE PROFILE
    // =====================================================

    private void updateProfile(UserProfile profile, AdminDoctorDto dto) {

        // Cập nhật họ tên
        profile.setFullName(dto.getFullName());

        // Cập nhật số điện thoại
        profile.setPhoneNumber(dto.getPhoneNumber());

        // Cập nhật ngày sinh
        profile.setDateOfBirth(dto.getDateOfBirth());

        // Cập nhật giới tính
        profile.setGender(dto.getGender());

        // Cập nhật địa chỉ
        profile.setAddress(dto.getAddress());

        // Cập nhật CCCD/CMND
        profile.setIdentityCard(dto.getIdentityCard());

        // Upload avatar nếu có
        handleAvatarUpload(profile, dto);
    }

    // =====================================================
    // AVATAR
    // =====================================================

    private void handleAvatarUpload(UserProfile profile, AdminDoctorDto dto) {

        // Nếu không có file thì bỏ qua
        if (dto.getAvatarFile() == null || dto.getAvatarFile().isEmpty()) {
            return;
        }

        try {

            // Lưu file vào storage
            String fileName = fileStorageService.storeFile(dto.getAvatarFile());

            // Lưu tên file vào DB
            profile.setAvatarUrl(fileName);

        } catch (Exception e) {

            // Upload lỗi nhưng không làm dừng chương trình
        }
    }

    // =====================================================
    // SPECIALTY
    // =====================================================

    private Specialty getSpecialty(Long id) {

        // Tìm chuyên khoa theo id
        return specialtyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên khoa"));
    }

    // =====================================================
    // GET ALL
    // =====================================================

    @Override
    public List<AdminDoctorDto> getAllDoctors() {

        // Lấy toàn bộ doctor rồi map sang dto
        return doctorRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // =====================================================
    // SEARCH
    // =====================================================

    @Override
    public List<AdminDoctorDto> searchDoctors(
            String keyword,
            Long specialtyId,
            Boolean active,
            Gender gender
    ) {

        // Nếu keyword rỗng thì cho null
        String kw = (keyword != null && !keyword.isBlank()) ? keyword : null;

        return doctorRepository.findAll()
                .stream()

                // Lọc dữ liệu
                .filter(d -> {

                    UserProfile p = d.getUser().getProfile();
                    User u = d.getUser();

                    // ================= SEARCH KEYWORD =================
                    if (kw != null) {

                        String name = p != null ? p.getFullName() : "";
                        String email = u.getEmail() != null ? u.getEmail() : "";

                        // Nếu keyword không nằm trong name/email
                        if (!name.toLowerCase().contains(kw.toLowerCase()) &&
                                !email.toLowerCase().contains(kw.toLowerCase())) {
                            return false;
                        }
                    }

                    // ================= FILTER SPECIALTY =================
                    if (specialtyId != null &&
                            !d.getSpecialty().getId().equals(specialtyId)) {
                        return false;
                    }

                    // ================= FILTER ACTIVE =================
                    if (active != null &&
                            !u.getActive().equals(active)) {
                        return false;
                    }

                    // ================= FILTER GENDER =================
                    if (gender != null &&
                            (p == null || p.getGender() != gender)) {
                        return false;
                    }

                    return true;
                })

                // Map sang dto
                .map(this::mapToDto)

                // Convert thành list
                .toList();
    }

    // =====================================================
    // GET BY ID
    // =====================================================

    @Override
    public AdminDoctorDto getDoctorById(Long id) {

        // Tìm doctor theo id
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

        // Chuyển sang dto
        return mapToDto(doctor);
    }

    // =====================================================
    // DELETE
    // =====================================================

    @Override
    public void deleteDoctor(Long id) {

        // Tìm doctor
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

        // Soft delete bằng cách inactive user
        doctor.getUser().setActive(false);
    }

    // =====================================================
    // TOGGLE ACTIVE
    // =====================================================

    @Override
    public void toggleDoctor(Long id) {

        // Tìm doctor
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

        // Lấy trạng thái hiện tại
        boolean current = Boolean.TRUE.equals(doctor.getUser().getActive());

        // Đảo trạng thái active
        doctor.getUser().setActive(!current);
    }

    // =====================================================
    // MAP DTO
    // =====================================================

    private AdminDoctorDto mapToDto(Doctor doctor) {

        // Tạo dto mới
        AdminDoctorDto dto = new AdminDoctorDto();

        // Lấy user
        User user = doctor.getUser();

        // Lấy profile
        UserProfile profile = user.getProfile();

        // ================= USER =================

        dto.setId(doctor.getId());
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.getActive());

        // ================= PROFILE =================

        if (profile != null) {

            dto.setFullName(profile.getFullName());
            dto.setPhoneNumber(profile.getPhoneNumber());
            dto.setDateOfBirth(profile.getDateOfBirth());
            dto.setGender(profile.getGender());
            dto.setAddress(profile.getAddress());
            dto.setIdentityCard(profile.getIdentityCard());
            dto.setAvatarUrl(profile.getAvatarUrl());
        }

        // ================= DOCTOR =================

        dto.setSpecialtyId(doctor.getSpecialty().getId());
        dto.setSpecialtyName(doctor.getSpecialty().getName());
        dto.setLicenseNumber(doctor.getLicenseNumber());
        dto.setExperienceYears(doctor.getExperienceYears());
        dto.setDescription(doctor.getDescription());
        dto.setConsultationFee(doctor.getConsultationFee());

        // Trả dto
        return dto;
    }
}