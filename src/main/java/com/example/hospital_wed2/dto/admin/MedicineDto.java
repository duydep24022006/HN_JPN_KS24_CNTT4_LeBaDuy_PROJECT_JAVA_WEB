package com.example.hospital_wed2.dto.admin;

import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineDto {

    private Long id;

    @NotBlank(message = "Tên thuốc không được để trống")
    @Size(max = 255, message = "Tên thuốc không được vượt quá 255 ký tự")
    private String name;

    @NotBlank(message = "Hoạt chất thuốc không được để trống")
    @Size(max = 255, message = "Hoạt chất không được vượt quá 255 ký tự")
    private String ingredient;

    @NotBlank(message = "Đơn vị không được để trống")
    @Size(max = 50, message = "Đơn vị không được vượt quá 50 ký tự")
    private String unit;

    @NotNull(message = "Giá thuốc không được để trống")
    @Positive(message = "Giá thuốc phải lớn hơn 0")
    private Double price;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stockQuantity;

    @NotBlank(message = "Tên nhà sản xuất không được để trống")
    @Size(max = 255, message = "Tên nhà sản xuất không được vượt quá 255 ký tự")
    private String manufacturer;

    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String description;

    @NotNull(message = "Trạng thái không được để trống")
    private Boolean active;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}

