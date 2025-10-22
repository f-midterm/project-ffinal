package apartment.example.backend.controller;

import apartment.example.backend.entity.*;
import apartment.example.backend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VillagerControllerTest {

    @Mock private TenantService tenantService;
    @Mock private LeaseService leaseService;
    @Mock private PaymentService paymentService;
    @Mock private MaintenanceRequestService maintenanceRequestService;

    @InjectMocks
    private VillagerController villagerController;

    private Tenant tenant;
    private Lease lease;
    private Payment payment;
    private Unit unit;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        tenant = new Tenant();
        tenant.setId(1L);
        tenant.setFirstName("John");
        tenant.setLastName("Doe");
        tenant.setEmail("john@example.com");
        tenant.setPhone("123456789");

        unit = new Unit();
        unit.setId(10L);
        unit.setRoomNumber("A101");
        unit.setFloor(1);
        unit.setType("Studio");
        unit.setRentAmount(new BigDecimal("7500"));

        lease = new Lease();
        lease.setId(100L);
        lease.setTenant(tenant);
        lease.setUnit(unit);
        lease.setRentAmount(new BigDecimal("7500"));
        lease.setStartDate(LocalDate.now().minusMonths(1));
        lease.setEndDate(LocalDate.now().plusMonths(11));

        payment = new Payment();
        payment.setId(200L);
        payment.setAmount(new BigDecimal("7500"));
        payment.setLease(lease);
    }

    @Test
    void testGetVillagerDashboard_Success() {
        when(tenantService.findByEmail("john@example.com")).thenReturn(Optional.of(tenant));
        when(leaseService.getLeasesByTenant(1L)).thenReturn(List.of(lease));
        when(paymentService.getPaymentsByLease(100L)).thenReturn(List.of(payment));

        ResponseEntity<Map<String, Object>> response = villagerController.getVillagerDashboard("john@example.com");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().containsKey("tenant"));
        assertTrue(response.getBody().containsKey("leases"));
        assertTrue(response.getBody().containsKey("payments"));
    }

    @Test
    void testGetActiveLeaseByEmail_Found() {
        when(tenantService.findByEmail("john@example.com")).thenReturn(Optional.of(tenant));
        when(leaseService.getLeasesByTenant(1L)).thenReturn(List.of(lease));

        ResponseEntity<?> response = villagerController.getActiveLeaseByEmail("john@example.com");

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testGetActiveLeaseByEmail_NotFound() {
        when(tenantService.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = villagerController.getActiveLeaseByEmail("unknown@example.com");

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetPaymentsByEmail_Success() {
        when(tenantService.findByEmail("john@example.com")).thenReturn(Optional.of(tenant));
        when(leaseService.getLeasesByTenant(1L)).thenReturn(List.of(lease));
        when(paymentService.getPaymentsByLease(100L)).thenReturn(List.of(payment));

        ResponseEntity<?> response = villagerController.getPaymentsByEmail("john@example.com");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(((List<?>) response.getBody()).size() > 0);
    }

    @Test
    void testCreateMaintenanceRequest_Success() {
        when(tenantService.findByEmail("john@example.com")).thenReturn(Optional.of(tenant));
        when(leaseService.getLeasesByTenant(1L)).thenReturn(List.of(lease));

        MaintenanceRequest mockRequest = new MaintenanceRequest();
        mockRequest.setId(500L);
        mockRequest.setTitle("Broken AC");

        when(maintenanceRequestService.createMaintenanceRequest(any(MaintenanceRequest.class)))
                .thenReturn(mockRequest);

        Map<String, Object> body = new HashMap<>();
        body.put("email", "john@example.com");
        body.put("title", "Broken AC");
        body.put("description", "Air conditioner not working");
        body.put("priority", "HIGH");
        body.put("category", "ELECTRICAL");
        body.put("urgency", "EMERGENCY");
        body.put("preferredTime", "Evening");

        ResponseEntity<MaintenanceRequest> response = villagerController.createMaintenanceRequest(body);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Broken AC", response.getBody().getTitle());
    }

    @Test
    void testCreateMaintenanceRequest_TenantNotFound() {
        when(tenantService.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        Map<String, Object> body = Map.of("email", "notfound@example.com");
        ResponseEntity<MaintenanceRequest> response = villagerController.createMaintenanceRequest(body);

        assertEquals(404, response.getStatusCodeValue());
    }
}