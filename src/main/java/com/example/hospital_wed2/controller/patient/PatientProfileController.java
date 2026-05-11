package com.example.hospital_wed2.controller.patient;

import com.example.hospital_wed2.dto.profile.ChangePasswordRequest;
import com.example.hospital_wed2.dto.profile.UpdateProfileRequest;
import com.example.hospital_wed2.dto.profile.UserProfileResponse;
import com.example.hospital_wed2.service.FileStorageService;
import com.example.hospital_wed2.service.profile.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final ProfileService profileService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public String getMyProfile(@RequestParam(required = false) Boolean edit, Model model , HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserProfileResponse profile = profileService.getMyProfile(auth.getName());
        model.addAttribute("profile", profile);
        model.addAttribute("editMode", edit != null && edit);
        model.addAttribute("currentUri", request.getRequestURI());
        return "patient/profile";
    }

    @PostMapping("/update")
    public String updateMyProfile(
            @Valid @ModelAttribute("profile") UpdateProfileRequest request,
            BindingResult bindingResult,
            @RequestParam(required = false) MultipartFile avatarFile,
            Model model, RedirectAttributes ra) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profileService.getMyProfile(username));
            model.addAttribute("editMode", true);
            return "patient/profile";
        }
        if (avatarFile != null && !avatarFile.isEmpty()) {
            request.setAvatarUrl(fileStorageService.storeFile(avatarFile));
        }
        profileService.updateMyProfile(username, request);
        ra.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        return "redirect:/patient/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @ModelAttribute ChangePasswordRequest request,
            RedirectAttributes ra) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            profileService.changePassword(auth.getName(), request);
            ra.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/patient/profile";
    }
}
