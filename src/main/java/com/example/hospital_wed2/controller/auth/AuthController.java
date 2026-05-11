package com.example.hospital_wed2.controller.auth;

import com.example.hospital_wed2.dto.auth.LoginDTO;
import com.example.hospital_wed2.dto.auth.RegisterDTO;
import com.example.hospital_wed2.entity.User;
import com.example.hospital_wed2.service.auth.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model, HttpSession session) {
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/patient/dashboard";
        }
        model.addAttribute("registerDTO", new RegisterDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute RegisterDTO registerDTO,
                                 BindingResult bindingResult,
                                 Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(registerDTO);
            return "redirect:/login?success=true";
        } catch (RuntimeException e) {
            if (e.getMessage().contains("điện thoại") || e.getMessage().contains("phone")) {
                bindingResult.rejectValue("phoneNumber", "error.phone", e.getMessage());
            } else if (e.getMessage().contains("Email")) {
                bindingResult.rejectValue("email", "error.email", e.getMessage());
            } else if (e.getMessage().contains("Tên")) {
                bindingResult.rejectValue("username", "error.username", e.getMessage());
            } else {
                model.addAttribute("error", e.getMessage());
            }

            model.addAttribute("registerDTO", registerDTO);
            return "auth/register";
        }
    }
    @GetMapping("/login")
    public String showLoginPage(Model model, HttpSession session,
                                @RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "success", required = false) String success) {
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/patient/dashboard";
        }
        if (error != null) {
            model.addAttribute("errorMessage", "Email hoặc mật khẩu không đúng");
        }
        if (success != null) {
            model.addAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập");
        }
        model.addAttribute("loginDTO", new LoginDTO());
        return "auth/login";
    }

    // ... phần login và logout giữ nguyên ...
    @PostMapping("/login")
    public String handleLogin(@Valid @ModelAttribute("loginDTO") LoginDTO dto,
                              BindingResult bindingResult,
                              HttpSession session,
                              Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            User user = authService.login(dto);
            session.setAttribute("currentUser", user);
            session.setAttribute("userRole", user.getRole().name());
            session.setMaxInactiveInterval(60 * 60);

            return switch (user.getRole()) {
                case ADMIN -> "redirect:/admin/dashboard";
                case DOCTOR -> "redirect:/doctor/profile";
                case PATIENT -> "redirect:/patient/dashboard";
            };

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/logout")
    public String handleLogout(HttpSession session, RedirectAttributes redirectAttributes) {
        authService.logout(session);
        redirectAttributes.addFlashAttribute("successMessage", "Đã đăng xuất thành công");
        return "redirect:/login";
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        if (session.getAttribute("currentUser") != null) {
            String role = (String) session.getAttribute("userRole");
            return switch (role) {
                case "ADMIN" -> "redirect:/admin/dashboard";
                case "DOCTOR" -> "redirect:/doctor/profile";
                default -> "redirect:/patient/dashboard";
            };
        }
        return "redirect:/login";
    }
}