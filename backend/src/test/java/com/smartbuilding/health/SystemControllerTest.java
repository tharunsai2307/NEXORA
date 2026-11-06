package com.smartbuilding.health;

import com.smartbuilding.config.SecurityConfig;
import com.smartbuilding.health.controller.SystemController;
import com.smartbuilding.health.dto.SystemInfoResponse;
import com.smartbuilding.health.service.HealthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SystemController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class SystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthService healthService;

    @MockBean
    private com.smartbuilding.building.service.DashboardStatsService dashboardStatsService;

    @Test
    @DisplayName("GET /api/v1/system/info returns system status and metadata")
    void getSystemInfo_returnsOk() throws Exception {
        SystemInfoResponse mockResponse = new SystemInfoResponse(
                "smart-building-backend",
                "1.0.0-SNAPSHOT",
                "UP",
                "UP",
                "UP",
                "dev",
                Map.of("database", "PostgreSQL (Connected)", "aiEngine", "smart-building-ai (Operational)"),
                Instant.now()
        );

        when(healthService.getSystemInfo()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/system/info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationName").value("smart-building-backend"))
                .andExpect(jsonPath("$.backendStatus").value("UP"))
                .andExpect(jsonPath("$.databaseConnectivityStatus").value("UP"))
                .andExpect(jsonPath("$.aiServiceConnectivityStatus").value("UP"));
    }
}
