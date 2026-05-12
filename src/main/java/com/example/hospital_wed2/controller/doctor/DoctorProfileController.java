package com.example.hospital_wed2.controller.doctor;

import com.example.hospital_wed2.dto.profile.doctor.DoctorProfileResponse;
import com.example.hospital_wed2.dto.profile.doctor.UpdateDoctorProfileRequest;
import com.example.hospital_wed2.dto.profile.shared.ChangePasswordRequest;
import com.example.hospital_wed2.service.doctor.DoctorProfileService;
import com.example.hospital_wed2.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/doctor/profile")
@RequiredArgsConstructor
public class DoctorProfileController {

    private final DoctorProfileService doctorProfileService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public String getProfile(
            @RequestParam(required = false) Boolean edit,
            Model model,
            HttpServletRequest request,
            Authentication auth) {

        String email = auth.getName();
        DoctorProfileResponse profile = doctorProfileService.getDoctorProfile(email);

        model.addAttribute("profile", profile);
        model.addAttribute("updateRequest", convertToUpdateRequest(profile));
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        model.addAttribute("editMode", edit != null && edit);
        model.addAttribute("currentUri", request.getRequestURI());

        return "doctor/profile";
    }

    @PostMapping("/update")
    public String updateProfile(
            @Valid @ModelAttribute("updateRequest") UpdateDoctorProfileRequest request,
            BindingResult bindingResult,
            @RequestParam(required = false) MultipartFile avatarFile,
            RedirectAttributes ra,
            Authentication auth) {

        String email = auth.getName();

        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("errorMessage", "Dữ liệu không hợp lệ, vui lòng kiểm tra lại");
            return "redirect:/doctor/profile?edit=true";
        }

        // Upload avatar nếu có
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String fileName = fileStorageService.storeFile(avatarFile);
                request.setAvatarUrl(fileName);
            } catch (Exception e) {
                ra.addFlashAttribute("errorMessage", "Upload ảnh thất bại: " + e.getMessage());
                return "redirect:/doctor/profile?edit=true";
            }
        }

        try {
            doctorProfileService.updateDoctorProfile(email, request);
            ra.addFlashAttribute("successMessage", "Cập nhật hồ sơ bác sĩ thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/doctor/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @Valid @ModelAttribute ChangePasswordRequest request,
            BindingResult bindingResult,
            RedirectAttributes ra,
            Authentication auth) {

        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("passwordError", "Dữ liệu mật khẩu không hợp lệ");
            return "redirect:/doctor/profile";
        }

        try {
            doctorProfileService.changePassword(auth.getName(), request);
            ra.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("passwordError", e.getMessage());
        }

        return "redirect:/doctor/profile";
    }

    private UpdateDoctorProfileRequest convertToUpdateRequest(DoctorProfileResponse profile) {
        UpdateDoctorProfileRequest req = new UpdateDoctorProfileRequest();
        req.setSpecialtyId(profile.getSpecialtyId());
        req.setLicenseNumber(profile.getLicenseNumber());
        req.setExperienceYears(profile.getExperienceYears());
        req.setDescription(profile.getDescription());
        req.setConsultationFee(profile.getConsultationFee());
        req.setAvatarUrl(profile.getAvatarUrl());
        return req;
    }
}