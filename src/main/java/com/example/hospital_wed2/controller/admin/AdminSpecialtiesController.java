package com.example.hospital_wed2.controller.admin;

import com.example.hospital_wed2.dto.admin.SpecialtyDto;
import com.example.hospital_wed2.service.admin.SpecialtyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/specialties")
@RequiredArgsConstructor
public class AdminSpecialtiesController {

    private final SpecialtyService specialtyService;

    // ================= LIST + SEARCH =================

    @GetMapping
    public String listSpecialties(
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("specialties", specialtyService.searchSpecialties(keyword));
        } else {
            model.addAttribute("specialties", specialtyService.findAllSpecialties());
        }
        model.addAttribute("keyword", keyword);
        return "admin/specialties";
    }

    // ================= CREATE FORM =================

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("specialty", new SpecialtyDto());
        return "admin/specialty-form";
    }

    // ================= EDIT FORM =================

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("specialty", specialtyService.findById(id));
        return "admin/specialty-form";
    }

    // ================= SAVE =================

    @PostMapping("/save")
    public String saveSpecialty(
            @Valid @ModelAttribute("specialty") SpecialtyDto dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "admin/specialty-form";
        }

        try {
            specialtyService.saveSpecialty(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    dto.getId() != null ? "Cập nhật chuyên khoa thành công!" : "Thêm chuyên khoa thành công!");
            return "redirect:/admin/specialties";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/specialty-form";
        }
    }

    // ================= DELETE =================

    @PostMapping("/delete/{id}")
    public String deleteSpecialty(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            specialtyService.deleteSpecialty(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa chuyên khoa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/specialties";
    }
}
