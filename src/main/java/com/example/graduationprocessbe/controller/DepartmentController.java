package com.example.graduationprocessbe.controller;

import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import com.example.graduationprocessbe.dto.PageResponse;
import com.example.graduationprocessbe.dto.request.CreateDepartmentRequest;
import com.example.graduationprocessbe.dto.request.UpdateDepartmentRequest;
import com.example.graduationprocessbe.dto.response.DepartmentResponse;
import com.example.graduationprocessbe.service.DepartmentService;
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

import static com.example.graduationprocessbe.util.ApiResponses.ok;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<ApiResponseWrapper<DepartmentResponse>> create(
            @RequestBody @Valid CreateDepartmentRequest request) {
        return ok(departmentService.create(request));
    }

    /** GET /api/departments?keyword=&page=0&size=10&sortBy=deptCode&direction=asc */
    @GetMapping
    public ResponseEntity<ApiResponseWrapper<PageResponse<DepartmentResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ok(departmentService.search(keyword, page, size, sortBy, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<DepartmentResponse>> getById(@PathVariable String id) {
        return ok(departmentService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<DepartmentResponse>> update(
            @PathVariable String id, @RequestBody @Valid UpdateDepartmentRequest request) {
        return ok(departmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<Void>> delete(@PathVariable String id) {
        departmentService.delete(id);
        return ok(null);
    }
}
