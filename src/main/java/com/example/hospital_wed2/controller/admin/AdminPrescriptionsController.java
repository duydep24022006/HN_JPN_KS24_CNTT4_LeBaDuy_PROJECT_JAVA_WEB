package com.example.hospital_wed2.controller.admin;

import com.example.hospital_wed2.entity.PrescriptionStatus;
import com.example.hospital_wed2.service.admin.AdminPrescriptionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/prescriptions")
@RequiredArgsConstructor
public class AdminPrescriptionsController {

    private final AdminPrescriptionService prescriptionService;

    @GetMapping
    public String listPrescriptions(
            @RequestParam(required = false) PrescriptionStatus status,
            Model model,
            HttpServletRequest request) {

        if (status != null) {
            model.addAttribute("prescriptions", prescriptionService.getByStatus(status));
        } else {
            model.addAttribute("prescriptions", prescriptionService.getAllPrescriptions());
        }
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", PrescriptionStatus.values());
        model.addAttribute("currentUri", request.getRequestURI());
        return "admin/prescriptions";
    }

    @GetMapping("/{id}")
    public String viewDetail(@PathVariable Long id, Model model) {
        model.addAttribute("prescription", prescriptionService.getById(id));
        return "admin/prescription-detail";
    }

    @PostMapping({"/{id}/update-status", "/{id}/status", "/update-status/{id}", "/status/{id}"})
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam PrescriptionStatus status,
            @RequestParam(required = false) String note,
            RedirectAttributes ra) {

        try {
            prescriptionService.updateStatus(id, status, note);
            ra.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn thuốc thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/prescriptions/" + id;
    }
}