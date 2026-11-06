package com.smartbuilding.health;

import com.smartbuilding.ai.service.AiServiceClient;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.health.dto.DatabaseHealthResponse;
import com.smartbuilding.health.service.HealthServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class HealthServiceDatabaseTest {

    @Mock
    private AiServiceClient aiServiceClient;

    @Test
    @DisplayName("getDatabaseHealth() returns UP when connection to database is valid")
    void getDatabaseHealth_whenConnectionIsValid_returnsUp() {
        DriverManagerDataSource validDataSource = new DriverManagerDataSource();
        validDataSource.setDriverClassName("org.h2.Driver");
        validDataSource.setUrl("jdbc:h2:mem:health_test_db;DB_CLOSE_DELAY=-1");
        validDataSource.setUsername("sa");
        validDataSource.setPassword("");

        HealthServiceImpl healthService = new HealthServiceImpl(validDataSource, aiServiceClient);
        DatabaseHealthResponse response = healthService.getDatabaseHealth();

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("UP");
        assertThat(response.database()).contains("H2");
        assertThat(response.message()).contains("healthy and responsive");
    }

    @Test
    @DisplayName("getDatabaseHealth() throws ServiceException when database connection fails")
    void getDatabaseHealth_whenConnectionFails_throwsServiceException() {
        // Pointing to a closed invalid port
        DriverManagerDataSource invalidDataSource = new DriverManagerDataSource();
        invalidDataSource.setDriverClassName("org.h2.Driver");
        invalidDataSource.setUrl("jdbc:h2:tcp://localhost:59999/nonexistent");
        invalidDataSource.setUsername("sa");
        invalidDataSource.setPassword("");

        HealthServiceImpl healthService = new HealthServiceImpl(invalidDataSource, aiServiceClient);

        assertThatThrownBy(healthService::getDatabaseHealth)
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Database connectivity verification failed");
    }
}
