package com.example.hospital_wed2.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    // BUG-03: Đọc từ config thay vì hardcode Windows path
    @Value("${app.upload.dir}")
    private String uploadDirPath;

    private Path uploadDir;

    // BUG-04: Chỉ cho phép các loại ảnh
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    @PostConstruct
    public void init() {
        this.uploadDir = Paths.get(uploadDirPath);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục upload: " + uploadDirPath, e);
        }
    }

    public String storeFile(MultipartFile file) {
        // BUG-04: Validate loại file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được rỗng");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh JPEG, PNG, WebP hoặc GIF");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 2MB");
        }

        // BUG-19: Dùng UUID làm tên file, không dùng original filename
        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file"
        );
        String extension = "";
        if (originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            // Chỉ cho phép extension ảnh hợp lệ
            if (!List.of(".jpg", ".jpeg", ".png", ".webp", ".gif").contains(extension)) {
                extension = ".jpg";
            }
        }

        String newFileName = UUID.randomUUID().toString() + extension;
        Path target = uploadDir.resolve(newFileName).normalize();

        // BUG-19: Kiểm tra target nằm trong uploadDir (chống path traversal)
        if (!target.startsWith(uploadDir)) {
            throw new SecurityException("Tên file không hợp lệ");
        }

        try {
            Files.copy(file.getInputStream(), target);
        } catch (IOException e) {
            throw new RuntimeException("Upload file thất bại", e);
        }
        return newFileName;
    }

    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isBlank()) return;
        // BUG-19: Kiểm tra path traversal khi xóa file
        Path target = uploadDir.resolve(fileName).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new SecurityException("Đường dẫn file không hợp lệ");
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new RuntimeException("Xóa file thất bại", e);
        }
    }
}
