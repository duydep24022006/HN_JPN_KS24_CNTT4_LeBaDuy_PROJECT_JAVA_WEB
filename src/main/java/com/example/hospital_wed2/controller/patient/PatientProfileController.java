package com.example.hospital_wed2.controller.patient;

import com.example.hospital_wed2.dto.profile.shared.ChangePasswordRequest;
import com.example.hospital_wed2.dto.profile.shared.UpdateProfileRequest;
import com.example.hospital_wed2.dto.profile.shared.UserProfileResponse;
import com.example.hospital_wed2.service.patient.PatientProfileService;
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
@RequestMapping("/patient/profile")
@RequiredArgsConstructor
public class PatientProfileController {

    private final PatientProfileService patientProfileService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public String getMyProfile(
            @RequestParam(required = false) Boolean edit,
            Model model,
            HttpServletRequest request,
            Authentication auth) {

        String email = auth.getName();
        UserProfileResponse profile = patientProfileService.getMyProfile(email);

        model.addAttribute("profile", profile);
        model.addAttribute("updateRequest", convertToUpdateRequest(profile)); // để bind form
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        model.addAttribute("editMode", edit != null && edit);
        model.addAttribute("currentUri", request.getRequestURI());

        return "patient/profile";
    }

    @PostMapping("/update")
    public String updateMyProfile(
            @Valid @ModelAttribute("updateRequest") UpdateProfileRequest request,
            BindingResult bindingResult,
            @RequestParam(required = false) MultipartFile avatarFile,
            Model model,
            RedirectAttributes ra,
            Authentication auth) {

        String email = auth.getName();

        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", patientProfileService.getMyProfile(email));
            model.addAttribute("editMode", true);
            return "patient/profile";
        }

        // Upload avatar nếu có
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String fileName = fileStorageService.storeFile(avatarFile);
                request.setAvatarUrl(fileName);
            } catch (Exception e) {
                ra.addFlashAttribute("errorMessage", "Upload ảnh thất bại: " + e.getMessage());
                return "redirect:/patient/profile?edit=true";
            }
        }

        try {
            patientProfileService.updateMyProfile(email, request);
            ra.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/patient/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @Valid @ModelAttribute ChangePasswordRequest request,
            BindingResult bindingResult,
            RedirectAttributes ra,
            Authentication auth) {

        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("passwordError", "Dữ liệu mật khẩu không hợp lệ");
            return "redirect:/patient/profile";
        }

        try {
            patientProfileService.changePassword(auth.getName(), request);
            ra.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("passwordError", e.getMessage());
        }

        return "redirect:/patient/profile";
    }

    // Helper chuyển Response → Request để bind form
    private UpdateProfileRequest convertToUpdateRequest(UserProfileResponse profile) {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFullName(profile.getFullName());
        req.setPhoneNumber(profile.getPhoneNumber());
        req.setDateOfBirth(profile.getDateOfBirth());
        req.setGender(profile.getGender());
        req.setAddress(profile.getAddress());
        req.setIdentityCard(profile.getIdentityCard());
        req.setAvatarUrl(profile.getAvatarUrl());
        return req;
    }
}