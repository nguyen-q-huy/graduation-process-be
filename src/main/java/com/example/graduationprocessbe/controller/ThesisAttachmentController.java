package com.example.graduationprocessbe.controller;

import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import com.example.graduationprocessbe.service.ThesisAttachmentService;
import com.example.graduationprocessbe.service.ThesisProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static com.example.graduationprocessbe.util.ApiResponses.ok;

@RestController
@RequestMapping("/api/theses/{id}/attachments")
@RequiredArgsConstructor
public class ThesisAttachmentController {

    private final ThesisProcessService thesisProcessService;
    private final ThesisAttachmentService attachments;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;
    private final com.example.graduationprocessbe.service.CurrentUserService currentUserService;

    /** Uploads a PDF; the returned {@code url} goes into {@code attachmentUrl} when the SUBMIT task is completed. */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponseWrapper<Map<String, String>>> upload(
            @PathVariable String id, @RequestParam("file") MultipartFile file) throws IOException {
        thesisProcessService.getThesis(id); // same access check as reading the thesis
        var actor = currentUserService.getCurrentUser().orElseThrow();
        Integer locked = jdbc.queryForObject("SELECT count(*) FROM members WHERE thesis_id=? AND user_id=? AND locked=true", Integer.class, id, actor.getId());
        if (locked != null && locked > 0) throw new org.springframework.security.access.AccessDeniedException("Trưởng nhóm đã khóa quyền nộp của bạn. Bạn vẫn xem được hồ sơ.");
        String stored = attachments.store(id, file);
        return ok(Map.of("url", ThesisAttachmentService.urlOf(id, stored), "fileName", stored.substring(37)));
    }

    @GetMapping("/{name:.+}")
    public ResponseEntity<Resource> download(@PathVariable String id, @PathVariable String name) {
        thesisProcessService.getThesis(id);
        Resource file = attachments.load(id, name);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(name.substring(37)).build().toString())
                .body(file);
    }
}
