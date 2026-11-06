package com.smartbuilding.building;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbuilding.building.controller.FloorController;
import com.smartbuilding.building.dto.FloorRequest;
import com.smartbuilding.building.dto.FloorResponse;
import com.smartbuilding.building.model.FloorStatus;
import com.smartbuilding.building.service.FloorService;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.security.UserDetailsImpl;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FloorController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class FloorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FloorService floorService;

    private UsernamePasswordAuthenticationToken createAuth() {
        UserDetailsImpl user = new UserDetailsImpl(
                "mgr-1", "manager@nexora.com", "Building Manager", "hash",
                Role.BUILDING_MANAGER, true, List.of(new SimpleGrantedAuthority("ROLE_BUILDING_MANAGER"))
        );
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    @DisplayName("GET /api/v1/buildings/{id}/floors returns all floors for building")
    void getFloorsByBuilding_success() throws Exception {
        FloorResponse f1 = new FloorResponse(
                "f1", "b1", "Ground Level", 0, "Lobby and reception",
                60.0, 40.0, FloorStatus.ACTIVE, 3, 100, 25, Instant.now(), Instant.now()
        );

        when(floorService.getFloorsByBuilding(eq("b1"), any())).thenReturn(List.of(f1));

        mockMvc.perform(get("/api/v1/buildings/b1/floors")
                        .with(authentication(createAuth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].floorNumber").value(0));
    }

    @Test
    @DisplayName("POST /api/v1/buildings/{id}/floors creates new floor")
    void createFloor_success() throws Exception {
        FloorRequest request = new FloorRequest(
                "Level 1 - Engineering", 1, "R&D Office Space",
                60.0, 40.0, FloorStatus.ACTIVE
        );

        FloorResponse response = new FloorResponse(
                "f2", "b1", "Level 1 - Engineering", 1, "R&D Office Space",
                60.0, 40.0, FloorStatus.ACTIVE, 0, 0, 0, Instant.now(), Instant.now()
        );

        when(floorService.createFloor(eq("b1"), any(FloorRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/buildings/b1/floors")
                        .with(authentication(createAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.floorName").value("Level 1 - Engineering"));
    }

    @Test
    @DisplayName("POST /api/v1/buildings/{id}/floors rejects duplicate floor number")
    void createFloor_duplicateNumber() throws Exception {
        FloorRequest request = new FloorRequest(
                "Duplicate Floor", 1, null, 60.0, 40.0, FloorStatus.ACTIVE
        );

        when(floorService.createFloor(eq("b1"), any(FloorRequest.class), any()))
                .thenThrow(ServiceException.conflict("Floor number 1 already exists in this building"));

        mockMvc.perform(post("/api/v1/buildings/b1/floors")
                        .with(authentication(createAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Floor number 1 already exists in this building"));
    }

    @Test
    @DisplayName("DELETE /api/v1/floors/{id} rejects deletion when rooms exist")
    void deleteFloor_rejectsWhenRoomsExist() throws Exception {
        doThrow(ServiceException.badRequest("Cannot delete floor with 3 existing rooms. Please remove all rooms first."))
                .when(floorService).deleteFloor(eq("f1"), any());

        mockMvc.perform(delete("/api/v1/floors/f1")
                        .with(authentication(createAuth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete floor with 3 existing rooms. Please remove all rooms first."));
    }
}
