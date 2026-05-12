package com.example.hospital_wed2.controller.admin;

import com.example.hospital_wed2.dto.admin.MedicineDto;
import com.example.hospital_wed2.service.admin.AdminMedicineService;
import jakarta.servlet.http.HttpServletRequest;
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

    private final AdminMedicineService medicineService;

    @GetMapping
    public String listMedicines(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            Model model,
            HttpServletRequest request) {

        model.addAttribute("medicines", medicineService.search(keyword, active));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedActive", active);
        model.addAttribute("currentUri", request.getRequestURI());
        return "admin/medicines";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("medicineDTO", new MedicineDto());
        return "admin/medicine-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("medicineDTO", medicineService.findById(id));
        return "admin/medicine-form";
    }

    @PostMapping("/save")
    public String saveMedicine(
            @Valid @ModelAttribute("medicineDTO") MedicineDto dto,
            BindingResult result,
            RedirectAttributes ra) {

        if (result.hasErrors()) {
            return "admin/medicine-form";
        }

        try {
            medicineService.save(dto);
            ra.addFlashAttribute("successMessage",
                    dto.getId() != null ? "Cập nhật thuốc thành công!" : "Thêm thuốc thành công!");
            return "redirect:/admin/medicines";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/medicines";
        }
    }

    @PostMapping({"/{id}/toggle", "/toggle/{id}"})
    public String toggleStatus(@PathVariable Long id, RedirectAttributes ra) {
        try {
            medicineService.toggleStatus(id);
            ra.addFlashAttribute("successMessage", "Đã thay đổi trạng thái thuốc");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/medicines";
    }

    @PostMapping({"/{id}/delete", "/delete/{id}"})
    public String deleteMedicine(@PathVariable Long id, RedirectAttributes ra) {
        try {
            medicineService.delete(id);
            ra.addFlashAttribute("successMessage", "Đã xóa thuốc");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/medicines";
    }
}