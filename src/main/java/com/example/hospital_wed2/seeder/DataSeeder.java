package com.example.hospital_wed2.seeder;

import com.example.hospital_wed2.entity.*;
import com.example.hospital_wed2.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final UserProfileRepository profileRepo;
    private final SpecialtyRepository specialtyRepo;
    private final DoctorRepository doctorRepo;
    private final MedicineRepository medicineRepo;
    private final AppointmentRepository appointmentRepo;
    private final MedicalRecordRepository medicalRecordRepo;
    private final PrescriptionRepository prescriptionRepo;
    private final PrescriptionDetailRepository detailRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) {
            System.out.println("⚠️ Data already seeded -> skip");
            return;
        }

        // =====================================================
        // SPECIALTIES (6 chuyên khoa)
        // =====================================================
        Specialty noiKhoa = specialtyRepo.save(Specialty.builder()
                .name("Nội khoa").description("Khám và điều trị các bệnh lý nội tạng tổng quát như tiêu hóa, hô hấp, thần kinh").imageUrl("/uploads/specialties/noi-khoa.jpg").build());

        Specialty timMach = specialtyRepo.save(Specialty.builder()
                .name("Tim mạch").description("Chuyên điều trị các bệnh về tim, mạch máu, huyết áp và rối loạn nhịp tim").imageUrl("/uploads/specialties/tim-mach.jpg").build());

        Specialty nhiKhoa = specialtyRepo.save(Specialty.builder()
                .name("Nhi khoa").description("Chăm sóc sức khỏe toàn diện cho trẻ em từ sơ sinh đến 16 tuổi").imageUrl("/uploads/specialties/nhi-khoa.jpg").build());

        Specialty daLieu = specialtyRepo.save(Specialty.builder()
                .name("Da liễu").description("Khám và điều trị bệnh về da, tóc, móng và các bệnh lây qua đường tình dục").imageUrl("/uploads/specialties/da-lieu.jpg").build());

        Specialty matKhoa = specialtyRepo.save(Specialty.builder()
                .name("Mắt").description("Chẩn đoán và điều trị các bệnh về mắt, tật khúc xạ, đục thủy tinh thể").imageUrl("/uploads/specialties/mat.jpg").build());

        Specialty xuongKhop = specialtyRepo.save(Specialty.builder()
                .name("Cơ xương khớp").description("Khám và điều trị bệnh về xương, khớp, cột sống, gout, loãng xương").imageUrl("/uploads/specialties/xuong-khop.jpg").build());

        // =====================================================
        // ADMIN
        // =====================================================
        User admin = createUser("admin1", "admin@gmail.com", Role.ADMIN, true);
        createProfile(admin, "Quản trị viên hệ thống", "0901000001", LocalDate.of(1985, 1, 15), Gender.MALE, "123 Lý Thường Kiệt, Hà Nội");

        // =====================================================
        // DOCTORS (5 bác sĩ)
        // =====================================================
        User uD1 = createUser("bsnguyenvana", "doctor1@gmail.com", Role.DOCTOR, true);
        createProfile(uD1, "Nguyễn Văn An", "0902000001", LocalDate.of(1978, 5, 10), Gender.MALE, "45 Trần Hưng Đạo, Hà Nội");
        Doctor d1 = createDoctor(uD1, noiKhoa, "LIC-NOI-001", 15, "Bác sĩ chuyên khoa II nội tổng quát, 15 năm kinh nghiệm tại Bệnh viện Bạch Mai", new BigDecimal("250000"));

        User uD2 = createUser("bstranthib", "doctor2@gmail.com", Role.DOCTOR, true);
        createProfile(uD2, "Trần Thị Bích", "0902000002", LocalDate.of(1982, 3, 20), Gender.FEMALE, "78 Hai Bà Trưng, TP.HCM");
        Doctor d2 = createDoctor(uD2, timMach, "LIC-TIM-001", 10, "Tiến sĩ tim mạch, chuyên về rối loạn nhịp tim và suy tim, từng tu nghiệp tại Pháp", new BigDecimal("350000"));

        User uD3 = createUser("bslevanc", "doctor3@gmail.com", Role.DOCTOR, true);
        createProfile(uD3, "Lê Văn Cường", "0902000003", LocalDate.of(1980, 7, 5), Gender.MALE, "12 Nguyễn Huệ, Đà Nẵng");
        Doctor d3 = createDoctor(uD3, nhiKhoa, "LIC-NHI-001", 12, "Bác sĩ chuyên khoa nhi, chuyên về bệnh sơ sinh và dinh dưỡng trẻ em", new BigDecimal("200000"));

        User uD4 = createUser("bsphamthid", "doctor4@gmail.com", Role.DOCTOR, true);
        createProfile(uD4, "Phạm Thị Dung", "0902000004", LocalDate.of(1986, 11, 14), Gender.FEMALE, "56 Đinh Tiên Hoàng, Hà Nội");
        Doctor d4 = createDoctor(uD4, daLieu, "LIC-DA-001", 8, "Chuyên gia da liễu thẩm mỹ, điều trị mụn, nám, sẹo và các bệnh da mãn tính", new BigDecimal("300000"));

        User uD5 = createUser("bshoangvane", "doctor5@gmail.com", Role.DOCTOR, false);
        createProfile(uD5, "Hoàng Văn Em", "0902000005", LocalDate.of(1975, 9, 22), Gender.MALE, "99 Lê Lợi, Huế");
        Doctor d5 = createDoctor(uD5, matKhoa, "LIC-MAT-001", 20, "Giáo sư nhãn khoa, 20 năm kinh nghiệm phẫu thuật đục thủy tinh thể và ghép giác mạc", new BigDecimal("400000"));

        // =====================================================
        // PATIENTS (5 bệnh nhân)
        // =====================================================
        User uP1 = createUser("benhnhan1", "patient1@gmail.com", Role.PATIENT, true);
        createProfile(uP1, "Vũ Minh Tuấn", "0903000001", LocalDate.of(1995, 4, 12), Gender.MALE, "34 Nguyễn Trãi, Hà Nội");

        User uP2 = createUser("benhnhan2", "patient2@gmail.com", Role.PATIENT, true);
        createProfile(uP2, "Nguyễn Thị Lan", "0903000002", LocalDate.of(2000, 8, 25), Gender.FEMALE, "67 Pasteur, TP.HCM");

        User uP3 = createUser("benhnhan3", "patient3@gmail.com", Role.PATIENT, true);
        createProfile(uP3, "Đặng Quốc Hùng", "0903000003", LocalDate.of(1988, 2, 17), Gender.MALE, "23 Hùng Vương, Đà Nẵng");

        User uP4 = createUser("benhnhan4", "patient4@gmail.com", Role.PATIENT, true);
        createProfile(uP4, "Lý Thị Mai", "0903000004", LocalDate.of(2003, 6, 30), Gender.FEMALE, "11 Trần Phú, Hải Phòng");

        User uP5 = createUser("benhnhan5", "patient5@gmail.com", Role.PATIENT, true);
        createProfile(uP5, "Bùi Thanh Sơn", "0903000005", LocalDate.of(1970, 12, 5), Gender.MALE, "88 Lê Duẩn, Cần Thơ");

        // =====================================================
        // MEDICINES (10 thuốc)
        // =====================================================
        Medicine med1 = medicineRepo.save(Medicine.builder()
                .name("Paracetamol 500mg").ingredient("Paracetamol").unit("Viên").price(2000.0)
                .stockQuantity(5000).manufacturer("DHG Pharma").description("Thuốc giảm đau, hạ sốt thông dụng").isActive(true).build());

        Medicine med2 = medicineRepo.save(Medicine.builder()
                .name("Amoxicillin 500mg").ingredient("Amoxicillin trihydrate").unit("Viên").price(3500.0)
                .stockQuantity(2000).manufacturer("Mekophar").description("Kháng sinh nhóm penicillin, điều trị nhiễm khuẩn").isActive(true).build());

        Medicine med3 = medicineRepo.save(Medicine.builder()
                .name("Omeprazole 20mg").ingredient("Omeprazole").unit("Viên").price(4000.0)
                .stockQuantity(1500).manufacturer("Stada").description("Ức chế bơm proton, điều trị loét dạ dày").isActive(true).build());

        Medicine med4 = medicineRepo.save(Medicine.builder()
                .name("Amlodipine 5mg").ingredient("Amlodipine besylate").unit("Viên").price(5500.0)
                .stockQuantity(1000).manufacturer("Pfizer").description("Thuốc hạ huyết áp, chẹn kênh canxi").isActive(true).build());

        Medicine med5 = medicineRepo.save(Medicine.builder()
                .name("Cetirizine 10mg").ingredient("Cetirizine hydrochloride").unit("Viên").price(3000.0)
                .stockQuantity(800).manufacturer("ICA").description("Thuốc kháng histamin, điều trị dị ứng").isActive(true).build());

        Medicine med6 = medicineRepo.save(Medicine.builder()
                .name("Metformin 850mg").ingredient("Metformin hydrochloride").unit("Viên").price(2500.0)
                .stockQuantity(1200).manufacturer("Traphaco").description("Điều trị đái tháo đường type 2").isActive(true).build());

        Medicine med7 = medicineRepo.save(Medicine.builder()
                .name("Vitamin C 1000mg").ingredient("Ascorbic acid").unit("Viên sủi").price(8000.0)
                .stockQuantity(3000).manufacturer("Mekophar").description("Bổ sung vitamin C, tăng cường miễn dịch").isActive(true).build());

        Medicine med8 = medicineRepo.save(Medicine.builder()
                .name("Ibuprofen 400mg").ingredient("Ibuprofen").unit("Viên").price(4500.0)
                .stockQuantity(600).manufacturer("DHG Pharma").description("Kháng viêm, giảm đau, hạ sốt nhóm NSAID").isActive(true).build());

        Medicine med9 = medicineRepo.save(Medicine.builder()
                .name("Loratadine 10mg").ingredient("Loratadine").unit("Viên").price(2800.0)
                .stockQuantity(900).manufacturer("Domesco").description("Kháng histamin thế hệ 2, ít gây buồn ngủ").isActive(false).build());

        Medicine med10 = medicineRepo.save(Medicine.builder()
                .name("Simvastatin 20mg").ingredient("Simvastatin").unit("Viên").price(6000.0)
                .stockQuantity(400).manufacturer("Merck").description("Thuốc hạ cholesterol, bảo vệ tim mạch").isActive(true).build());

        // =====================================================
        // APPOINTMENTS (6 lịch hẹn)
        // =====================================================
        Appointment ap1 = createAppointment(uP1, d1, LocalDate.now().minusDays(5), LocalTime.of(8, 30), AppointmentStatus.COMPLETED, "Đau đầu, mệt mỏi kéo dài 1 tuần");
        Appointment ap2 = createAppointment(uP2, d2, LocalDate.now().minusDays(3), LocalTime.of(9, 0),  AppointmentStatus.COMPLETED, "Khó thở, đau ngực khi gắng sức");
        Appointment ap3 = createAppointment(uP3, d1, LocalDate.now().minusDays(1), LocalTime.of(10, 0), AppointmentStatus.COMPLETED, "Đau bụng vùng thượng vị, ợ chua");
        Appointment ap4 = createAppointment(uP4, d3, LocalDate.now(),              LocalTime.of(14, 0), AppointmentStatus.CONFIRMED, "Trẻ sốt cao 39 độ, ho nhiều");
        Appointment ap5 = createAppointment(uP5, d4, LocalDate.now().plusDays(2),  LocalTime.of(15, 30),AppointmentStatus.PENDING,   "Nổi mẩn đỏ toàn thân, ngứa dữ dội");
        Appointment ap6 = createAppointment(uP1, d2, LocalDate.now().plusDays(5),  LocalTime.of(8, 0),  AppointmentStatus.PENDING,   "Tái khám theo dõi huyết áp");

        // =====================================================
        // MEDICAL RECORDS
        // =====================================================
        MedicalRecord mr1 = medicalRecordRepo.save(MedicalRecord.builder()
                .appointment(ap1)
                .symptoms("Đau đầu vùng thái dương, mệt mỏi, mất ngủ")
                .diagnosis("Hội chứng đau đầu căng thẳng - Tension headache")
                .treatmentPlan("Nghỉ ngơi, giảm stress, dùng thuốc giảm đau theo chỉ định, tái khám sau 2 tuần")
                .followUpDate(LocalDate.now().plusDays(14))
                .notes("Bệnh nhân làm việc nhiều giờ trước máy tính, cần điều chỉnh tư thế")
                .build());

        MedicalRecord mr2 = medicalRecordRepo.save(MedicalRecord.builder()
                .appointment(ap2)
                .symptoms("Khó thở khi gắng sức, đau ngực trái, hồi hộp")
                .diagnosis("Rối loạn nhịp tim - Arrhythmia, kèm tăng huyết áp độ I")
                .treatmentPlan("Dùng thuốc hạ huyết áp, theo dõi nhịp tim, hạn chế muối, tái khám sau 1 tháng")
                .followUpDate(LocalDate.now().plusDays(30))
                .notes("Huyết áp đo được 145/95 mmHg, nhịp tim không đều. Cần đo Holter 24h")
                .build());

        MedicalRecord mr3 = medicalRecordRepo.save(MedicalRecord.builder()
                .appointment(ap3)
                .symptoms("Đau thượng vị, ợ chua, buồn nôn sau ăn, ăn không ngon")
                .diagnosis("Viêm loét dạ dày - Gastric ulcer, nghi ngờ nhiễm H.pylori")
                .treatmentPlan("Thuốc ức chế bơm proton 4 tuần, kháng sinh diệt H.pylori, ăn đúng giờ, tránh cay nóng")
                .followUpDate(LocalDate.now().plusDays(28))
                .notes("Cần nội soi dạ dày để xác nhận chẩn đoán. Bệnh nhân có tiền sử uống nhiều rượu")
                .build());

        // =====================================================
        // PRESCRIPTIONS
        // =====================================================
        Prescription pr1 = prescriptionRepo.save(Prescription.builder()
                .medicalRecord(mr1).status(PrescriptionStatus.DISPENSED)
                .notes("Uống sau bữa ăn. Không lái xe sau khi uống thuốc. Tái khám nếu đau đầu nặng hơn.")
                .dispensedAt(LocalDateTime.now().minusDays(4)).build());

        Prescription pr2 = prescriptionRepo.save(Prescription.builder()
                .medicalRecord(mr2).status(PrescriptionStatus.DISPENSED)
                .notes("Uống thuốc đều đặn hàng ngày, không tự ý ngừng thuốc. Đo huyết áp tại nhà mỗi sáng.")
                .dispensedAt(LocalDateTime.now().minusDays(2)).build());

        Prescription pr3 = prescriptionRepo.save(Prescription.builder()
                .medicalRecord(mr3).status(PrescriptionStatus.PENDING)
                .notes("Uống trước bữa ăn 30 phút. Kiêng rượu bia, đồ cay, chua trong thời gian điều trị.")
                .build());

        // =====================================================
        // PRESCRIPTION DETAILS
        // =====================================================

        // Đơn 1 - Đau đầu
        detailRepo.save(PrescriptionDetail.builder().prescription(pr1).medicine(med1).quantity(20)
                .dosage("2 viên/lần x 2 lần/ngày").durationDays(5).instruction("Uống sau bữa ăn, cách nhau ít nhất 6 tiếng").build());
        detailRepo.save(PrescriptionDetail.builder().prescription(pr1).medicine(med5).quantity(14)
                .dosage("1 viên/lần x 1 lần/ngày (buổi tối)").durationDays(14).instruction("Uống vào buổi tối trước khi ngủ").build());
        detailRepo.save(PrescriptionDetail.builder().prescription(pr1).medicine(med7).quantity(14)
                .dosage("1 viên sủi/ngày").durationDays(14).instruction("Hòa tan trong 200ml nước uống ngay").build());

        // Đơn 2 - Tim mạch
        detailRepo.save(PrescriptionDetail.builder().prescription(pr2).medicine(med4).quantity(30)
                .dosage("1 viên/lần x 1 lần/ngày (sáng)").durationDays(30).instruction("Uống vào buổi sáng, cùng giờ mỗi ngày").build());
        detailRepo.save(PrescriptionDetail.builder().prescription(pr2).medicine(med10).quantity(30)
                .dosage("1 viên/lần x 1 lần/ngày (tối)").durationDays(30).instruction("Uống buổi tối sau ăn, tránh ăn bưởi").build());

        // Đơn 3 - Dạ dày
        detailRepo.save(PrescriptionDetail.builder().prescription(pr3).medicine(med3).quantity(56)
                .dosage("1 viên/lần x 2 lần/ngày (sáng-tối)").durationDays(28).instruction("Uống trước bữa ăn 30 phút, nuốt nguyên viên không nhai").build());
        detailRepo.save(PrescriptionDetail.builder().prescription(pr3).medicine(med2).quantity(42)
                .dosage("1 viên/lần x 3 lần/ngày").durationDays(14).instruction("Uống sau bữa ăn, hoàn thành đủ liệu trình kháng sinh").build());
        detailRepo.save(PrescriptionDetail.builder().prescription(pr3).medicine(med1).quantity(20)
                .dosage("1 viên/lần, khi đau").durationDays(null).instruction("Chỉ dùng khi đau, tối đa 4 viên/ngày").build());

        System.out.println("======================================");
        System.out.println("✅ DATA SEEDED SUCCESSFULLY");
        System.out.println("======================================");
        System.out.println("ADMIN:    admin@gmail.com / 123456");
        System.out.println("DOCTOR:   doctor1@gmail.com / 123456");
        System.out.println("PATIENT:  patient1@gmail.com / 123456");
        System.out.println("Seeded: 6 specialties, 5 doctors, 5 patients, 10 medicines");
        System.out.println("        6 appointments, 3 medical records, 3 prescriptions, 8 details");
        System.out.println("======================================");
    }

    private User createUser(String username, String email, Role role, boolean active) {
        return userRepo.save(User.builder().username(username).email(email)
                .password(passwordEncoder.encode("123456")).role(role).active(active).build());
    }

    private UserProfile createProfile(User user, String fullName, String phone,
                                      LocalDate dob, Gender gender, String address) {
        return profileRepo.save(UserProfile.builder().user(user).fullName(fullName)
                .phoneNumber(phone).dateOfBirth(dob).gender(gender).address(address).build());
    }

    private Doctor createDoctor(User user, Specialty specialty, String licenseNumber,
                                int experienceYears, String description, BigDecimal fee) {
        return doctorRepo.save(Doctor.builder().user(user).specialty(specialty)
                .licenseNumber(licenseNumber).experienceYears(experienceYears)
                .description(description).consultationFee(fee).build());
    }

    private Appointment createAppointment(User patient, Doctor doctor, LocalDate date,
                                          LocalTime time, AppointmentStatus status, String reason) {
        return appointmentRepo.save(Appointment.builder().patient(patient).doctor(doctor)
                .appointmentDate(date).appointmentTime(time).status(status).reason(reason).build());
    }
}