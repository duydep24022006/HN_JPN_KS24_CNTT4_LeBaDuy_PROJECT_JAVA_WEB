package com.example.hospital_wed2.controller.admin;

import com.example.hospital_wed2.dto.admin.MedicineDto;
import com.example.hospital_wed2.service.admin.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/medicines")
@RequiredArgsConstructor
public class AdminMedicinesController {

    private final MedicineService medicineService;

    @GetMapping
    public String listMedicines(@RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam(value = "active", required = false) Boolean active,
                                Model model) {

        if (keyword != null && !keyword.isBlank() && active != null) {
            model.addAttribute("medicines", medicineService.findByNameAndStatus(keyword, active));
        } else if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("medicines", medicineService.findByName(keyword));
        } else if (active != null) {
            model.addAttribute("medicines", medicineService.findByStatus(active));
        } else {
            model.addAttribute("medicines", medicineService.findAll());
        }

        if (!model.containsAttribute("medicineDTO")) {
            model.addAttribute("medicineDTO", new MedicineDto());
        }
        return "admin/medicines";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        MedicineDto dto = new MedicineDto();
        dto.setActive(true);
        model.addAttribute("medicineDTO", dto);
        return "admin/medicine-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("medicineDTO") MedicineDto medicineDTO,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/medicine-form";
        }
        try {
            medicineService.save(medicineDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    medicineDTO.getId() != null ? "Cập nhật thuốc thành công!" : "Thêm thuốc thành công!");
            return "redirect:/admin/medicines";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/medicine-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        MedicineDto dto = medicineService.findById(id);
        model.addAttribute("medicineDTO", dto);
        return "admin/medicine-form";
    }

    // FIX: Đổi từ @GetMapping sang @PostMapping để tránh accidental trigger và CSRF issues
    @PostMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            medicineService.disable(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái thuốc");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/medicines";
    }
}
