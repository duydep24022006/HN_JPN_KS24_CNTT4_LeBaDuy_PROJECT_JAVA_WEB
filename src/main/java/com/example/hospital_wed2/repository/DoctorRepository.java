package com.example.hospital_wed2.repository;

import com.example.hospital_wed2.entity.Doctor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // Tìm bác sĩ thông qua userId trong bảng users
    // Trả về Optional để tránh lỗi null nếu không tìm thấy
    Optional<Doctor> findByUserId(Long userId);

    // Kiểm tra số giấy phép hành nghề đã tồn tại chưa
    // Dùng khi thêm mới bác sĩ để tránh trùng license
    boolean existsByLicenseNumber(String licenseNumber);

    // Kiểm tra license đã tồn tại nhưng bỏ qua doctor hiện tại
    // Thường dùng khi update để không bị trùng chính nó
    boolean existsByLicenseNumberAndIdNot(String licenseNumber, Long doctorId);

    // Override findAll để tự động load thêm user, profile, specialty
    // Giúp tránh lỗi LazyInitialization và giảm query N+1
    @Override
    @EntityGraph(attributePaths = {
            "user",
            "user.profile",
            "specialty"
    })
    List<Doctor> findAll();

    // Override findById để lấy đầy đủ dữ liệu liên quan của bác sĩ
    // Tự động join user + profile + specialty
    @Override
    @EntityGraph(attributePaths = {
            "user",
            "user.profile",
            "specialty"
    })
    Optional<Doctor> findById(Long id);

    // Query tìm kiếm bác sĩ nâng cao theo nhiều điều kiện
    // Có thể filter theo tên, email, chuyên khoa, trạng thái, giới tính
    @Query("SELECT d FROM Doctor d JOIN FETCH d.user u LEFT JOIN FETCH u.profile p JOIN FETCH d.specialty s " +
            "WHERE (:keyword IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%',:keyword,'%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%',:keyword,'%'))) " +
            "AND (:specialtyId IS NULL OR s.id = :specialtyId) " +
            "AND (:active IS NULL OR u.active = :active) " +
            "AND (:gender IS NULL OR p.gender = :gender) " +
            "ORDER BY p.fullName ASC")
    List<Doctor> searchDoctors(

            // Từ khóa tìm theo tên hoặc email
            @Param("keyword") String keyword,

            // Lọc theo chuyên khoa
            @Param("specialtyId") Long specialtyId,

            // Lọc trạng thái hoạt động của tài khoản
            @Param("active") Boolean active,

            // Lọc theo giới tính
            @Param("gender") com.example.hospital_wed2.entity.Gender gender
    );
}
