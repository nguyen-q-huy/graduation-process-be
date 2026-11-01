package com.example.graduationprocessbe.controller;

import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import com.example.graduationprocessbe.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.example.graduationprocessbe.util.ApiResponses.ok;

/**
 * Word templates for the exported documents (proposal and explanation of changes).
 * Every user can download a template (the browser fills it in); only an admin can replace or reset it.
 */
@RestController
@RequestMapping("/api/document-templates")
@RequiredArgsConstructor
public class DocumentTemplateController {

    private record Default(String name, String resource) {}

    private static final Map<String, Default> DEFAULTS = new LinkedHashMap<>();
    static {
        DEFAULTS.put("DECUONG", new Default("Đề cương chi tiết khóa luận tốt nghiệp", "document-templates/decuong.docx"));
        DEFAULTS.put("GIAITRINH_1", new Default("Giải trình chỉnh sửa (1 sinh viên)", "document-templates/giaitrinh_1.docx"));
        DEFAULTS.put("GIAITRINH_2", new Default("Giải trình chỉnh sửa (2 sinh viên)", "document-templates/giaitrinh_2.docx"));
    }
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final MediaType DOCX = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private final JdbcTemplate jdbc;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ApiResponseWrapper<List<Map<String, Object>>>> list() {
        ensureDefaults();
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT t.code,t.name,t.file_name,t.is_default,t.updated_at,u.full_name AS updated_by "
                        + "FROM document_templates t LEFT JOIN users u ON u.id=t.updated_by ORDER BY t.code");
        rows.forEach(row -> row.put("built_in", DEFAULTS.containsKey((String) row.get("code"))));
        return ok(rows);
    }

    @GetMapping("/{code}/file")
    public ResponseEntity<byte[]> download(@PathVariable String code) {
        ensureDefaults();
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT file_name,content FROM document_templates WHERE code=?", code);
        if (rows.isEmpty()) return ResponseEntity.notFound().build();
        String fileName = (String) rows.getFirst().get("file_name");
        if (fileName == null || rows.getFirst().get("content") == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(DOCX)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString())
                .body((byte[]) rows.getFirst().get("content"));
    }

    @PutMapping("/{code}/file")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<Map<String, Object>>> upload(@PathVariable String code, @RequestParam("file") MultipartFile file) throws IOException {
        ensureDefaults();
        Integer exists = jdbc.queryForObject("SELECT count(*) FROM document_templates WHERE code=?", Integer.class, code);
        if (exists == null || exists == 0) throw new IllegalArgumentException("Mẫu văn bản không tồn tại");
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().trim();
        if (!original.toLowerCase().endsWith(".docx")) throw new IllegalArgumentException("Chỉ nhận tệp Word (.docx)");
        if (file.isEmpty() || file.getSize() > MAX_BYTES) throw new IllegalArgumentException("Tệp trống hoặc lớn hơn 5MB");
        byte[] bytes = file.getBytes();
        requireWordDocument(bytes);
        jdbc.update("UPDATE document_templates SET file_name=?,content=?,is_default=false,updated_by=?,updated_at=now() WHERE code=?",
                original, bytes, currentUserId(), code);
        return ok(Map.of("code", code, "fileName", original));
    }

    /** Adds a new template next to the bundled ones; its tags come from the form settings of the steps that use it. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<Map<String, Object>>> create(@RequestParam String code, @RequestParam String name, @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        ensureDefaults();
        String key = code == null ? "" : code.trim().toUpperCase();
        if (!key.matches("[A-Z][A-Z0-9_]{1,49}")) throw new IllegalArgumentException("Mã mẫu gồm chữ không dấu, số và dấu gạch dưới, bắt đầu bằng chữ (2-50 ký tự)");
        String title = name == null ? "" : name.trim();
        if (title.isEmpty() || title.length() > 150) throw new IllegalArgumentException("Cần tên mẫu (tối đa 150 ký tự)");
        Integer existing = jdbc.queryForObject("SELECT count(*) FROM document_templates WHERE code=?", Integer.class, key);
        if (existing != null && existing > 0) throw new IllegalArgumentException("Mã mẫu đã tồn tại");
        // The Word file can come later: a template may be defined first (code and name) and filled in afterwards.
        String original = null;
        byte[] bytes = null;
        if (file != null && !file.isEmpty()) {
            original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().trim();
            if (!original.toLowerCase().endsWith(".docx")) throw new IllegalArgumentException("Chỉ nhận tệp Word (.docx)");
            if (file.getSize() > MAX_BYTES) throw new IllegalArgumentException("Tệp lớn hơn 5MB");
            bytes = file.getBytes();
            requireWordDocument(bytes);
        }
        jdbc.update("INSERT INTO document_templates(code,name,file_name,content,is_default,updated_by,updated_at) VALUES(?,?,?,?,false,?,now())",
                key, title, original, bytes, currentUserId());
        return ok(Map.of("code", key, "hasFile", bytes != null));
    }

    /** Only templates added by an admin can be deleted, and not while a step still uses one. */
    @DeleteMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<Void>> delete(@PathVariable String code) {
        if (DEFAULTS.containsKey(code)) throw new IllegalArgumentException("Không xóa được mẫu có sẵn của hệ thống");
        Integer used = jdbc.queryForObject("SELECT count(*) FROM workflow_steps WHERE config @> CAST(? AS jsonb)", Integer.class,
                "{\"documents\":[{\"code\":\"" + code.replace("\\", "").replace("\"", "") + "\"}]}");
        if (used != null && used > 0) throw new IllegalArgumentException("Mẫu đang được dùng ở một bước của quy trình, hãy gỡ khỏi bước đó trước khi xóa");
        if (jdbc.update("DELETE FROM document_templates WHERE code=?", code) == 0) throw new IllegalArgumentException("Mẫu văn bản không tồn tại");
        return ok(null);
    }

    @PostMapping("/{code}/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<Map<String, Object>>> reset(@PathVariable String code) throws IOException {
        ensureDefaults();
        Default def = requireKnown(code);
        jdbc.update("UPDATE document_templates SET file_name=?,content=?,is_default=true,updated_by=?,updated_at=now() WHERE code=?",
                fileNameOf(def), readDefault(def), currentUserId(), code);
        return ok(Map.of("code", code));
    }

    private Default requireKnown(String code) {
        Default def = DEFAULTS.get(code);
        if (def == null) throw new IllegalArgumentException("Mẫu văn bản không tồn tại");
        return def;
    }

    private String currentUserId() {
        return currentUserService.getCurrentUser().map(com.example.graduationprocessbe.entity.User::getId).orElse(null);
    }

    private static String fileNameOf(Default def) {
        return def.resource().substring(def.resource().lastIndexOf('/') + 1);
    }

    private static byte[] readDefault(Default def) throws IOException {
        try (InputStream in = new ClassPathResource(def.resource()).getInputStream()) {
            return in.readAllBytes();
        }
    }

    /** A real Word file is a zip that contains word/document.xml. */
    private static void requireWordDocument(byte[] bytes) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(new java.io.ByteArrayInputStream(bytes))) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if ("word/document.xml".equals(entry.getName())) return;
            }
        }
        throw new IllegalArgumentException("Tệp không phải tài liệu Word hợp lệ");
    }

    /**
     * Makes sure the bundled templates exist. A template the admin never replaced follows the bundled file, so a
     * corrected default shipped with a release reaches existing databases; a customised template is never touched.
     */
    private void ensureDefaults() {
        retireLegacyExplanation();
        DEFAULTS.forEach((code, def) -> {
            try {
                byte[] bundled = readDefault(def);
                List<Map<String, Object>> rows = jdbc.queryForList("SELECT is_default,md5(content) AS digest FROM document_templates WHERE code=?", code);
                if (rows.isEmpty()) {
                    jdbc.update("INSERT INTO document_templates(code,name,file_name,content,is_default,updated_at) VALUES(?,?,?,?,true,now())",
                            code, def.name(), fileNameOf(def), bundled);
                } else if (Boolean.TRUE.equals(rows.getFirst().get("is_default")) && !md5(bundled).equals(rows.getFirst().get("digest"))) {
                    jdbc.update("UPDATE document_templates SET file_name=?,content=?,updated_at=now() WHERE code=? AND is_default=true",
                            fileNameOf(def), bundled, code);
                }
            } catch (IOException ex) {
                throw new IllegalStateException("Không đọc được mẫu mặc định " + code, ex);
            }
        });
    }

    /** The single explanation template was split in two; an admin-customised old one carries over to the two-student template. */
    private void retireLegacyExplanation() {
        List<Map<String, Object>> legacy = jdbc.queryForList("SELECT file_name,content,is_default,updated_by,updated_at FROM document_templates WHERE code='GIAITRINH'");
        if (legacy.isEmpty()) return;
        Map<String, Object> row = legacy.getFirst();
        if (Boolean.FALSE.equals(row.get("is_default"))
                && jdbc.queryForObject("SELECT count(*) FROM document_templates WHERE code='GIAITRINH_2'", Integer.class) == 0) {
            jdbc.update("INSERT INTO document_templates(code,name,file_name,content,is_default,updated_by,updated_at) VALUES('GIAITRINH_2',?,?,?,false,?,?)",
                    DEFAULTS.get("GIAITRINH_2").name(), row.get("file_name"), row.get("content"), row.get("updated_by"), row.get("updated_at"));
        }
        jdbc.update("DELETE FROM document_templates WHERE code='GIAITRINH'");
    }

    private static String md5(byte[] bytes) {
        try {
            return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("MD5").digest(bytes));
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
