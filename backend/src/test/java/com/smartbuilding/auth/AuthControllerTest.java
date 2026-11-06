package com.smartbuilding.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbuilding.auth.controller.AuthController;
import com.smartbuilding.auth.dto.AuthResponse;
import com.smartbuilding.auth.dto.LoginRequest;
import com.smartbuilding.auth.dto.RegisterRequest;
import com.smartbuilding.auth.service.AuthService;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.security.UserDetailsImpl;
import com.smartbuilding.user.dto.UserResponse;
import com.smartbuilding.user.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/v1/auth/register succeeds with valid details")
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Alex Rivera",
                "alex@nexora.com",
                "Password123!",
                "Password123!",
                "+1-555-0199"
        );

        UserResponse userResponse = new UserResponse(
                "usr-1", "Alex Rivera", "alex@nexora.com", "+1-555-0199",
                Role.BUILDING_MANAGER, true, Instant.now(), Instant.now()
        );
        AuthResponse authResponse = AuthResponse.of("mock-jwt-token", 86400000, userResponse);

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.user.role").value("BUILDING_MANAGER"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register rejects mismatched passwords")
    void register_mismatchedPasswords() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Alex Rivera",
                "alex@nexora.com",
                "Password123!",
                "DifferentPassword!",
                null
        );

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(ServiceException.badRequest("Password and confirmation password do not match"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Password and confirmation password do not match"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login succeeds with valid credentials")
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest("alex@nexora.com", "Password123!");

        UserResponse userResponse = new UserResponse(
                "usr-1", "Alex Rivera", "alex@nexora.com", null,
                Role.BUILDING_MANAGER, true, Instant.now(), Instant.now()
        );
        AuthResponse authResponse = AuthResponse.of("mock-jwt-token", 86400000, userResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-jwt-token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login rejects invalid credentials with 401")
    void login_invalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("alex@nexora.com", "WrongPassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(ServiceException.unauthorized("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me returns current user profile when authenticated")
    void getMe_authenticated() throws Exception {
        UserDetailsImpl principal = new UserDetailsImpl(
                "usr-1", "alex@nexora.com", "Alex Rivera", "hash",
                Role.BUILDING_MANAGER, true, List.of(new SimpleGrantedAuthority("ROLE_BUILDING_MANAGER"))
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        UserResponse userResponse = new UserResponse(
                "usr-1", "Alex Rivera", "alex@nexora.com", null,
                Role.BUILDING_MANAGER, true, Instant.now(), Instant.now()
        );
        when(authService.getCurrentUser("alex@nexora.com")).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/auth/me")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("alex@nexora.com"));
    }
}
