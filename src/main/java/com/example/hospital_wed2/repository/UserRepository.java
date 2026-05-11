package com.example.hospital_wed2.repository;

import com.example.hospital_wed2.entity.Role;
import com.example.hospital_wed2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM UserProfile p JOIN p.user u WHERE p.phoneNumber = :phone")
    boolean existsByPhoneNumber(String phone);

    @Query("SELECT COUNT(u) > 0 FROM UserProfile p JOIN p.user u WHERE p.phoneNumber = :phone AND u.id <> :userId")
    boolean existsByPhoneNumberAndUserIdNot(@Param("phone") String phone, @Param("userId") Long userId);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE (:role IS NULL OR u.role = :role) AND (:active IS NULL OR u.active = :active) AND (:keyword IS NULL OR u.email LIKE %:keyword% OR u.username LIKE %:keyword% OR u.profile.fullName LIKE %:keyword%)")
    List<User> searchUsers(@Param("role") Role role, @Param("active") Boolean active, @Param("keyword") String keyword);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.role <> 'ADMIN' ORDER BY u.createdAt DESC")
    List<User> findAllWithProfile();

    // BUG-08: Top 5 gần đây cho dashboard
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.createdAt IS NOT NULL ORDER BY u.createdAt DESC LIMIT 5")
    List<User> findTop5ByOrderByCreatedAtDesc();
}
