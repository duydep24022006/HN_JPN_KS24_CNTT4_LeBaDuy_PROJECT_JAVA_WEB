package com.example.hospital_wed2.repository;

import com.example.hospital_wed2.entity.Doctor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumberAndIdNot(String licenseNumber, Long doctorId);

    @Override
    @EntityGraph(attributePaths = {
            "user",
            "user.profile",
            "specialty"
    })
    List<Doctor> findAll();

    @Override
    @EntityGraph(attributePaths = {
            "user",
            "user.profile",
            "specialty"
    })
    Optional<Doctor> findById(Long id);

    @Query("SELECT d FROM Doctor d JOIN FETCH d.user u LEFT JOIN FETCH u.profile p JOIN FETCH d.specialty s " +
            "WHERE (:keyword IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%',:keyword,'%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%',:keyword,'%'))) " +
            "AND (:specialtyId IS NULL OR s.id = :specialtyId) " +
            "AND (:active IS NULL OR u.active = :active) " +
            "AND (:gender IS NULL OR p.gender = :gender) " +
            "ORDER BY p.fullName ASC")
    List<Doctor> searchDoctors(
        @Param("keyword") String keyword,
        @Param("specialtyId") Long specialtyId,
        @Param("active") Boolean active,
        @Param("gender") com.example.hospital_wed2.entity.Gender gender
    );
}
