package com.example.hospital_wed2.controller.admin;

import com.example.hospital_wed2.dto.admin.SpecialtyDto;
import com.example.hospital_wed2.service.admin.AdminSpecialtyService;
import com.example.hospital_wed2.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/specialties")
@RequiredArgsConstructor
public class AdminSpecialtiesController {

    private final AdminSpecialtyService specialtyService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public String listSpecialties(
            @RequestParam(required = false) String keyword,
            Model model,
            HttpServletRequest request) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("specialties", specialtyService.search(keyword));
        } else {
            model.addAttribute("specialties", specialtyService.findAll());
        }
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentUri", request.getRequestURI());
        return "admin/specialties";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("specialty", new SpecialtyDto());
        return "admin/specialty-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("specialty", specialtyService.findById(id));
        return "admin/specialty-form";
    }

    @PostMapping("/save")
    public String saveSpecialty(
            @Valid @ModelAttribute("specialty") SpecialtyDto dto,
            BindingResult result,
            @RequestParam(required = false) MultipartFile imageFile,
            RedirectAttributes ra) {

        if (result.hasErrors()) {
            return "admin/specialty-form";
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String fileName = fileStorageService.storeFile(imageFile);
                dto.setImageUrl("/uploads/" + fileName);   // hoặc set vào DTO nếu cần
            } catch (Exception e) {
                ra.addFlashAttribute("errorMessage", "Upload ảnh thất bại: " + e.getMessage());
                return "redirect:/admin/specialties";
            }
        }

        try {
            specialtyService.save(dto);
            ra.addFlashAttribute("successMessage",
                    dto.getId() != null ? "Cập nhật chuyên khoa thành công!" : "Thêm chuyên khoa thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/specialties";
    }

    @PostMapping({"/{id}/delete", "/delete/{id}"})
    public String deleteSpecialty(@PathVariable Long id, RedirectAttributes ra) {
        try {
            specialtyService.delete(id);
            ra.addFlashAttribute("successMessage", "Xóa chuyên khoa thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/specialties";
    }
}