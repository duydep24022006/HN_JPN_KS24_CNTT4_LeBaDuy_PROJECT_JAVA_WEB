package com.example.hospital_wed2.controller.admin;

import com.example.hospital_wed2.entity.Medicine;
import com.example.hospital_wed2.entity.Prescription;
import com.example.hospital_wed2.entity.PrescriptionDetail;
import com.example.hospital_wed2.entity.PrescriptionStatus;
import com.example.hospital_wed2.repository.MedicineRepository;
import com.example.hospital_wed2.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/admin/prescriptions")
@RequiredArgsConstructor
public class AdminPrescriptionsController {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicineRepository medicineRepository;

    // BUG-14: State machine - các chuyển trạng thái hợp lệ
    private static final Map<PrescriptionStatus, Set<PrescriptionStatus>> VALID_TRANSITIONS = Map.of(
            PrescriptionStatus.PENDING, Set.of(PrescriptionStatus.DISPENSED, PrescriptionStatus.CANCELLED),
            PrescriptionStatus.DISPENSED, Set.of(), // trạng thái cuối, không thể chuyển tiếp
            PrescriptionStatus.CANCELLED, Set.of()  // trạng thái cuối
    );

    @GetMapping
    public String listPrescriptions(
            @RequestParam(required = false) PrescriptionStatus status,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        List<Prescription> prescriptions;

        if (status != null) {
            prescriptions = prescriptionRepository.findByStatus(status);
        } else {
            prescriptions = prescriptionRepository.findAll();
        }

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            prescriptions = prescriptions.stream()
                .filter(p -> {
                    try {
                        var patientProfile = p.getMedicalRecord().getAppointment().getPatient().getProfile();
                        var doctorProfile = p.getMedicalRecord().getAppointment().getDoctor().getUser().getProfile();
                        String patientName = patientProfile != null ? patientProfile.getFullName() : "";
                        String doctorName = doctorProfile != null ? doctorProfile.getFullName() : "";
                        return patientName.toLowerCase().contains(kw) || doctorName.toLowerCase().contains(kw);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .toList();
        }

        model.addAttribute("prescriptions", prescriptions);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statuses", PrescriptionStatus.values());

        return "admin/prescriptions";
    }

    @GetMapping("/{id}")
    public String viewDetail(@PathVariable Long id, Model model) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn thuốc"));
        model.addAttribute("prescription", prescription);
        return "admin/prescription-detail";
    }

    @PostMapping("/{id}/status")
    @Transactional
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam PrescriptionStatus status,
            RedirectAttributes redirectAttributes
    ) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn thuốc"));

        PrescriptionStatus currentStatus = prescription.getStatus();

        // BUG-14: Kiểm tra chuyển trạng thái có hợp lệ không
        Set<PrescriptionStatus> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(status)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể chuyển trạng thái từ " + currentStatus + " sang " + status);
            return "redirect:/admin/prescriptions/" + id;
        }

        // BUG-05: Khi cấp phát, trừ tồn kho thuốc
        if (status == PrescriptionStatus.DISPENSED) {
            List<PrescriptionDetail> details = prescription.getDetails();
            if (details != null) {
                for (PrescriptionDetail detail : details) {
                    Medicine medicine = detail.getMedicine();
                    int needed = detail.getQuantity() != null ? detail.getQuantity() : 0;
                    int currentStock = medicine.getStockQuantity() != null ? medicine.getStockQuantity() : 0;

                    if (currentStock < needed) {
                        redirectAttributes.addFlashAttribute("errorMessage",
                                "Không đủ tồn kho cho thuốc \"" + medicine.getName() +
                                "\". Tồn kho hiện tại: " + currentStock + ", cần: " + needed);
                        return "redirect:/admin/prescriptions/" + id;
                    }
                }
                // Đã kiểm tra đủ tồn kho, giờ trừ
                for (PrescriptionDetail detail : details) {
                    Medicine medicine = detail.getMedicine();
                    int newStock = medicine.getStockQuantity() - detail.getQuantity();
                    medicine.setStockQuantity(newStock);
                    medicineRepository.save(medicine);
                }
            }
            prescription.setDispensedAt(java.time.LocalDateTime.now());
        }

        prescription.setStatus(status);
        prescriptionRepository.save(prescription);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái đơn thuốc");
        return "redirect:/admin/prescriptions/" + id;
    }
}
