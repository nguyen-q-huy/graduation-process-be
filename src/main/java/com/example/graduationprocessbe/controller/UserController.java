package com.example.graduationprocessbe.controller;

import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import com.example.graduationprocessbe.dto.request.CreateUserRequest;
import com.example.graduationprocessbe.dto.response.PageResponse;
import com.example.graduationprocessbe.dto.response.UserResponse;
import com.example.graduationprocessbe.exception.ResponseDetails;
import com.example.graduationprocessbe.service.UserService;
import com.example.graduationprocessbe.util.PaginationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponseWrapper<?>> getAllUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String userType) {
        List<UserResponse> users = userService.getAllUsers();
        String keyword = PaginationUtils.normalize(search);
        String type = PaginationUtils.normalize(userType);

        List<UserResponse> filtered = users.stream()
                .filter(user -> keyword.isBlank()
                        || PaginationUtils.normalize(user.getFullName()).contains(keyword)
                        || PaginationUtils.normalize(user.getEmail()).contains(keyword)
                        || PaginationUtils.normalize(user.getUsername()).contains(keyword))
                .filter(user -> type.isBlank()
                        || "all".equals(type)
                        || PaginationUtils.normalize(user.getUserType()).equals(type))
                .toList();

        Object data = page == null && size == null
                ? filtered
                : PaginationUtils.page(filtered, page, size);

        return ResponseEntity
                .status(ResponseDetails.API_SUCCESSFULLY.getHttpStatus())
                .body(new ApiResponseWrapper<>(ResponseDetails.API_SUCCESSFULLY, data));
    }

    @PostMapping
    public ResponseEntity<ApiResponseWrapper<UserResponse>> createUser(@RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity
                .status(ResponseDetails.API_SUCCESSFULLY.getHttpStatus())
                .body(new ApiResponseWrapper<>(
                        ResponseDetails.API_SUCCESSFULLY,
                        userService.createUser(request)));
    }

    @PostMapping("/{userId}/roles")
    public ResponseEntity<ApiResponseWrapper<Void>> assignRoleToUser(
            @PathVariable String userId,
            @RequestBody @Valid com.example.graduationprocessbe.dto.request.AssignRoleRequest request) {
        userService.assignRoleToUser(userId, request.getRoleId(), request.getThesisRoundId());
        return ResponseEntity
                .status(ResponseDetails.API_SUCCESSFULLY.getHttpStatus())
                .body(new ApiResponseWrapper<>(ResponseDetails.API_SUCCESSFULLY, null));
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponseWrapper<Void>> removeRoleFromUser(
            @PathVariable String userId,
            @PathVariable String roleId) {
        userService.removeRoleFromUser(userId, roleId);
        return ResponseEntity
                .status(ResponseDetails.API_SUCCESSFULLY.getHttpStatus())
                .body(new ApiResponseWrapper<>(ResponseDetails.API_SUCCESSFULLY, null));
    }
}
