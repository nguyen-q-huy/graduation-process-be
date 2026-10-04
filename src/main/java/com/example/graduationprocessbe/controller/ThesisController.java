package com.example.graduationprocessbe.controller;

import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import com.example.graduationprocessbe.dto.PageResponse;
import com.example.graduationprocessbe.dto.request.CreateThesisRequest;
import com.example.graduationprocessbe.dto.request.UpdateThesisRequest;
import com.example.graduationprocessbe.dto.response.ActivityHistoryResponse;
import com.example.graduationprocessbe.dto.response.AuditLogResponse;
import com.example.graduationprocessbe.dto.response.MemberResponse;
import com.example.graduationprocessbe.dto.response.TaskResponse;
import com.example.graduationprocessbe.dto.response.ThesisResponse;
import com.example.graduationprocessbe.exception.ResourceNotFoundException;
import com.example.graduationprocessbe.service.AuditLogService;
import com.example.graduationprocessbe.service.ThesisProcessService;
import com.example.graduationprocessbe.service.ThesisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.graduationprocessbe.util.ApiResponses.ok;

@RestController
@RequestMapping("/api/theses")
@RequiredArgsConstructor
public class ThesisController {

    private final ThesisProcessService thesisProcessService;
    private final ThesisService thesisService;
    private final AuditLogService auditLogService;

    /** Tạo đề tài và start quy trình. */
    @PostMapping
    public ResponseEntity<ApiResponseWrapper<ThesisResponse>> createThesis(
            @RequestBody @Valid CreateThesisRequest request) {
        return ok(thesisProcessService.createThesis(request));
    }

    /** GET /api/theses?keyword=&studentId=&lecturerId=&status=&phaseId=&page=0&size=10&sortBy=createdDate&direction=desc */
    @GetMapping
    public ResponseEntity<ApiResponseWrapper<PageResponse<ThesisResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String lecturerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String phaseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ok(thesisService.search(keyword, studentId, lecturerId, status, phaseId,
                page, size, sortBy, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<ThesisResponse>> getThesis(@PathVariable String id) {
        return ok(thesisProcessService.getThesis(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<ThesisResponse>> update(
            @PathVariable String id, @RequestBody @Valid UpdateThesisRequest request) {
        return ok(thesisService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<Void>> delete(@PathVariable String id) {
        thesisService.delete(id);
        return ok(null);
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<ApiResponseWrapper<List<TaskResponse>>> getThesisTasks(@PathVariable String id) {
        return ok(thesisProcessService.getThesisTasks(id));
    }

    /** Các bước process đã/đang đi qua (theo Flowable). */
    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponseWrapper<List<ActivityHistoryResponse>>> getHistory(@PathVariable String id) {
        return ok(thesisService.getHistory(id));
    }

    /** Audit log của đề tài, phân trang. */
    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<ApiResponseWrapper<PageResponse<AuditLogResponse>>> getAuditLogs(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        String processInstanceId = thesisProcessService.getThesis(id).getProcessInstanceId();
        if (processInstanceId == null) {
            throw new ResourceNotFoundException("Thesis has no process instance: " + id);
        }
        return ok(auditLogService.search(processInstanceId, null, null, null, null,
                page, size, sortBy, direction));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponseWrapper<List<MemberResponse>>> getMembers(@PathVariable String id) {
        return ok(thesisService.getMembers(id));
    }

    @PostMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponseWrapper<List<MemberResponse>>> addMember(
            @PathVariable String id, @PathVariable String userId) {
        return ok(thesisService.addMember(id, userId));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponseWrapper<List<MemberResponse>>> removeMember(
            @PathVariable String id, @PathVariable String userId) {
        return ok(thesisService.removeMember(id, userId));
    }
}
