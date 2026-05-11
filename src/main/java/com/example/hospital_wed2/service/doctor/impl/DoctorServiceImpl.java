package com.example.hospital_wed2.service.doctor.impl;

import com.example.hospital_wed2.dto.doctor.DoctorStatsDto;
import com.example.hospital_wed2.dto.doctor.ExamineRequest;
import com.example.hospital_wed2.entity.*;
import com.example.hospital_wed2.repository.*;
import com.example.hospital_wed2.service.doctor.DoctorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service // Đánh dấu đây là class Service để Spring quản lý
public class DoctorServiceImpl implements DoctorService {

    // Repository thao tác với bảng User
    private final UserRepository userRepository;

    // Repository thao tác với bảng Doctor
    private final DoctorRepository doctorRepository;

    // Repository thao tác với bảng Appointment
    private final AppointmentRepository appointmentRepository;

    // Repository thao tác với bảng MedicalRecord
    private final MedicalRecordRepository medicalRecordRepository;

    // Repository thao tác với bảng Prescription
    private final PrescriptionRepository prescriptionRepository;

    // Repository thao tác với bảng Medicine
    private final MedicineRepository medicineRepository;

    // Constructor inject các repository vào service
    public DoctorServiceImpl(UserRepository userRepository,
                             DoctorRepository doctorRepository,
                             AppointmentRepository appointmentRepository,
                             MedicalRecordRepository medicalRecordRepository,
                             PrescriptionRepository prescriptionRepository,
                             MedicineRepository medicineRepository) {

        // Gán UserRepository vào biến
        this.userRepository = userRepository;

        // Gán DoctorRepository vào biến
        this.doctorRepository = doctorRepository;

        // Gán AppointmentRepository vào biến
        this.appointmentRepository = appointmentRepository;

        // Gán MedicalRecordRepository vào biến
        this.medicalRecordRepository = medicalRecordRepository;

        // Gán PrescriptionRepository vào biến
        this.prescriptionRepository = prescriptionRepository;

        // Gán MedicineRepository vào biến
        this.medicineRepository = medicineRepository;
    }

    // =====================================================
    // STATS
    // =====================================================

    @Override
    @Transactional(readOnly = true) // Chỉ đọc dữ liệu, không update DB
    public DoctorStatsDto getStats(String username) {

        // Tìm bác sĩ theo username/email
        Doctor doctor = findDoctorByUsername(username);

        // Lấy ngày hiện tại
        LocalDate today = LocalDate.now();

        // Tạo object chứa thống kê
        DoctorStatsDto stats = new DoctorStatsDto();

        // Tổng lịch hẹn hôm nay
        stats.setTodayAppointments(
                appointmentRepository.findByDoctorAndDate(doctor, today).size()
        );

        // Tổng lịch đang chờ xác nhận
        stats.setPendingCount(
                appointmentRepository.countByDoctorAndStatus(
                        doctor,
                        AppointmentStatus.PENDING
                )
        );

        // Tổng lịch đã xác nhận
        stats.setConfirmedCount(
                appointmentRepository.countByDoctorAndStatus(
                        doctor,
                        AppointmentStatus.CONFIRMED
                )
        );

        // Tổng lịch hoàn thành hôm nay
        stats.setCompletedToday(
                appointmentRepository.countByDoctorAndDateAndStatus(
                        doctor,
                        today,
                        AppointmentStatus.COMPLETED
                )
        );

        // Tổng số bệnh nhân khác nhau đã khám
        stats.setTotalPatients(
                appointmentRepository.countDistinctPatientsByDoctor(doctor)
        );

        // Tổng số lịch đã hoàn thành
        stats.setTotalCompleted(
                appointmentRepository.countByDoctorAndStatus(
                        doctor,
                        AppointmentStatus.COMPLETED
                )
        );

        // Tổng số lịch đã hủy
        stats.setTotalCancelled(
                appointmentRepository.countByDoctorAndStatus(
                        doctor,
                        AppointmentStatus.CANCELLED
                )
        );

        // Trả dữ liệu thống kê
        return stats;
    }

    // =====================================================
    // APPOINTMENTS
    // =====================================================

    @Override
    @Transactional(readOnly = true) // Chỉ lấy dữ liệu lịch hẹn
    public List<Appointment> getAppointments(String username, String status) {

        // Tìm bác sĩ theo username/email
        Doctor doctor = findDoctorByUsername(username);

        // Nếu không truyền status thì lấy tất cả
        if (status == null || status.isBlank()) {

            // Lấy tất cả lịch hẹn của bác sĩ
            return appointmentRepository.findByDoctorOrderByDateDesc(doctor);
        }

        // Chuyển String sang Enum
        AppointmentStatus statusEnum =
                AppointmentStatus.valueOf(status.toUpperCase());

        // Lấy lịch theo trạng thái
        return appointmentRepository.findByDoctorAndStatus(
                doctor,
                statusEnum
        );
    }

