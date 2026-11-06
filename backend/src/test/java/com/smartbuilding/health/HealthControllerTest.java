package com.smartbuilding.health;

import com.smartbuilding.ai.dto.AiHealthDto;
import com.smartbuilding.config.SecurityConfig;
import com.smartbuilding.health.controller.HealthController;
import com.smartbuilding.health.dto.BackendHealthResponse;
import com.smartbuilding.health.dto.DatabaseHealthResponse;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthService healthService;

    @Test
    @DisplayName("GET /api/v1/health returns UP status and service identifier")
    void getBackendHealth_returnsOk() throws Exception {
        when(healthService.getBackendHealth())
                .thenReturn(new BackendHealthResponse("UP", "smart-building-backend"));

        mockMvc.perform(get("/api/v1/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("smart-building-backend"));
    }

    @Test
    @DisplayName("GET /api/v1/health/ai returns AI service status")
    void getAiHealth_returnsOk() throws Exception {
        when(healthService.getAiHealth())
                .thenReturn(new AiHealthDto("UP", "smart-building-ai"));

        mockMvc.perform(get("/api/v1/health/ai")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("smart-building-ai"));
    }

    @Test
    @DisplayName("GET /api/v1/health/database returns database status")
    void getDatabaseHealth_returnsOk() throws Exception {
        when(healthService.getDatabaseHealth())
                .thenReturn(new DatabaseHealthResponse(
                        "UP",
                        "PostgreSQL 16",
                        "Database connection pool is healthy and responsive",
                        Instant.now()
                ));

        mockMvc.perform(get("/api/v1/health/database")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").value("PostgreSQL 16"));
    }
}
