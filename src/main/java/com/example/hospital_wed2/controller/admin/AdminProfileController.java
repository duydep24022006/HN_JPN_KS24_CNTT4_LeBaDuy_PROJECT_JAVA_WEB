package com.example.hospital_wed2.controller.admin;


import com.example.hospital_wed2.dto.profile.shared.ChangePasswordRequest;
import com.example.hospital_wed2.dto.profile.shared.UpdateProfileRequest;
import com.example.hospital_wed2.dto.profile.shared.UserProfileResponse;
import com.example.hospital_wed2.service.FileStorageService;
import com.example.hospital_wed2.service.admin.AdminProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/profile")
public class AdminProfileController {

    private final AdminProfileService profileService;
    private final FileStorageService fileStorageService;

    public AdminProfileController(AdminProfileService profileService,
                                  FileStorageService fileStorageService) {
        this.profileService = profileService;
        this.fileStorageService = fileStorageService;
    }

    // ─── GET: Hiển thị trang profile ────────────────────────────────────────

    @GetMapping
    public String getAdminOwnProfile(
            @RequestParam(required = false) Boolean edit,
            Model model,
            HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        try {
            UserProfileResponse profile = profileService.getMyProfile(auth.getName());
            model.addAttribute("profile", profile);
            // Populate form object cho th:object binding
            model.addAttribute("updateRequest", toUpdateRequest(profile));
        } catch (IllegalArgumentException e) {
            model.addAttribute("profile", new UserProfileResponse());
            model.addAttribute("updateRequest", new UpdateProfileRequest());
            model.addAttribute("errorMessage", e.getMessage());
        }

        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        model.addAttribute("editMode", edit != null && edit);
        model.addAttribute("currentUri", request.getRequestURI());

        return "admin/profile";
    }

    // ─── POST: Cập nhật thông tin cá nhân ───────────────────────────────────

    @PostMapping("/update")
    public String updateProfile(
            @Valid @ModelAttribute("updateRequest") UpdateProfileRequest request,
            BindingResult bindingResult,
            @RequestParam(required = false) MultipartFile avatarFile,
            Model model,
            HttpServletRequest httpRequest,
            RedirectAttributes ra) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        if (bindingResult.hasErrors()) {
            // Trả lại trang với lỗi validation
            try {
                model.addAttribute("profile", profileService.getMyProfile(username));
            } catch (Exception ignored) {
                model.addAttribute("profile", new UserProfileResponse());
            }
            model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
            model.addAttribute("editMode", true);
            model.addAttribute("currentUri", httpRequest.getRequestURI());
            return "admin/profile";
        }

        // Upload avatar nếu có file mới
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String fileName = fileStorageService.storeFile(avatarFile);
                request.setAvatarUrl(fileName);
            } catch (IllegalArgumentException e) {
                // Lỗi upload ảnh — trả về form với thông báo lỗi
                try {
                    model.addAttribute("profile", profileService.getMyProfile(username));
                } catch (Exception ignored) {
                    model.addAttribute("profile", new UserProfileResponse());
                }
                model.addAttribute("updateRequest", request);
                model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
                model.addAttribute("editMode", true);
                model.addAttribute("avatarError", e.getMessage());
                model.addAttribute("currentUri", httpRequest.getRequestURI());
                return "admin/profile";
            }
        }

        try {
            profileService.updateMyProfile(username, request);
            ra.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Cập nhật thất bại: " + e.getMessage());
        }

        return "redirect:/admin/profile";
    }

    // ─── POST: Đổi mật khẩu ─────────────────────────────────────────────────

    @PostMapping("/change-password")
    public String changePassword(
            @ModelAttribute("changePasswordRequest") ChangePasswordRequest request,
            RedirectAttributes ra) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        try {
            profileService.changePassword(auth.getName(), request);
            ra.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("passwordError", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("passwordError", "Đổi mật khẩu thất bại: " + e.getMessage());
        }

        return "redirect:/admin/profile";
    }

    // ─── Helper: Chuyển ProfileResponse → UpdateRequest để bind vào form ────

    private UpdateProfileRequest toUpdateRequest(UserProfileResponse profile) {
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