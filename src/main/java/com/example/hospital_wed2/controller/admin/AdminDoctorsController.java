package com.example.hospital_wed2.controller.admin;

import com.example.hospital_wed2.dto.admin.AdminDoctorDto;
import com.example.hospital_wed2.entity.Gender;
import com.example.hospital_wed2.service.admin.DoctorService;
import com.example.hospital_wed2.service.admin.SpecialtyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping({"/admin/doctors"})
@RequiredArgsConstructor
public class AdminDoctorsController {

    private final DoctorService doctorService;
    private final SpecialtyService specialtyService;

    // ================= LIST + SEARCH =================

    @GetMapping
    public String listDoctors(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long specialtyId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Gender gender,
            Model model
    ) {
        boolean hasFilter = (keyword != null && !keyword.isBlank())
                || specialtyId != null || active != null || gender != null;

        if (hasFilter) {
            model.addAttribute("doctors", doctorService.searchDoctors(keyword, specialtyId, active, gender));
        } else {
            model.addAttribute("doctors", doctorService.getAllDoctors());
        }

        model.addAttribute("specialties", specialtyService.findAllSpecialties());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedSpecialty", specialtyId);
        model.addAttribute("selectedActive", active);
        model.addAttribute("selectedGender", gender);
        model.addAttribute("genders", Gender.values());

        return "admin/doctor";
    }

    // ================= CREATE FORM =================

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("doctor", new AdminDoctorDto());
        model.addAttribute("specialties", specialtyService.findAllSpecialties());
        model.addAttribute("genders", Gender.values());
        return "admin/doctor-form";
    }

    // ================= EDIT FORM =================

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("doctor", doctorService.getDoctorById(id));
        model.addAttribute("specialties", specialtyService.findAllSpecialties());
        model.addAttribute("genders", Gender.values());
        return "admin/doctor-form";
    }

    // ================= SAVE =================

    @PostMapping("/save")
    public String saveDoctor(
            @Valid @ModelAttribute("doctor") AdminDoctorDto dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            model.addAttribute("specialties", specialtyService.findAllSpecialties());
            model.addAttribute("genders", Gender.values());
            return "admin/doctor-form";
        }

        try {
            doctorService.saveDoctor(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    dto.getId() != null ? "Cập nhật bác sĩ thành công!" : "Thêm bác sĩ thành công!");
            return "redirect:/admin/doctors";
        } catch (RuntimeException e) {
            model.addAttribute("specialties", specialtyService.findAllSpecialties());
            model.addAttribute("genders", Gender.values());
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/doctor-form";
        }
    }

    // ================= TOGGLE ACTIVE =================
    // FIX: Tách toggle và delete thành 2 action riêng

    @PostMapping("/toggle/{id}")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            doctorService.toggleDoctor(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái bác sĩ");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/doctors";
    }

    // ================= DELETE (soft delete - vô hiệu hóa) =================

    @PostMapping("/delete/{id}")
    public String deleteDoctor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            doctorService.deleteDoctor(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã vô hiệu hóa bác sĩ");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/doctors";
    }
}
