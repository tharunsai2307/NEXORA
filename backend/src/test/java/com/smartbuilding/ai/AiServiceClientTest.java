package com.smartbuilding.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbuilding.ai.dto.AiHealthDto;
import com.smartbuilding.ai.dto.AiTestInferenceRequest;
import com.smartbuilding.ai.dto.AiTestInferenceResponse;
import com.smartbuilding.ai.service.AiServiceClient;
import com.smartbuilding.ai.service.AiServiceClientImpl;
import com.smartbuilding.common.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class AiServiceClientTest {

    private RestClient restClient;
    private MockRestServiceServer mockServer;
    private AiServiceClient aiServiceClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8000");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
        aiServiceClient = new AiServiceClientImpl(restClient);
    }

    @Test
    @DisplayName("checkHealth() parses response from AI engine successfully")
    void checkHealth_success() throws Exception {
        AiHealthDto expected = new AiHealthDto("UP", "smart-building-ai");
        mockServer.expect(requestTo("http://localhost:8000/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expected), MediaType.APPLICATION_JSON));

        AiHealthDto actual = aiServiceClient.checkHealth();
        assertThat(actual).isNotNull();
        assertThat(actual.status()).isEqualTo("UP");
        assertThat(actual.service()).isEqualTo("smart-building-ai");
        mockServer.verify();
    }

    @Test
    @DisplayName("checkHealth() throws ServiceException when AI engine returns 500 error")
    void checkHealth_serverError() {
        mockServer.expect(requestTo("http://localhost:8000/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThatThrownBy(() -> aiServiceClient.checkHealth())
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Python AI service is unreachable");
        mockServer.verify();
    }

    @Test
    @DisplayName("isReachable() returns true when AI service responds UP")
    void isReachable_returnsTrue() throws Exception {
        AiHealthDto health = new AiHealthDto("UP", "smart-building-ai");
        mockServer.expect(requestTo("http://localhost:8000/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(health), MediaType.APPLICATION_JSON));

        boolean reachable = aiServiceClient.isReachable();
        assertThat(reachable).isTrue();
        mockServer.verify();
    }

    @Test
    @DisplayName("isReachable() returns false gracefully when AI service fails")
    void isReachable_returnsFalseOnFailure() {
        mockServer.expect(requestTo("http://localhost:8000/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        boolean reachable = aiServiceClient.isReachable();
        assertThat(reachable).isFalse();
        mockServer.verify();
    }

    @Test
    @DisplayName("testInference() sends payload and parses echoed response")
    void testInference_success() throws Exception {
        AiTestInferenceRequest req = new AiTestInferenceRequest(
                "temp-01",
                "temperature",
                21.5,
                "CELSIUS",
                Map.of("room", "101")
        );
        AiTestInferenceResponse expectedResp = new AiTestInferenceResponse(
                "INFERENCE_ECHO_VERIFIED",
                "Phase 1 bridge verified",
                Map.of("sensor_id", "temp-01"),
                null,
                "Phase 1 - Communication Test",
                "smart-building-ai"
        );

        mockServer.expect(requestTo("http://localhost:8000/api/v1/inference/test"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedResp), MediaType.APPLICATION_JSON));

        AiTestInferenceResponse res = aiServiceClient.testInference(req);
        assertThat(res).isNotNull();
        assertThat(res.status()).isEqualTo("INFERENCE_ECHO_VERIFIED");
        mockServer.verify();
    }
}
