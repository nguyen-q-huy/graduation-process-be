package com.example.graduationprocessbe.controller;
import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import com.example.graduationprocessbe.dto.request.UpdateMenuRequest;
import com.example.graduationprocessbe.exception.ResponseDetails;
import com.example.graduationprocessbe.service.RbacSetupService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
@RestController
@RequestMapping("/api/rbac")
@RequiredArgsConstructor
public class RbacSetupController {
    private final RbacSetupService service;
    public record Member(@NotBlank String userId,@NotBlank String roleId,String thesisRoundId) {}
    public record MenuConfiguration(@NotNull @Valid UpdateMenuRequest menu) {}
    @GetMapping("/setup")
    @PreAuthorize("hasAnyAuthority('VIEW_USERS','VIEW_ROLES','VIEW_MENUS')")
    public ApiResponseWrapper<?> setup(Authentication auth) {
        return new ApiResponseWrapper<>(ResponseDetails.API_SUCCESSFULLY,service.snapshot(auth));
    }
    @PostMapping("/memberships")
    @PreAuthorize("hasAuthority('VIEW_USERS') and hasAuthority('USERS_ASSIGN_ROLE')")
    public ApiResponseWrapper<Void> addMember(@Valid @RequestBody Member request) {
        service.addMember(request.userId(),request.roleId(),request.thesisRoundId()); return ok();
    }
    @DeleteMapping("/memberships/{id}")
    @PreAuthorize("hasAuthority('VIEW_USERS') and hasAuthority('USERS_ASSIGN_ROLE')")
    public ApiResponseWrapper<Void> removeMember(@PathVariable String id) { service.removeMember(id); return ok(); }
    @PutMapping("/menus/{id}/configuration")
    @PreAuthorize("hasAuthority('VIEW_MENUS') and hasAuthority('MENUS_UPDATE')")
    public ApiResponseWrapper<Void> configure(@PathVariable String id,@Valid @RequestBody MenuConfiguration request) {
        service.configureMenu(id,request.menu()); return ok();
    }
    private ApiResponseWrapper<Void> ok() { return new ApiResponseWrapper<>(ResponseDetails.API_SUCCESSFULLY,null); }
}
