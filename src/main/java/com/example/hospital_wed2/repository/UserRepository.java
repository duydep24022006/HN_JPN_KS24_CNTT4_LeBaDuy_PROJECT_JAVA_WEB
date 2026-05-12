package com.example.hospital_wed2.repository;

import com.example.hospital_wed2.entity.Role;
import com.example.hospital_wed2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    // Tìm user theo email
    // Dùng khi đăng nhập hoặc lấy thông tin tài khoản
    Optional<User> findByEmail(String email);

    // Kiểm tra username đã tồn tại chưa
    // Dùng khi đăng ký tài khoản mới
    boolean existsByUsername(String username);

    // Kiểm tra email đã tồn tại chưa
    // Tránh tạo nhiều tài khoản cùng email
    boolean existsByEmail(String email);

    // Kiểm tra số điện thoại đã được dùng chưa
    // Join UserProfile để tìm phoneNumber
    @Query("SELECT COUNT(u) > 0 FROM UserProfile p JOIN p.user u WHERE p.phoneNumber = :phone")
    boolean existsByPhoneNumber(String phone);

    // Kiểm tra phone đã tồn tại nhưng bỏ qua user hiện tại
    // Dùng khi update profile để tránh trùng số điện thoại
    @Query("SELECT COUNT(u) > 0 FROM UserProfile p JOIN p.user u WHERE p.phoneNumber = :phone AND u.id <> :userId")
    boolean existsByPhoneNumberAndUserIdNot(
            @Param("phone") String phone,
            @Param("userId") Long userId
    );

    // Kiểm tra email đã tồn tại nhưng không tính user hiện tại
    // Thường dùng trong chức năng cập nhật user
    boolean existsByEmailAndIdNot(String email, Long id);

    // Tìm kiếm user theo role, trạng thái active và keyword
    // Keyword tìm theo email, username hoặc fullName
    @Query("""
    SELECT u FROM User u
    LEFT JOIN FETCH u.profile
    WHERE u.role <> com.example.hospital_wed2.entity.Role.ADMIN
    AND (:role IS NULL OR u.role = :role)
    AND (:active IS NULL OR u.active = :active)
    AND (
        :keyword IS NULL
        OR u.email LIKE %:keyword%
        OR u.username LIKE %:keyword%
        OR u.profile.fullName LIKE %:keyword%
    )
""")
    List<User> searchUsers(
            @Param("role") Role role,
            @Param("active") Boolean active,
            @Param("keyword") String keyword
    );

    // Lấy toàn bộ user ngoại trừ ADMIN kèm profile
    // Dùng cho trang quản lý người dùng
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.role <> 'ADMIN' ORDER BY u.createdAt DESC")
    List<User> findAllWithProfile();

    // Lấy 5 user mới tạo gần đây nhất
    // Dùng cho dashboard hoặc thống kê nhanh
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.createdAt IS NOT NULL ORDER BY u.createdAt DESC LIMIT 5")
    List<User> findTop5ByOrderByCreatedAtDesc();

    long countByActiveTrue();

    long countByRole(com.example.hospital_wed2.entity.Role role);
}
