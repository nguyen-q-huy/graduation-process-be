package com.example.graduationprocessbe.controller;

import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import com.example.graduationprocessbe.dto.PageResponse;
import com.example.graduationprocessbe.dto.request.CreateUserRequest;
import com.example.graduationprocessbe.dto.request.UpdateUserRequest;
import com.example.graduationprocessbe.dto.response.RoleResponse;
import com.example.graduationprocessbe.dto.response.UserResponse;
import com.example.graduationprocessbe.service.UserService;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponseWrapper<UserResponse>> createUser(@RequestBody @Valid CreateUserRequest request) {
        return ok(userService.createUser(request));
    }

    /** GET /api/users?keyword=&status=&departmentId=&page=0&size=10&sortBy=createdDate&direction=desc */
    @GetMapping
    public ResponseEntity<ApiResponseWrapper<PageResponse<UserResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ok(userService.search(keyword, status, departmentId, page, size, sortBy, direction));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponseWrapper<UserResponse>> me() {
        return ok(userService.getCurrentUser());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<UserResponse>> getById(@PathVariable String id) {
        return ok(userService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<UserResponse>> update(
            @PathVariable String id, @RequestBody @Valid UpdateUserRequest request) {
        return ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<Void>> delete(@PathVariable String id) {
        userService.delete(id);
        return ok(null);
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<ApiResponseWrapper<List<RoleResponse>>> getRoles(@PathVariable String id) {
        return ok(userService.getRoles(id));
    }

    @PostMapping("/{id}/roles/{roleId}")
    public ResponseEntity<ApiResponseWrapper<List<RoleResponse>>> assignRole(
            @PathVariable String id, @PathVariable String roleId) {
        return ok(userService.assignRole(id, roleId));
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    public ResponseEntity<ApiResponseWrapper<List<RoleResponse>>> removeRole(
            @PathVariable String id, @PathVariable String roleId) {
        return ok(userService.removeRole(id, roleId));
    }
}
