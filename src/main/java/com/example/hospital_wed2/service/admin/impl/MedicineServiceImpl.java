package com.example.hospital_wed2.service.admin.impl;

import com.example.hospital_wed2.dto.admin.MedicineDto;
import com.example.hospital_wed2.entity.Medicine;
import com.example.hospital_wed2.repository.MedicineRepository;
import com.example.hospital_wed2.service.admin.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Đánh dấu đây là class Service để Spring quản lý
@Service

// Lombok tự tạo constructor cho các field final
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    // Repository dùng để thao tác với database bảng Medicine
    private final MedicineRepository medicineRepository;

    // =====================================================
    // LẤY TOÀN BỘ THUỐC
    // =====================================================

    public List<MedicineDto> findAll() {

        // Lấy toàn bộ thuốc từ DB
        return medicineRepository.findAll()

                // Chuyển từng entity sang DTO
                .stream()

                // map từng Medicine -> MedicineDto
                .map(this::toDTO)

                // Chuyển stream thành List
                .toList();
    }

    // =====================================================
    // TÌM THUỐC THEO ID
    // =====================================================

    public MedicineDto findById(Long id) {

        // Tìm thuốc theo id
        return medicineRepository.findById(id)

                // Nếu tìm thấy thì chuyển sang DTO
                .map(this::toDTO)

                // Nếu không tìm thấy thì báo lỗi
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy thuốc"));
    }

    // =====================================================
    // THÊM / CẬP NHẬT THUỐC
    // =====================================================

    @Override

    // Đảm bảo xử lý database theo transaction
    @Transactional
    public MedicineDto save(MedicineDto dto) {

        // =================================================
        // KIỂM TRA TÊN THUỐC BỊ TRÙNG
        // =================================================

        // Nếu có id => đang edit thuốc
        if (dto.getId() != null) {

            // Kiểm tra tên thuốc trùng nhưng bỏ qua chính nó
            if (medicineRepository.existsByNameIgnoreCaseAndIdNot(
                    dto.getName(),
                    dto.getId()
            )) {

                // Báo lỗi nếu tên bị trùng
                throw new RuntimeException(
                        "Tên thuốc \"" + dto.getName() +
                                "\" đã tồn tại trong hệ thống"
                );
            }

        } else {

            // Nếu không có id => đang thêm mới
            if (medicineRepository.existsByNameIgnoreCase(
                    dto.getName()
            )) {

                // Báo lỗi nếu tên thuốc đã tồn tại
                throw new RuntimeException(
                        "Tên thuốc \"" + dto.getName() +
                                "\" đã tồn tại trong hệ thống"
                );
            }
        }

        // =================================================
        // CHUYỂN DTO -> ENTITY
        // =================================================

        Medicine medicine = toEntity(dto);

        // =================================================
        // LƯU DATABASE
        // =================================================

        Medicine saved = medicineRepository.save(medicine);

        // =================================================
        // TRẢ VỀ DTO
        // =================================================

        return toDTO(saved);
    }

    // =====================================================
    // KHOÁ / MỞ KHOÁ THUỐC
    // =====================================================

    // FIX:
    // Dùng Boolean.TRUE.equals để tránh NullPointerException
    public void disable(Long id) {

        // Tìm thuốc theo id
        Medicine medicine = medicineRepository.findById(id)

                // Không tìm thấy thì báo lỗi
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy thuốc"));

        // Lấy trạng thái hiện tại
        Boolean current = medicine.getActive();

        // Đảo trạng thái true <-> false
        medicine.setActive(!Boolean.TRUE.equals(current));

        // Lưu lại DB
        medicineRepository.save(medicine);
    }

    // =====================================================
    // TÌM THUỐC THEO TÊN
    // =====================================================

    @Override
    public List<MedicineDto> findByName(String name) {

        // Tìm thuốc chứa keyword, không phân biệt hoa thường
        return medicineRepository.findByNameContainingIgnoreCase(name)

                // Chuyển sang stream
                .stream()

                // Entity -> DTO
                .map(this::toDTO)

                // Chuyển thành list
                .toList();
    }

    // =====================================================
    // TÌM THEO TÊN + TRẠNG THÁI
    // =====================================================

    @Override
    public List<MedicineDto> findByNameAndStatus(
            String keyword,
            Boolean active
    ) {

        // Tìm theo keyword + trạng thái active
        return medicineRepository
                .findByNameContainingIgnoreCaseAndIsActive(
                        keyword,
                        active
                )

                // Stream dữ liệu
                .stream()

                // Entity -> DTO
                .map(this::toDTO)

                // Trả về list
                .toList();
    }

    // =====================================================
    // TÌM THEO TRẠNG THÁI
    // =====================================================

    public List<MedicineDto> findByStatus(Boolean active) {

        // Tìm toàn bộ thuốc theo trạng thái
        return medicineRepository.findByIsActive(active)

                // Stream dữ liệu
                .stream()

                // Chuyển entity -> DTO
                .map(this::toDTO)

                // Trả về list
                .toList();
    }

    // =====================================================
    // ENTITY -> DTO
    // =====================================================

    // Mapper dùng để chuyển Medicine thành MedicineDto
    private MedicineDto toDTO(Medicine medicine) {

        return new MedicineDto(

                // ID thuốc
                medicine.getId(),

                // Tên thuốc
                medicine.getName(),

                // Thành phần thuốc
                medicine.getIngredient(),

                // Đơn vị tính
                medicine.getUnit(),

                // Giá thuốc
                medicine.getPrice(),

                // Số lượng tồn kho
                medicine.getStockQuantity(),

                // Nhà sản xuất
                medicine.getManufacturer(),

                // Mô tả thuốc
                medicine.getDescription(),

                // Trạng thái hoạt động
                medicine.getActive()
        );
    }

    // =====================================================
    // DTO -> ENTITY
    // =====================================================

    // Mapper dùng để chuyển DTO thành entity
    private Medicine toEntity(MedicineDto dto) {

        // Tạo object Medicine mới
        Medicine medicine = new Medicine();

        // Gán id
        medicine.setId(dto.getId());

        // Gán tên thuốc
        medicine.setName(dto.getName());

        // Gán thành phần thuốc
        medicine.setIngredient(dto.getIngredient());

        // Gán đơn vị tính
        medicine.setUnit(dto.getUnit());

        // Gán nhà sản xuất
        medicine.setManufacturer(dto.getManufacturer());

        // Gán mô tả
        medicine.setDescription(dto.getDescription());

        // Nếu price null thì mặc định = 0.0
        medicine.setPrice(
                dto.getPrice() != null
                        ? dto.getPrice()
                        : 0.0
        );

        // Nếu stock null thì mặc định = 0
        medicine.setStockQuantity(
                dto.getStockQuantity() != null
                        ? dto.getStockQuantity()
                        : 0
        );

        // Nếu active null thì mặc định = true
        medicine.setActive(
                dto.getActive() != null
                        ? dto.getActive()
                        : true
        );

        // Trả về entity
        return medicine;
    }
}