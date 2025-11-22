package apartment.example.backend.controller.Integrationtest;


import apartment.example.backend.entity.Unit;
import apartment.example.backend.entity.enums.UnitStatus;
import apartment.example.backend.repository.UnitRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin", roles = {"ADMIN"})
public class UnitControllertest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long savedId;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        // Override JWT properties for testing
        registry.add("jwt.secret", () -> "dGVzdHNlY3JldA==");
        registry.add("jwt.expiration", () -> "3600000");
        
        // Override CORS properties for testing
        registry.add("cors.allowed-origins", () -> "http://localhost:5173");
        
        // Override database properties for Testcontainers
        registry.add("spring.datasource.url", () -> "jdbc:tc:mysql:8.0:///testdb");
        registry.add("spring.datasource.driver-class-name", () -> "org.testcontainers.jdbc.ContainerDatabaseDriver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.sql.init.mode", () -> "never");
        
        registry.add("spring.jpa.show-sql", () -> "true");
    }

    @BeforeEach
    void setup() {
        unitRepository.deleteAll();

        Unit u = new Unit();
        u.setRoomNumber("A101");
        u.setFloor(1);
        u.setStatus(UnitStatus.AVAILABLE);
        u.setRentAmount(BigDecimal.valueOf(8000));
        u.setUnitType("Standard");
        savedId = unitRepository.saveAndFlush(u).getId();
    }

    @Test
    void testGetAllUnits() throws Exception {
        mockMvc.perform(get("/units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomNumber").value("A101"));
    }

    @Test
    void testGetUnitById() throws Exception {
        mockMvc.perform(get("/units/" + savedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("A101"));
    }

    @Test
    void testCreateUnit() throws Exception {
        Unit newUnit = new Unit();
        newUnit.setRoomNumber("102");
        newUnit.setFloor(1);
        newUnit.setStatus(UnitStatus.AVAILABLE);
        newUnit.setRentAmount(BigDecimal.valueOf(9000));

        // --- เพิ่มบรรทัดนี้ ---
        newUnit.setUnitType("Standard");
        // -------------------

        mockMvc.perform(post("/units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUnit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomNumber").value("102"));
    }

    @Test
    void testUpdateUnit() throws Exception {
        Unit update = new Unit();
        update.setRoomNumber("101-UPDATE");
        update.setFloor(1);
        update.setStatus(UnitStatus.MAINTENANCE);
        update.setRentAmount(BigDecimal.valueOf(7000));

        // --- เพิ่มบรรทัดนี้ ---
        update.setUnitType("Standard");
        // -------------------

        mockMvc.perform(put("/units/" + savedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("101-UPDATE"))
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));
    }

    @Test
    void testPatchStatus() throws Exception {
        String json = """
        { "status": "OCCUPIED" }
        """;

        mockMvc.perform(patch("/units/" + savedId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OCCUPIED"));
    }

    @Test
    void testDeleteUnit() throws Exception {
        mockMvc.perform(delete("/units/" + savedId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/units/" + savedId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDashboard() throws Exception {
        mockMvc.perform(get("/units/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUnits").value(1))
                .andExpect(jsonPath("$.availableUnits").value(1));
    }
}
