package com.example.hospital_wed2.controller.admin;

import com.example.hospital_wed2.entity.Role;
import com.example.hospital_wed2.entity.User;
import com.example.hospital_wed2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;

    // ================= LIST + SEARCH =================

    @GetMapping
    public String listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            Model model
    ) {
        boolean hasFilter = (keyword != null && !keyword.isBlank()) || role != null || active != null;

        List<User> users;
        if (hasFilter) {
            String kw = (keyword != null && !keyword.isBlank()) ? keyword : null;
            users = userRepository.searchUsers(role, active, kw);
        } else {
            users = userRepository.findAllWithProfile();
        }

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedActive", active);
        model.addAttribute("roles",
                Arrays.stream(Role.values())
                        .filter(r -> r != Role.ADMIN)
                        .toList());

        return "admin/users";
    }

    // ================= TOGGLE ACTIVE =================

    @PostMapping("/toggle/{id}")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        user.setActive(!user.getActive());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage",
                user.getActive() ? "Đã kích hoạt tài khoản" : "Đã vô hiệu hóa tài khoản");
        return "redirect:/admin/users";
    }
}
