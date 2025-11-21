package apartment.example.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class HealthControllerTest {

    private final HealthController controller = new HealthController();

    @Test
    void health_shouldReturnStatusUp() {
        ResponseEntity<Map<String, Object>> response = controller.health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();
        assertEquals("UP", body.get("status"));
        assertEquals("apartment-backend", body.get("service"));
        assertEquals("1.0.0", body.get("version"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void ready_shouldReturnStatusReady() {
        ResponseEntity<Map<String, Object>> response = controller.ready();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();
        assertEquals("READY", body.get("status"));
        assertNotNull(body.get("timestamp"));
    }
}
