package com.example.graduationprocessbe.controller;

import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import com.example.graduationprocessbe.dto.PageResponse;
import com.example.graduationprocessbe.dto.request.CreateRoleRequest;
import com.example.graduationprocessbe.dto.request.UpdateRoleRequest;
import com.example.graduationprocessbe.dto.response.RoleResponse;
import com.example.graduationprocessbe.service.RoleService;
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
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<ApiResponseWrapper<RoleResponse>> create(@RequestBody @Valid CreateRoleRequest request) {
        return ok(roleService.create(request));
    }

    /** GET /api/roles?keyword=&page=0&size=10&sortBy=roleCode&direction=asc */
    @GetMapping
    public ResponseEntity<ApiResponseWrapper<PageResponse<RoleResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ok(roleService.search(keyword, page, size, sortBy, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<RoleResponse>> getById(@PathVariable String id) {
        return ok(roleService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<RoleResponse>> update(
            @PathVariable String id, @RequestBody @Valid UpdateRoleRequest request) {
        return ok(roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<Void>> delete(@PathVariable String id) {
        roleService.delete(id);
        return ok(null);
    }
}
