package com.example.hospital_wed2.repository;

import com.example.hospital_wed2.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile,Long> {
    Optional<UserProfile> findByUserId(Long userId);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByUserId(Long userId);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);
}
