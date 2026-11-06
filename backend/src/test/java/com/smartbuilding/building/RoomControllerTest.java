package com.smartbuilding.building;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbuilding.building.controller.RoomController;
import com.smartbuilding.building.dto.RoomRequest;
import com.smartbuilding.building.dto.RoomResponse;
import com.smartbuilding.building.model.RoomType;
import com.smartbuilding.building.model.SafetyStatus;
import com.smartbuilding.building.service.RoomService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    private UsernamePasswordAuthenticationToken createAuth() {
        UserDetailsImpl user = new UserDetailsImpl(
                "mgr-1", "manager@nexora.com", "Building Manager", "hash",
                Role.BUILDING_MANAGER, true, List.of(new SimpleGrantedAuthority("ROLE_BUILDING_MANAGER"))
        );
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    @DisplayName("GET /api/v1/floors/{id}/rooms returns rooms on floor")
    void getRoomsByFloor_success() throws Exception {
        RoomResponse r1 = new RoomResponse(
                "r1", "f1", 0, "b1", "Main Lobby", "LOBBY-01", RoomType.LOBBY,
                "Ground floor lobby", 50, 12, SafetyStatus.NORMAL,
                0.0, 0.0, 0.0, 15.0, 12.0, 4.0, true, Instant.now(), Instant.now()
        );

        when(roomService.getRoomsByFloor(eq("f1"), any())).thenReturn(List.of(r1));

        mockMvc.perform(get("/api/v1/floors/f1/rooms")
                        .with(authentication(createAuth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].roomCode").value("LOBBY-01"))
                .andExpect(jsonPath("$.data[0].maximumCapacity").value(50))
                .andExpect(jsonPath("$.data[0].posX").value(0.0))
                .andExpect(jsonPath("$.data[0].width").value(15.0));
    }

    @Test
    @DisplayName("POST /api/v1/floors/{id}/rooms creates room with 3D coordinates and dimensions")
    void createRoom_success() throws Exception {
        RoomRequest request = new RoomRequest(
                "Server Room Alpha", "SRV-01", RoomType.SERVER_ROOM, "Data center infrastructure",
                10, 2, SafetyStatus.NORMAL, 10.0, 0.0, 5.0, 8.0, 6.0, 3.5, true
        );

        RoomResponse response = new RoomResponse(
                "r2", "f1", 0, "b1", "Server Room Alpha", "SRV-01", RoomType.SERVER_ROOM,
                "Data center infrastructure", 10, 2, SafetyStatus.NORMAL,
                10.0, 0.0, 5.0, 8.0, 6.0, 3.5, true, Instant.now(), Instant.now()
        );

        when(roomService.createRoom(eq("f1"), any(RoomRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/floors/f1/rooms")
                        .with(authentication(createAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.roomCode").value("SRV-01"))
                .andExpect(jsonPath("$.data.posX").value(10.0))
                .andExpect(jsonPath("$.data.posY").value(0.0))
                .andExpect(jsonPath("$.data.posZ").value(5.0))
                .andExpect(jsonPath("$.data.width").value(8.0))
                .andExpect(jsonPath("$.data.depth").value(6.0))
                .andExpect(jsonPath("$.data.height").value(3.5));
    }

    @Test
    @DisplayName("POST /api/v1/floors/{id}/rooms rejects duplicate roomCode on floor")
    void createRoom_duplicateRoomCode() throws Exception {
        RoomRequest request = new RoomRequest(
                "Duplicate Room", "SRV-01", RoomType.SERVER_ROOM, null,
                10, 0, SafetyStatus.NORMAL, 0.0, 0.0, 0.0, 5.0, 5.0, 3.0, true
        );

        when(roomService.createRoom(eq("f1"), any(RoomRequest.class), any()))
                .thenThrow(ServiceException.conflict("Room code 'SRV-01' already exists on this floor"));

        mockMvc.perform(post("/api/v1/floors/f1/rooms")
                        .with(authentication(createAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Room code 'SRV-01' already exists on this floor"));
    }
}
