package com.example.graduationprocessbe.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.regex.Pattern;

/** Stores student PDF attachments on disk under {@code app.storage.dir}/{thesisId}/. */
@Service
public class ThesisAttachmentService {

    private static final Pattern STORED_NAME = Pattern.compile("[0-9a-f-]{36}_[A-Za-z0-9._-]{1,100}[.]pdf");
    private static final long MAX_BYTES = 20L * 1024 * 1024;

    private final Path root;

    public ThesisAttachmentService(@Value("${app.storage.dir:./uploads}") String dir) {
        this.root = Path.of(dir).toAbsolutePath().normalize();
    }

    public String store(String thesisId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Chưa chọn file");
        if (file.getSize() > MAX_BYTES) throw new IllegalArgumentException("File tối đa 20MB");
        byte[] head = new byte[5];
        try (InputStream in = file.getInputStream()) {
            if (in.readNBytes(head, 0, 5) < 5 || !"%PDF-".equals(new String(head, StandardCharsets.US_ASCII)))
                throw new IllegalArgumentException("Chỉ chấp nhận file PDF");
        }
        String base = java.util.Objects.toString(file.getOriginalFilename(), "bao-cao.pdf");
        base = base.replaceAll("(?i)[.]pdf$", "").replaceAll("[^A-Za-z0-9._-]", "_");
        if (base.length() > 80) base = base.substring(0, 80);
        if (base.isBlank()) base = "bao-cao";
        String stored = UUID.randomUUID() + "_" + base + ".pdf";
        Path dir = thesisDir(thesisId);
        Files.createDirectories(dir);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dir.resolve(stored), StandardCopyOption.REPLACE_EXISTING);
        }
        return stored;
    }

    public Resource load(String thesisId, String storedName) {
        if (!STORED_NAME.matcher(storedName).matches()) throw new IllegalArgumentException("Tên file không hợp lệ");
        Path file = thesisDir(thesisId).resolve(storedName).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file))
            throw new com.example.graduationprocessbe.exception.ResourceNotFoundException("Không tìm thấy file");
        return new FileSystemResource(file);
    }

    public static String urlOf(String thesisId, String storedName) {
        return "/api/theses/" + thesisId + "/attachments/" + storedName;
    }

    private Path thesisDir(String thesisId) {
        if (!thesisId.matches("[0-9a-zA-Z-]{1,36}")) throw new IllegalArgumentException("Mã đề tài không hợp lệ");
        return root.resolve(thesisId);
    }
}
