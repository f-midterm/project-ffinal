package apartment.example.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // <-- บอกให้ Spring Boot ใช้ application-test.properties
@Transactional // <-- ทำให้ข้อมูลที่สร้างในแต่ละเทสไม่ถูกบันทึกจริงหลังเทสจบ
public class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc; // เครื่องมือสำหรับยิง HTTP request จำลอง

    @Autowired
    private ObjectMapper objectMapper; // เครื่องมือสำหรับแปลง Java Object เป็น JSON

    @Test
    void shouldCreateTenantSuccessfully() throws Exception {
        // Arrange: เตรียมข้อมูลที่จะส่งไป
        Map<String, Object> newTenant = new HashMap<>();
        newTenant.put("firstName", "Apiwat");
        newTenant.put("lastName", "Test");
        newTenant.put("email", "apiwat.test@example.com");
        newTenant.put("phone", "0812345678");

        // Act & Assert: ยิง request และตรวจสอบผลลัพธ์
        mockMvc.perform(post("/api/tenants") // สมมติว่า Endpoint ของคุณคือ /api/tenants
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTenant)))
                .andExpect(status().isCreated()) // ตรวจสอบว่า HTTP Status คือ 201 Created
                .andExpect(jsonPath("$.id").exists()) // ตรวจสอบว่า Response JSON มี id กลับมา
                .andExpect(jsonPath("$.firstName").value("Apiwat"))
                .andExpect(jsonPath("$.email").value("apiwat.test@example.com"));
    }
}