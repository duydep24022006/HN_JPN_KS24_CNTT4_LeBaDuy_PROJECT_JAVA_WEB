package com.example.hospital_wed2.validator;

import com.example.hospital_wed2.dto.auth.RegisterDTO;
import com.example.hospital_wed2.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueRegisterValidator implements ConstraintValidator<UniqueRegister, RegisterDTO> {

    private final UserRepository userRepository;

    @Override
    public boolean isValid(RegisterDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        boolean valid = true;

        // Kiểm tra username tồn tại
        if (dto.getUsername() != null && userRepository.existsByUsername(dto.getUsername())) {
            context.buildConstraintViolationWithTemplate("Tên đăng nhập đã tồn tại")
                    .addPropertyNode("username")
                    .addConstraintViolation();
            valid = false;
        }

        // Kiểm tra email tồn tại
        if (dto.getEmail() != null && userRepository.existsByEmail(dto.getEmail())) {
            context.buildConstraintViolationWithTemplate("Email đã được sử dụng")
                    .addPropertyNode("email")
                    .addConstraintViolation();
            valid = false;
        }

        if (dto.getPassword() != null && dto.getConfirmPassword() != null
                && !dto.getPassword().equals(dto.getConfirmPassword())) {
            context.buildConstraintViolationWithTemplate("Mật khẩu xác nhận không khớp")
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
