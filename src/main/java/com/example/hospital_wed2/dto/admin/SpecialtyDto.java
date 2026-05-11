package com.example.hospital_wed2.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SpecialtyDto {

    private Long id;

    @NotBlank(message = "Tên chuyên khoa không được để trống")
    @Size(max = 100, message = "Tên chuyên khoa tối đa 100 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;

    private String imageUrl;

    private MultipartFile imageFile;

    private int doctorCount;
}