    @Override
    @Transactional(readOnly = true) // Chỉ lấy dữ liệu
    public Appointment getAppointment(String username, Long appointmentId) {

        // Tìm bác sĩ
        Doctor doctor = findDoctorByUsername(username);

        // Tìm lịch hẹn theo id và bác sĩ
        return appointmentRepository.findByIdAndDoctor(
                        appointmentId,
                        doctor
                )

                // Nếu không tồn tại thì báo lỗi
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy lịch hẹn"
                        )
                );
    }

    @Override
    @Transactional // Có update DB nên dùng transaction
    public void confirmAppointment(String username, Long appointmentId) {

        // Lấy lịch hẹn
        Appointment appointment =
                getAppointment(username, appointmentId);

        // Chỉ xác nhận lịch đang chờ
        if (appointment.getStatus() != AppointmentStatus.PENDING) {

            // Báo lỗi nếu trạng thái không hợp lệ
            throw new IllegalStateException(
                    "Chỉ có thể xác nhận lịch hẹn đang chờ"
            );
        }

        // Đổi trạng thái sang CONFIRMED
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        // Lưu lại DB
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional // Có update DB
    public void cancelAppointment(String username, Long appointmentId) {

        // Lấy lịch hẹn
        Appointment appointment =
                getAppointment(username, appointmentId);

        // Không cho hủy nếu đã hoàn thành hoặc đã hủy
        if (appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.CANCELLED) {

            // Báo lỗi
            throw new IllegalStateException(
                    "Không thể hủy lịch hẹn này"
            );
        }

        // Đổi trạng thái sang CANCELLED
        appointment.setStatus(AppointmentStatus.CANCELLED);

        // Lưu DB
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional // Có insert/update nhiều bảng
    public void examine(
            String username,
            Long appointmentId,
            ExamineRequest request
    ) {

        // Lấy lịch hẹn
        Appointment appointment =
                getAppointment(username, appointmentId);

        // Chỉ khám khi lịch đã xác nhận
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {

            // Báo lỗi nếu chưa xác nhận
            throw new IllegalStateException(
                    "Chỉ có thể khám lịch hẹn đã xác nhận"
            );
        }

        // Danh sách id thuốc
        List<Long> medicineIds = request.getMedicineIds();

        // Danh sách số lượng thuốc
        List<Integer> quantities = request.getQuantities();

        // Danh sách liều dùng
        List<String> dosages = request.getDosages();

        // Kiểm tra phải có ít nhất 1 thuốc
        if (medicineIds == null || medicineIds.isEmpty()) {

            // Báo lỗi nếu chưa kê thuốc
            throw new IllegalArgumentException(
                    "Phải kê ít nhất một loại thuốc"
            );
        }

        // Kiểm tra số lượng thuốc hợp lệ
        if (quantities == null
                || quantities.size() != medicineIds.size()) {

            // Báo lỗi nếu dữ liệu sai
            throw new IllegalArgumentException(
                    "Số lượng không hợp lệ"
            );
        }

        // =====================================================
        // 1. CREATE MEDICAL RECORD
        // =====================================================

        // Tạo bệnh án
        MedicalRecord record = MedicalRecord.builder()

                // Gắn lịch hẹn
                .appointment(appointment)

                // Triệu chứng
                .symptoms(request.getSymptoms())

                // Chẩn đoán
                .diagnosis(request.getDiagnosis())

                // Ghi chú bác sĩ
                .notes(request.getDoctorNote())

                // Build object
                .build();

        // Lưu bệnh án
        medicalRecordRepository.save(record);

        // =====================================================
        // 2. CREATE PRESCRIPTION
        // =====================================================

        // Tạo đơn thuốc
        Prescription prescription = Prescription.builder()

                // Gắn bệnh án
                .medicalRecord(record)

                // Trạng thái đơn thuốc
                .status(PrescriptionStatus.PENDING)

                // Build object
                .build();

        // Lưu đơn thuốc
        prescriptionRepository.save(prescription);

        // =====================================================
        // 3. CREATE PRESCRIPTION DETAILS
        // =====================================================

        // Danh sách chi tiết thuốc
        List<PrescriptionDetail> details = new ArrayList<>();

        // Duyệt toàn bộ thuốc
        for (int i = 0; i < medicineIds.size(); i++) {

            // Lấy id thuốc
            Long medId = medicineIds.get(i);

            // Nếu id null thì bỏ qua
            if (medId == null) continue;

            // Tìm thuốc trong DB
            Medicine medicine = medicineRepository.findById(medId)

                    // Báo lỗi nếu thuốc không tồn tại
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Thuốc không tồn tại"
                            )
                    );

            // Lấy số lượng thuốc
            Integer qty = quantities.get(i);

            // Kiểm tra số lượng hợp lệ
            if (qty == null || qty < 1) {

                // Báo lỗi nếu nhỏ hơn 1
                throw new IllegalArgumentException(
                        "Số lượng thuốc phải ít nhất là 1"
                );
            }

            // Lấy liều dùng
            String dosage =
                    (dosages != null && i < dosages.size())
                            ? dosages.get(i)
                            : null;

            // Tạo chi tiết đơn thuốc
            PrescriptionDetail detail =
                    PrescriptionDetail.builder()

                            // Gắn đơn thuốc
                            .prescription(prescription)

                            // Gắn thuốc
                            .medicine(medicine)

                            // Số lượng thuốc
                            .quantity(qty)

                            // Liều dùng
                            .dosage(dosage)

                            // Build object
                            .build();

            // Thêm vào danh sách
            details.add(detail);
        }

        // Gắn danh sách chi tiết thuốc
        prescription.setDetails(details);

        // Lưu đơn thuốc và chi tiết
        prescriptionRepository.save(prescription);

        // =====================================================
        // 4. COMPLETE APPOINTMENT
        // =====================================================

        // Đổi trạng thái lịch hẹn sang COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);

        // Lưu DB
        appointmentRepository.save(appointment);
    }

    // =====================================================
    // MEDICAL RECORDS
    // =====================================================

    @Override
    @Transactional(readOnly = true) // Chỉ lấy dữ liệu
    public MedicalRecord getMedicalRecord(Long appointmentId) {

        // Tìm bệnh án theo appointmentId
        return medicalRecordRepository
                .findByAppointmentId(appointmentId)

                // Nếu không có thì trả null
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true) // Chỉ lấy dữ liệu
    public Prescription getPrescription(Long appointmentId) {

        // Tìm đơn thuốc theo appointmentId
        return prescriptionRepository
                .findByAppointmentId(appointmentId)

                // Nếu không có thì trả null
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true) // Chỉ đọc dữ liệu
    public List<MedicalRecord> getMedicalRecords(String username) {

        // Tìm bác sĩ
        Doctor doctor = findDoctorByUsername(username);

        // Lấy danh sách bệnh án của bác sĩ
        return medicalRecordRepository.findByDoctor(doctor);
    }

    // =====================================================
    // MEDICINES
    // =====================================================

    @Override
    @Transactional(readOnly = true) // Chỉ lấy thuốc
    public List<Medicine> getActiveMedicines() {

        // Lấy thuốc đang hoạt động
        return medicineRepository.findByIsActive(true);
    }

    // =====================================================
    // DASHBOARD
    // =====================================================

    @Override
    @Transactional(readOnly = true) // Chỉ đọc dữ liệu
    public List<Appointment> getTodaySchedule(String username) {

        // Tìm bác sĩ
        Doctor doctor = findDoctorByUsername(username);

        // Lấy lịch hôm nay
        return appointmentRepository.findByDoctorAndDate(
                doctor,
                LocalDate.now()
        );
    }

    @Override
    @Transactional(readOnly = true) // Chỉ đọc dữ liệu
    public List<Appointment> getRecentAppointments(String username) {

        // Tìm bác sĩ
        Doctor doctor = findDoctorByUsername(username);

        // Lấy tất cả lịch gần đây
        List<Appointment> all =
                appointmentRepository
                        .findByDoctorOrderByCreatedAtDesc(doctor);

        // Chỉ lấy 5 lịch gần nhất
        return all.stream()
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true) // Chỉ đếm dữ liệu
    public long countByStatus(String username, String status) {

        // Tìm bác sĩ
        Doctor doctor = findDoctorByUsername(username);

        // Chuyển String sang Enum
        AppointmentStatus statusEnum =
                AppointmentStatus.valueOf(status.toUpperCase());

        // Đếm lịch theo trạng thái
        return appointmentRepository.countByDoctorAndStatus(
                doctor,
                statusEnum
        );
    }

    // =====================================================
    // HELPER
    // =====================================================

    // Hàm hỗ trợ tìm doctor theo username/email
    private Doctor findDoctorByUsername(String username) {

        // Tìm user theo email
        User user = userRepository.findByEmail(username)

                // Báo lỗi nếu không tồn tại
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy người dùng: " + username
                        )
                );

        // Tìm doctor theo userId
        return doctorRepository.findByUserId(user.getId())

                // Báo lỗi nếu không tồn tại doctor
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy bác sĩ"
                        )
                );
    }
}