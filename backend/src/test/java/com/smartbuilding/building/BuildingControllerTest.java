package com.smartbuilding.building;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbuilding.building.controller.BuildingController;
import com.smartbuilding.building.dto.BuildingRequest;
import com.smartbuilding.building.dto.BuildingResponse;
import com.smartbuilding.building.model.BuildingStatus;
import com.smartbuilding.building.model.BuildingType;
import com.smartbuilding.building.service.BuildingService;
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

@WebMvcTest(BuildingController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class BuildingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BuildingService buildingService;

    private UsernamePasswordAuthenticationToken createAuth() {
        UserDetailsImpl user = new UserDetailsImpl(
                "mgr-1", "manager@nexora.com", "Building Manager", "hash",
                Role.BUILDING_MANAGER, true, List.of(new SimpleGrantedAuthority("ROLE_BUILDING_MANAGER"))
        );
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    @DisplayName("GET /api/v1/buildings returns list of accessible buildings")
    void getBuildings_success() throws Exception {
        BuildingResponse b1 = new BuildingResponse(
                "b1", "Nexora Tower Alpha", "NEX-TWR-01", BuildingType.COMMERCIAL,
                "100 Innovation Way", 3, BuildingStatus.ACTIVE, "Headquarters",
                7, 240, 68, Instant.now(), Instant.now()
        );

        when(buildingService.getBuildings(any(), any(), any(), any())).thenReturn(List.of(b1));

        mockMvc.perform(get("/api/v1/buildings")
                        .with(authentication(createAuth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].buildingCode").value("NEX-TWR-01"));
    }

    @Test
    @DisplayName("POST /api/v1/buildings creates a new building")
    void createBuilding_success() throws Exception {
        BuildingRequest request = new BuildingRequest(
                "Nexora Tower Beta", "NEX-TWR-02", BuildingType.INDUSTRIAL,
                "200 Innovation Way", BuildingStatus.ACTIVE, "R&D Facility"
        );

        BuildingResponse response = new BuildingResponse(
                "b2", "Nexora Tower Beta", "NEX-TWR-02", BuildingType.INDUSTRIAL,
                "200 Innovation Way", 0, BuildingStatus.ACTIVE, "R&D Facility",
                0, 0, 0, Instant.now(), Instant.now()
        );

        when(buildingService.createBuilding(any(BuildingRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/buildings")
                        .with(authentication(createAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.buildingCode").value("NEX-TWR-02"));
    }

    @Test
    @DisplayName("POST /api/v1/buildings rejects duplicate building code")
    void createBuilding_duplicateCode() throws Exception {
        BuildingRequest request = new BuildingRequest(
                "Nexora Tower Duplicate", "NEX-TWR-01", BuildingType.COMMERCIAL,
                "100 Innovation Way", BuildingStatus.ACTIVE, "Duplicate"
        );

        when(buildingService.createBuilding(any(BuildingRequest.class), any()))
                .thenThrow(ServiceException.conflict("Building code 'NEX-TWR-01' is already in use"));

        mockMvc.perform(post("/api/v1/buildings")
                        .with(authentication(createAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("DELETE /api/v1/buildings/{id} rejects deletion when floors exist")
    void deleteBuilding_rejectsWhenFloorsExist() throws Exception {
        doThrow(ServiceException.badRequest("Cannot delete building with active floors. Please delete all floors first."))
                .when(buildingService).deleteBuilding(eq("b1"), any());

        mockMvc.perform(delete("/api/v1/buildings/b1")
                        .with(authentication(createAuth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete building with active floors. Please delete all floors first."));
    }
}
