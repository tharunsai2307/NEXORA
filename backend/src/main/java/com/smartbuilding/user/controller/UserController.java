package com.smartbuilding.user.controller;

import com.smartbuilding.common.ApiResponse;
import com.smartbuilding.security.UserDetailsImpl;
import com.smartbuilding.user.dto.UpdateUserRoleRequest;
import com.smartbuilding.user.dto.UpdateUserStatusRequest;
import com.smartbuilding.user.dto.UserResponse;
import com.smartbuilding.user.model.Role;
import com.smartbuilding.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> listUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active) {
        List<UserResponse> users = userService.listUsers(query, role, active);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRoleRequest request,
            @AuthenticationPrincipal UserDetailsImpl adminPrincipal) {
        String adminId = adminPrincipal != null ? adminPrincipal.getId() : "SYSTEM";
        UserResponse updatedUser = userService.updateUserRole(id, request.role(), adminId);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", updatedUser));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            @AuthenticationPrincipal UserDetailsImpl adminPrincipal) {
        String adminId = adminPrincipal != null ? adminPrincipal.getId() : "SYSTEM";
        UserResponse updatedUser = userService.updateUserStatus(id, request.active(), adminId);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", updatedUser));
    }
}

