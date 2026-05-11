package com.example.hospital_wed2.service.admin.impl;

import com.example.hospital_wed2.dto.admin.SpecialtyDto;
import com.example.hospital_wed2.entity.Specialty;
import com.example.hospital_wed2.repository.SpecialtyRepository;
import com.example.hospital_wed2.service.FileStorageService;
import com.example.hospital_wed2.service.admin.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Đánh dấu đây là class Service để Spring quản lý
@Service

// Lombok tự tạo constructor cho các field final
@RequiredArgsConstructor

// Các thao tác DB sẽ chạy trong transaction
@Transactional
public class SpecialtyServiceImpl implements SpecialtyService {

    // Repository thao tác bảng Specialty
    private final SpecialtyRepository specialtyRepository;

    // Service dùng để upload/lưu file ảnh
    private final FileStorageService fileStorageService;

    // =====================================================
    // LẤY TOÀN BỘ CHUYÊN KHOA
    // =====================================================

    @Override
    public List<SpecialtyDto> findAllSpecialties() {

        // Lấy tất cả chuyên khoa từ DB
        return specialtyRepository.findAll()

                // Chuyển list thành stream
                .stream()

                // Chuyển entity -> DTO
                .map(this::toDTO)

                // Chuyển stream thành list
                .toList();
    }

    // =====================================================
    // TÌM KIẾM CHUYÊN KHOA THEO TÊN
    // =====================================================

    @Override
    public List<SpecialtyDto> searchSpecialties(String keyword) {

        // Tìm chuyên khoa chứa keyword
        return specialtyRepository.findByNameContainingIgnoreCase(keyword)

                // Stream dữ liệu
                .stream()

                // Entity -> DTO
                .map(this::toDTO)

                // Trả về list
                .toList();
    }

    // =====================================================
    // TÌM CHUYÊN KHOA THEO ID
    // =====================================================

    @Override
    public SpecialtyDto findById(Long id) {

        // Tìm chuyên khoa theo id
        return specialtyRepository.findById(id)

                // Nếu có thì chuyển sang DTO
                .map(this::toDTO)

                // Không tìm thấy thì báo lỗi
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy chuyên khoa"));
    }

    // =====================================================
    // THÊM / CẬP NHẬT CHUYÊN KHOA
    // =====================================================

    @Override
    public void saveSpecialty(SpecialtyDto dto) {

        // Nếu có id => đang update
        if (dto.getId() != null) {

            // =================================================
            // UPDATE
            // =================================================

            // Tìm chuyên khoa hiện tại
            Specialty existing = specialtyRepository.findById(dto.getId())

                    // Không tìm thấy thì báo lỗi
                    .orElseThrow(() ->
                            new RuntimeException("Không tìm thấy chuyên khoa"));

            // Kiểm tra tên chuyên khoa trùng
            if (specialtyRepository.existsByNameAndIdNot(
                    dto.getName(),
                    dto.getId()
            )) {

                // Báo lỗi nếu tên đã tồn tại
                throw new RuntimeException("Tên chuyên khoa đã tồn tại");
            }

            // Cập nhật tên chuyên khoa
            existing.setName(dto.getName());

            // Cập nhật mô tả
            existing.setDescription(dto.getDescription());

            // =================================================
            // UPLOAD ẢNH MỚI
            // =================================================

            // Nếu có file ảnh mới
            if (dto.getImageFile() != null &&
                    !dto.getImageFile().isEmpty()) {

                // Lưu file vào server
                String fileName =
                        fileStorageService.storeFile(dto.getImageFile());

                // Lưu đường dẫn ảnh
                existing.setImageUrl("/uploads/" + fileName);
            }

        } else {

            // =================================================
            // CREATE
            // =================================================

            // Kiểm tra tên chuyên khoa bị trùng
            if (specialtyRepository.existsByName(dto.getName())) {

                // Báo lỗi nếu đã tồn tại
                throw new RuntimeException("Tên chuyên khoa đã tồn tại");
            }

            // Tạo object Specialty mới
            Specialty specialty = new Specialty();

            // Gán tên chuyên khoa
            specialty.setName(dto.getName());

            // Gán mô tả
            specialty.setDescription(dto.getDescription());

            // =================================================
            // UPLOAD ẢNH
            // =================================================

            // Nếu có file ảnh
            if (dto.getImageFile() != null &&
                    !dto.getImageFile().isEmpty()) {

                // Lưu file vào server
                String fileName =
                        fileStorageService.storeFile(dto.getImageFile());

                // Lưu đường dẫn ảnh
                specialty.setImageUrl("/uploads/" + fileName);
            }

            // Lưu chuyên khoa vào DB
            specialtyRepository.save(specialty);
        }
    }

    // =====================================================
    // XÓA CHUYÊN KHOA
    // =====================================================

    @Override
    public void deleteSpecialty(Long id) {

        // Tìm chuyên khoa theo id
        Specialty specialty = specialtyRepository.findById(id)

                // Không tìm thấy thì báo lỗi
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy chuyên khoa"));

        // =================================================
        // KIỂM TRA CHUYÊN KHOA ĐANG CÓ BÁC SĨ
        // =================================================

        // Nếu danh sách bác sĩ không rỗng
        if (!specialty.getDoctors().isEmpty()) {

            // Không cho phép xóa
            throw new RuntimeException(
                    "Không thể xóa chuyên khoa đang có bác sĩ"
            );
        }

        // Xóa chuyên khoa khỏi DB
        specialtyRepository.delete(specialty);
    }

    // =====================================================
    // ENTITY -> DTO
    // =====================================================

    // Chuyển Specialty entity thành SpecialtyDto
    private SpecialtyDto toDTO(Specialty specialty) {

        // Tạo DTO mới
        SpecialtyDto dto = new SpecialtyDto();

        // Gán id
        dto.setId(specialty.getId());

        // Gán tên chuyên khoa
        dto.setName(specialty.getName());

        // Gán mô tả
        dto.setDescription(specialty.getDescription());

        // Gán đường dẫn ảnh
        dto.setImageUrl(specialty.getImageUrl());

        // Gán số lượng bác sĩ thuộc chuyên khoa
        dto.setDoctorCount(
                specialty.getDoctors() != null
                        ? specialty.getDoctors().size()
                        : 0
        );

        // Trả về DTO
        return dto;
    }
}