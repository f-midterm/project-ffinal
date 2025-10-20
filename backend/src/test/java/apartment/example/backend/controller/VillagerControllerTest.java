package apartment.example.backend.controller;

import apartment.example.backend.dto.LeaseResponseDto;
import apartment.example.backend.dto.PaymentResponseDto;
import apartment.example.backend.entity.*;
import apartment.example.backend.entity.enums.*;
import apartment.example.backend.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import apartment.example.backend.dto.MaintenanceRequestDTO;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VillagerControllerTest {

    @Mock
    private TenantService tenantService;

    @Mock
    private LeaseService leaseService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private MaintenanceRequestService maintenanceRequestService;

    @InjectMocks
    private VillagerController villagerController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Tenant testTenant;
    private Unit testUnit;
    private Lease testLease;
    private Payment testPayment;
    private MaintenanceRequest testMaintenanceRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(villagerController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDate/LocalDateTime support

        // Setup test data
        testTenant = new Tenant();
        testTenant.setId(1L);
        testTenant.setFirstName("John");
        testTenant.setLastName("Doe");
        testTenant.setEmail("john.doe@example.com");
        testTenant.setPhone("0812345678");

        testUnit = new Unit();
        testUnit.setId(1L);
        testUnit.setRoomNumber("101");
        testUnit.setFloor(1);
        testUnit.setType("1 Bedroom");
        testUnit.setRentAmount(BigDecimal.valueOf(10000));
        testUnit.setSizeSqm(BigDecimal.valueOf(45.5));
        testUnit.setDescription("Cozy apartment");

        testLease = new Lease();
        testLease.setId(1L);
        testLease.setTenant(testTenant);
        testLease.setUnit(testUnit);
        testLease.setStartDate(LocalDate.now().minusMonths(1));
        testLease.setEndDate(LocalDate.now().plusMonths(11));
        testLease.setRentAmount(BigDecimal.valueOf(10000));
        testLease.setBillingCycle(BillingCycle.valueOf("MONTHLY"));
        testLease.setSecurityDeposit(BigDecimal.valueOf(20000));
        testLease.setCreatedAt(LocalDateTime.now());
        testLease.setUpdatedAt(LocalDateTime.now());

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setLease(testLease);
        testPayment.setPaymentType(PaymentType.valueOf("RENT"));
        testPayment.setAmount(BigDecimal.valueOf(10000));
        testPayment.setDueDate(LocalDate.now());
        testPayment.setPaidDate(LocalDate.now());
        testPayment.setPaymentMethod(PaymentMethod.valueOf("BANK_TRANSFER"));
        testPayment.setStatus(PaymentStatus.PAID);
        testPayment.setReceiptNumber("REC001");
        testPayment.setNotes("Payment for January");
        testPayment.setCreatedAt(LocalDateTime.now());
        testPayment.setUpdatedAt(LocalDateTime.now());

        testMaintenanceRequest = new MaintenanceRequest();
        testMaintenanceRequest.setId(1L);
        testMaintenanceRequest.setTenantId(testTenant.getId());
        testMaintenanceRequest.setUnitId(testUnit.getId());
        testMaintenanceRequest.setTitle("Air conditioner not working");
        testMaintenanceRequest.setDescription("The AC is not cooling properly");
        testMaintenanceRequest.setPriority(MaintenanceRequest.Priority.HIGH);
        testMaintenanceRequest.setCategory(MaintenanceRequest.Category.ELECTRICAL);
        testMaintenanceRequest.setPreferredTime("Morning");
        testMaintenanceRequest.setStatus(MaintenanceRequest.RequestStatus.SUBMITTED);
    }

    // ========== Dashboard Tests ==========

    @Test
    void getVillagerDashboard_Success() throws Exception {
        // Arrange
        when(tenantService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testTenant));
        when(leaseService.getLeasesByTenant(1L)).thenReturn(List.of(testLease));
        when(paymentService.getPaymentsByLease(1L)).thenReturn(List.of(testPayment));

        // Act & Assert
        mockMvc.perform(get("/api/villager/dashboard/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenant.id").value(1))
                .andExpect(jsonPath("$.tenant.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.leases").isArray())
                .andExpect(jsonPath("$.activeLease").exists())
                .andExpect(jsonPath("$.payments").isArray());

        verify(tenantService).findByEmail("john.doe@example.com");
        verify(leaseService).getLeasesByTenant(1L);
        verify(paymentService).getPaymentsByLease(1L);
    }

    @Test
    void getVillagerDashboard_TenantNotFound() throws Exception {
        // Arrange
        when(tenantService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/villager/dashboard/nonexistent@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenant").doesNotExist());

        verify(tenantService).findByEmail("nonexistent@example.com");
        verify(leaseService, never()).getLeasesByTenant(anyLong());
    }

    @Test
    void getVillagerDashboard_ServiceThrowsException() throws Exception {
        // Arrange
        when(tenantService.findByEmail(anyString())).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/villager/dashboard/john.doe@example.com"))
                .andExpect(status().isInternalServerError());
    }

    // ========== Lease Tests ==========

    @Test
    void getActiveLeaseByEmail_Success() throws Exception {
        // Arrange
        when(tenantService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testTenant));
        when(leaseService.getLeasesByTenant(1L)).thenReturn(List.of(testLease));

        // Act & Assert
        mockMvc.perform(get("/api/villager/lease/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rentAmount").value(10000))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.unit.roomNumber").value("101"));

        verify(tenantService).findByEmail("john.doe@example.com");
        verify(leaseService).getLeasesByTenant(1L);
    }

    @Test
    void getActiveLeaseByEmail_TenantNotFound() throws Exception {
        // Arrange
        when(tenantService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/villager/lease/nonexistent@example.com"))
                .andExpect(status().isNotFound());

        verify(tenantService).findByEmail("nonexistent@example.com");
    }

    @Test
    void getActiveLeaseByEmail_NoLeaseFound() throws Exception {
        // Arrange
        when(tenantService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testTenant));
        when(leaseService.getLeasesByTenant(1L)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/villager/lease/john.doe@example.com"))
                .andExpect(status().isNotFound());
    }

    // ========== Payment Tests ==========

    @Test
    void getPaymentsByEmail_Success() throws Exception {
        // Arrange
        when(tenantService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testTenant));
        when(leaseService.getLeasesByTenant(1L)).thenReturn(List.of(testLease));
        when(paymentService.getPaymentsByLease(1L)).thenReturn(List.of(testPayment));

        // Act & Assert
        mockMvc.perform(get("/api/villager/payments/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(10000))
                .andExpect(jsonPath("$[0].status").value("PAID"))
                .andExpect(jsonPath("$[0].tenant.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$[0].unit.roomNumber").value("101"));

        verify(tenantService).findByEmail("john.doe@example.com");
        verify(paymentService).getPaymentsByLease(1L);
    }

    @Test
    void getPaymentsByEmail_TenantNotFound() throws Exception {
        // Arrange
        when(tenantService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/villager/payments/nonexistent@example.com"))
                .andExpect(status().isNotFound());
    }

    // ========== Maintenance Request Tests ==========

    @Test
    void getVillagerMaintenanceRequests_Success() throws Exception {
        // Arrange
        when(tenantService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testTenant));
        when(maintenanceRequestService.getRequestsByTenantId(1L)).thenReturn(List.of(testMaintenanceRequest));

        // Act & Assert
        mockMvc.perform(get("/api/villager/maintenance/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Air conditioner not working"));

        verify(maintenanceRequestService).getRequestsByTenantId(1L);
    }

    @Test
    void getVillagerMaintenanceRequests_TenantNotFound() throws Exception {
        // Arrange
        when(tenantService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/villager/maintenance/nonexistent@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createMaintenanceRequest_Success() throws Exception {
        // Arrange
        MaintenanceRequestDTO requestDTO = new MaintenanceRequestDTO();
        requestDTO.setUrgency("EMERGENCY");
        requestDTO.setTenantId(1L);
        requestDTO.setUnitId(101L);
        requestDTO.setTitle("Water leak");
        requestDTO.setDescription("Bathroom sink is leaking");
        requestDTO.setPriority("HIGH");
        requestDTO.setCategory("PLUMBING");
        requestDTO.setPreferredTime("Afternoon");

        when(tenantService.findByEmail(any())).thenReturn(Optional.of(testTenant));
        when(leaseService.getLeasesByTenant(anyLong())).thenReturn(List.of(testLease));
        when(maintenanceRequestService.createMaintenanceRequest(any(MaintenanceRequest.class)))
                .thenReturn(testMaintenanceRequest);

        // Act & Assert
        mockMvc.perform(post("/api/villager/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        verify(maintenanceRequestService).createMaintenanceRequest(any(MaintenanceRequest.class));
    }

    @Test
    void createMaintenanceRequest_TenantNotFound() throws Exception {
        // Arrange
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("email", "nonexistent@example.com");
        requestData.put("title", "Water leak");

        when(tenantService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/villager/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createMaintenanceRequest_NoActiveLease() throws Exception {
        // Arrange
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("email", "john.doe@example.com");
        requestData.put("title", "Water leak");

        when(tenantService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testTenant));
        when(leaseService.getLeasesByTenant(1L)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(post("/api/villager/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getVillagerMaintenanceStats_Success() throws Exception {
        // Arrange
        MaintenanceRequest req1 = new MaintenanceRequest();
        req1.setStatus(MaintenanceRequest.RequestStatus.SUBMITTED);

        MaintenanceRequest req2 = new MaintenanceRequest();
        req2.setStatus(MaintenanceRequest.RequestStatus.IN_PROGRESS);

        MaintenanceRequest req3 = new MaintenanceRequest();
        req3.setStatus(MaintenanceRequest.RequestStatus.COMPLETED);

        List<MaintenanceRequest> requests = Arrays.asList(req1, req2, req3);

        when(tenantService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testTenant));
        when(maintenanceRequestService.getRequestsByTenantId(1L)).thenReturn(requests);

        // Act & Assert
        mockMvc.perform(get("/api/villager/maintenance/stats/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.pending").value(1))
                .andExpect(jsonPath("$.inProgress").value(1))
                .andExpect(jsonPath("$.completed").value(1))
                .andExpect(jsonPath("$.requests").isArray());

        verify(maintenanceRequestService).getRequestsByTenantId(1L);
    }

    @Test
    void getVillagerMaintenanceStats_TenantNotFound() throws Exception {
        // Arrange
        when(tenantService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/villager/maintenance/stats/nonexistent@example.com"))
                .andExpect(status().isNotFound());
    }

    // ========== Direct Controller Method Tests ==========

    @Test
    void getVillagerDashboard_DirectCall_Success() {
        // Arrange
        when(tenantService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testTenant));
        when(leaseService.getLeasesByTenant(1L)).thenReturn(List.of(testLease));
        when(paymentService.getPaymentsByLease(1L)).thenReturn(List.of(testPayment));

        // Act
        ResponseEntity<Map<String, Object>> response = villagerController.getVillagerDashboard("john.doe@example.com");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("tenant"));
        assertTrue(response.getBody().containsKey("leases"));
        assertTrue(response.getBody().containsKey("activeLease"));
        assertTrue(response.getBody().containsKey("payments"));
    }

    @Test
    void getActiveLeaseByEmail_DirectCall_Success() {
        // Arrange
        when(tenantService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testTenant));
        when(leaseService.getLeasesByTenant(1L)).thenReturn(List.of(testLease));

        // Act
        ResponseEntity<LeaseResponseDto> response = villagerController.getActiveLeaseByEmail("john.doe@example.com");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("101", response.getBody().getUnit().getRoomNumber());
    }

    @Test
    void getPaymentsByEmail_DirectCall_Success() {
        // Arrange
        when(tenantService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testTenant));
        when(leaseService.getLeasesByTenant(1L)).thenReturn(List.of(testLease));
        when(paymentService.getPaymentsByLease(1L)).thenReturn(List.of(testPayment));

        // Act
        ResponseEntity<List<PaymentResponseDto>> response = villagerController.getPaymentsByEmail("john.doe@example.com");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        assertEquals("john.doe@example.com", response.getBody().get(0).getTenant().getEmail());
    }
}