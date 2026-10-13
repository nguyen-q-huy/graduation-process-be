package com.example.graduationprocessbe.controller;

import com.example.graduationprocessbe.service.PdfTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class PdfReportController {

    private static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private final PdfTemplateService pdfTemplateService;

    /** GET /api/reports/download/{id}: the thesis report as a PDF attachment. */
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        byte[] pdf = pdfTemplateService.generateThesisReport(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("report-" + id + ".pdf", StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(pdf);
    }

    /**
     * POST /api/reports/convert: converts the Word file in the request body (already filled from a template) into a PDF.
     * Used by the proposal / explanation export, so the PDF keeps the text selectable and the Word layout.
     */
    @PostMapping(value = "/convert", consumes = {DOCX, MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public ResponseEntity<byte[]> convert(@RequestBody byte[] docx) {
        byte[] pdf = pdfTemplateService.convertDocxToPdf(docx);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(pdf);
    }
}
