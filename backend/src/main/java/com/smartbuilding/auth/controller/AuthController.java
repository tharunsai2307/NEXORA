package com.smartbuilding.auth.controller;

import com.smartbuilding.auth.dto.AuthResponse;
import com.smartbuilding.auth.dto.LoginRequest;
import com.smartbuilding.auth.dto.RegisterRequest;
import com.smartbuilding.auth.service.AuthService;
import com.smartbuilding.common.ApiResponse;
import com.smartbuilding.security.UserDetailsImpl;
import com.smartbuilding.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal,
            Authentication authentication,
            jakarta.servlet.http.HttpServletRequest request) {
        Authentication currentAuth = authentication != null ? authentication : SecurityContextHolder.getContext().getAuthentication();
        String email = null;
        if (userPrincipal != null) {
            email = userPrincipal.getUsername();
        } else if (currentAuth != null && currentAuth.getPrincipal() instanceof UserDetailsImpl principal) {
            email = principal.getUsername();
        } else if (currentAuth != null && currentAuth.getName() != null && !currentAuth.getName().equals("anonymousUser")) {
            email = currentAuth.getName();
        }

        if (email == null && request != null && request.getSession(false) != null) {
            Object secCtx = request.getSession(false).getAttribute("SPRING_SECURITY_CONTEXT");
            if (secCtx instanceof org.springframework.security.core.context.SecurityContext sc && sc.getAuthentication() != null) {
                email = sc.getAuthentication().getName();
            }
        }

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
        }
        UserResponse response = authService.getCurrentUser(email);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", response));
    }
}

