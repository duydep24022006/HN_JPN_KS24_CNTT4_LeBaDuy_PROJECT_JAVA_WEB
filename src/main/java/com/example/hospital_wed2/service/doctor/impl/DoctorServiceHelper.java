package com.example.hospital_wed2.service.doctor.impl;

import com.example.hospital_wed2.entity.Doctor;
import com.example.hospital_wed2.entity.User;
import com.example.hospital_wed2.exception.ResourceNotFoundException;
import com.example.hospital_wed2.repository.DoctorRepository;
import com.example.hospital_wed2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DoctorServiceHelper {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    public Doctor getDoctorByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + email));

        return doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin bác sĩ"));
    }
}