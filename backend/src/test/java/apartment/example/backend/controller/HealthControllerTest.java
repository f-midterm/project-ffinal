package apartment.example.backend.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HealthControllerTest {

    private HealthController healthController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        healthController = new HealthController();
        mockMvc = MockMvcBuilders.standaloneSetup(healthController).build();
    }

    @Test
    void health_ShouldReturnStatusUp() {
        // When
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
        assertThat(response.getBody().get("service")).isEqualTo("apartment-backend");
        assertThat(response.getBody().get("version")).isEqualTo("1.0.0");
        assertThat(response.getBody().get("timestamp")).isInstanceOf(LocalDateTime.class);
    }

    @Test
    void health_ShouldContainAllRequiredFields() {
        // When
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Then
        assertThat(response.getBody()).containsKeys("status", "timestamp", "service", "version");
    }

    @Test
    void health_ShouldReturnCurrentTimestamp() {
        // Given
        LocalDateTime before = LocalDateTime.now();

        // When
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Then
        LocalDateTime after = LocalDateTime.now();
        LocalDateTime timestamp = (LocalDateTime) response.getBody().get("timestamp");

        assertThat(timestamp).isAfterOrEqualTo(before);
        assertThat(timestamp).isBeforeOrEqualTo(after);
    }

    @Test
    void ready_ShouldReturnStatusReady() {
        // When
        ResponseEntity<Map<String, Object>> response = healthController.ready();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("READY");
        assertThat(response.getBody().get("timestamp")).isInstanceOf(LocalDateTime.class);
    }

    @Test
    void ready_ShouldContainAllRequiredFields() {
        // When
        ResponseEntity<Map<String, Object>> response = healthController.ready();

        // Then
        assertThat(response.getBody()).containsKeys("status", "timestamp");
    }

    @Test
    void ready_ShouldReturnCurrentTimestamp() {
        // Given
        LocalDateTime before = LocalDateTime.now();

        // When
        ResponseEntity<Map<String, Object>> response = healthController.ready();

        // Then
        LocalDateTime after = LocalDateTime.now();
        LocalDateTime timestamp = (LocalDateTime) response.getBody().get("timestamp");

        assertThat(timestamp).isAfterOrEqualTo(before);
        assertThat(timestamp).isBeforeOrEqualTo(after);
    }

    // Integration tests with MockMvc
    @Test
    void healthEndpoint_ShouldReturnOkStatus() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("apartment-backend"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void readyEndpoint_ShouldReturnOkStatus() throws Exception {
        mockMvc.perform(get("/health/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void healthEndpoint_ShouldReturnJsonContentType() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void readyEndpoint_ShouldReturnJsonContentType() throws Exception {
        mockMvc.perform(get("/health/ready"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
}
