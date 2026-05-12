package com.example.hospital_wed2.controller.admin;

import com.example.hospital_wed2.dto.admin.AdminDoctorDto;
import com.example.hospital_wed2.entity.Gender;
import com.example.hospital_wed2.service.admin.AdminDoctorService;
import com.example.hospital_wed2.service.admin.AdminSpecialtyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/doctors")
@RequiredArgsConstructor
public class AdminDoctorsController {

    private final AdminDoctorService doctorService;
    private final AdminSpecialtyService specialtyService;

    @GetMapping
    public String listDoctors(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long specialtyId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Gender gender,
            Model model,
            HttpServletRequest request) {

        model.addAttribute("doctors", doctorService.searchDoctors(keyword, specialtyId, active, gender));
        model.addAttribute("specialties", specialtyService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedSpecialty", specialtyId);
        model.addAttribute("selectedActive", active);
        model.addAttribute("selectedGender", gender);
        model.addAttribute("genders", Gender.values());
        model.addAttribute("currentUri", request.getRequestURI());

        return "admin/doctor";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("doctor", new AdminDoctorDto());
        model.addAttribute("specialties", specialtyService.findAll());
        model.addAttribute("genders", Gender.values());
        return "admin/doctor-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("doctor", doctorService.getDoctorById(id));
        model.addAttribute("specialties", specialtyService.findAll());
        model.addAttribute("genders", Gender.values());
        return "admin/doctor-form";
    }

    @PostMapping("/save")
    public String saveDoctor(
            @Valid @ModelAttribute("doctor") AdminDoctorDto dto,
            BindingResult result,
            Model model,
            RedirectAttributes ra) {

        if (result.hasErrors()) {
            model.addAttribute("specialties", specialtyService.findAll());
            model.addAttribute("genders", Gender.values());
            return "admin/doctor-form";
        }

        try {
            doctorService.saveDoctor(dto);
            ra.addFlashAttribute("successMessage",
                    dto.getId() != null ? "Cập nhật bác sĩ thành công!" : "Thêm bác sĩ thành công!");
            return "redirect:/admin/doctors";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/doctors";
        }
    }

    @PostMapping({"/{id}/toggle", "/toggle/{id}"})
    public String toggleStatus(@PathVariable Long id, RedirectAttributes ra) {
        try {
            doctorService.toggleDoctorStatus(id);
            ra.addFlashAttribute("successMessage", "Đã thay đổi trạng thái bác sĩ");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/doctors";
    }

    @PostMapping({"/{id}/delete", "/delete/{id}"})
    public String deleteDoctor(@PathVariable Long id, RedirectAttributes ra) {
        try {
            doctorService.deleteDoctor(id);
            ra.addFlashAttribute("successMessage", "Đã vô hiệu hóa bác sĩ");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/doctors";
    }
}