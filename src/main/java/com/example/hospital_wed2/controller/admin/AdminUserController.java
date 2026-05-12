package com.example.hospital_wed2.controller.admin;

import com.example.hospital_wed2.dto.admin.AdminUserDto;
import com.example.hospital_wed2.entity.Role;
import com.example.hospital_wed2.service.admin.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService userService;

    @GetMapping
    public String listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            Model model,
            HttpServletRequest request) {

        model.addAttribute("users", userService.searchUsers(keyword, role, active));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedActive", active);
        model.addAttribute("roles",
                Arrays.stream(Role.values())
                        .filter(r -> r != Role.ADMIN)
                        .toList());
        model.addAttribute("currentUri", request.getRequestURI());

        return "admin/users";
    }

    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "admin/user-detail";
    }

    @PostMapping({"/{id}/toggle", "/toggle/{id}"})
    public String toggleStatus(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.toggleUserStatus(id);
            ra.addFlashAttribute("successMessage", "Đã thay đổi trạng thái tài khoản");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping({"/{id}/delete", "/delete/{id}"})
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.deleteUser(id);
            ra.addFlashAttribute("successMessage", "Đã vô hiệu hóa tài khoản");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}