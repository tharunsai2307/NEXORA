package com.smartbuilding.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.security.UserDetailsImpl;
import com.smartbuilding.user.controller.UserController;
import com.smartbuilding.user.dto.UpdateUserRoleRequest;
import com.smartbuilding.user.dto.UpdateUserStatusRequest;
import com.smartbuilding.user.dto.UserResponse;
import com.smartbuilding.user.model.Role;
import com.smartbuilding.user.service.UserService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UsernamePasswordAuthenticationToken createAdminAuth() {
        UserDetailsImpl admin = new UserDetailsImpl(
                "adm-1", "admin@nexora.com", "System Admin", "hash",
                Role.SYSTEM_ADMIN, true, List.of(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))
        );
        return new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());
    }

    @Test
    @DisplayName("GET /api/v1/users lists users for administrator")
    void listUsers_success() throws Exception {
        UserResponse u1 = new UserResponse("u1", "Admin", "admin@nexora.com", null, Role.SYSTEM_ADMIN, true, Instant.now(), Instant.now());
        UserResponse u2 = new UserResponse("u2", "Manager", "manager@nexora.com", null, Role.BUILDING_MANAGER, true, Instant.now(), Instant.now());

        when(userService.listUsers(any(), any(), any())).thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/api/v1/users")
                        .with(authentication(createAdminAuth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/role updates user role")
    void updateUserRole_success() throws Exception {
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.SAFETY_OFFICER);
        UserResponse updated = new UserResponse("u2", "Manager", "manager@nexora.com", null, Role.SAFETY_OFFICER, true, Instant.now(), Instant.now());

        when(userService.updateUserRole(eq("u2"), eq(Role.SAFETY_OFFICER), any())).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/users/u2/role")
                        .with(authentication(createAdminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("SAFETY_OFFICER"));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/role rejects demoting the last admin")
    void updateUserRole_rejectsLastAdminDemotion() throws Exception {
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.BUILDING_MANAGER);

        when(userService.updateUserRole(eq("adm-1"), eq(Role.BUILDING_MANAGER), any()))
                .thenThrow(ServiceException.badRequest("Cannot demote the last active System Administrator"));

        mockMvc.perform(patch("/api/v1/users/adm-1/role")
                        .with(authentication(createAdminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot demote the last active System Administrator"));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/status toggles user active state")
    void updateUserStatus_success() throws Exception {
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(false);
        UserResponse updated = new UserResponse("u2", "Manager", "manager@nexora.com", null, Role.BUILDING_MANAGER, false, Instant.now(), Instant.now());

        when(userService.updateUserStatus(eq("u2"), eq(false), any())).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/users/u2/status")
                        .with(authentication(createAdminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));
    }
}
