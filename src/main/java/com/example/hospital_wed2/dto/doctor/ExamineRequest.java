package com.example.hospital_wed2.dto.doctor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ExamineRequest {

    @NotBlank(message = "Vui lòng nhập triệu chứng")
    @Size(max = 3000, message = "Triệu chứng không quá 3000 ký tự")
    private String symptoms;

    @NotBlank(message = "Vui lòng nhập chẩn đoán")
    @Size(max = 3000, message = "Chẩn đoán không quá 3000 ký tự")
    private String diagnosis;

    @Size(max = 2000, message = "Ghi chú không quá 2000 ký tự")
    private String doctorNote;

    @NotEmpty(message = "Phải kê ít nhất một loại thuốc")
    private List<Long> medicineIds;

    @NotEmpty(message = "Số lượng thuốc không được trống")
    private List<Integer> quantities;

    private List<String> dosages;

    // Getters & Setters
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getDoctorNote() { return doctorNote; }
    public void setDoctorNote(String doctorNote) { this.doctorNote = doctorNote; }

    public List<Long> getMedicineIds() { return medicineIds; }
    public void setMedicineIds(List<Long> medicineIds) { this.medicineIds = medicineIds; }

    public List<Integer> getQuantities() { return quantities; }
    public void setQuantities(List<Integer> quantities) { this.quantities = quantities; }

    public List<String> getDosages() { return dosages; }
    public void setDosages(List<String> dosages) { this.dosages = dosages; }
}