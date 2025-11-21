package apartment.example.backend.controller;

import apartment.example.backend.entity.*;
import apartment.example.backend.entity.enums.LeaseStatus;
import apartment.example.backend.entity.enums.PaymentStatus;
import apartment.example.backend.service.*;
import apartment.example.backend.dto.LeaseResponseDto;
import apartment.example.backend.dto.PaymentResponseDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VillagerControllerTest {

    @Mock private TenantService tenantService;
    @Mock private LeaseService leaseService;
    @Mock private PaymentService paymentService;
    @Mock private MaintenanceRequestService maintenanceRequestService;

    @InjectMocks
    private VillagerController controller;

    private Tenant tenant;
    private Lease lease;
    private Unit unit;
    private Payment payment;
    private MaintenanceRequest request;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);
        tenant.setEmail("a@a.com");
        tenant.setFirstName("John");
        tenant.setLastName("Doe");
        tenant.setPhone("123456789");

        unit = new Unit();
        unit.setId(10L);
        unit.setRoomNumber("A101");
        unit.setFloor(1);
        unit.setUnitType("DELUXE");
        unit.setRentAmount(BigDecimal.valueOf(5000));

        lease = new Lease();
        lease.setId(2L);
        lease.setTenant(tenant);
        lease.setUnit(unit);
        lease.setRentAmount(BigDecimal.valueOf(5000));
        lease.setStatus(LeaseStatus.ACTIVE);

        payment = new Payment();
        payment.setId(3L);
        payment.setLease(lease);
        payment.setAmount(BigDecimal.valueOf(3500));
        payment.setStatus(PaymentStatus.PAID);

        request = new MaintenanceRequest();
        request.setId(100L);
        request.setTenantId(tenant.getId());
        request.setTitle("Test Request");
        request.setStatus(MaintenanceRequest.RequestStatus.SUBMITTED);
    }

    // -------------------------------------------------------------
    // DASHBOARD TEST
    // -------------------------------------------------------------

    @Test
    void testGetVillagerDashboard_Found() {
        when(tenantService.findByEmail("a@a.com"))
                .thenReturn(Optional.of(tenant));
        when(leaseService.getLeasesByTenant(1L))
                .thenReturn(List.of(lease));
        when(paymentService.getPaymentsByLease(2L))
                .thenReturn(List.of(payment));

        ResponseEntity<Map<String, Object>> response =
                controller.getVillagerDashboard("a@a.com");

        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals(tenant, body.get("tenant"));
        assertEquals(List.of(lease), body.get("leases"));
        assertEquals(lease, body.get("activeLease"));
        assertEquals(List.of(payment), body.get("payments"));
    }

    @Test
    void testGetVillagerDashboard_TenantNotFound() {
        when(tenantService.findByEmail("x@x.com"))
                .thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response =
                controller.getVillagerDashboard("x@x.com");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
    }

    // -------------------------------------------------------------
    // GET LEASE BY EMAIL
    // -------------------------------------------------------------

    @Test
    void testGetActiveLeaseByEmail_Found() {
        when(tenantService.findByEmail("a@a.com"))
                .thenReturn(Optional.of(tenant));
        when(leaseService.getLeasesByTenant(1L))
                .thenReturn(List.of(lease));

        ResponseEntity<LeaseResponseDto> response =
                controller.getActiveLeaseByEmail("a@a.com");

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().getId());
    }

    @Test
    void testGetActiveLeaseByEmail_NotFound() {
        when(tenantService.findByEmail("x@x.com"))
                .thenReturn(Optional.empty());

        ResponseEntity<LeaseResponseDto> response =
                controller.getActiveLeaseByEmail("x@x.com");

        assertEquals(404, response.getStatusCodeValue());
    }

    // -------------------------------------------------------------
    // GET PAYMENTS BY EMAIL
    // -------------------------------------------------------------

    @Test
    void testGetPaymentsByEmail_Found() {
        when(tenantService.findByEmail("a@a.com"))
                .thenReturn(Optional.of(tenant));
        when(leaseService.getLeasesByTenant(1L))
                .thenReturn(List.of(lease));
        when(paymentService.getPaymentsByLease(2L))
                .thenReturn(List.of(payment));

        ResponseEntity<List<PaymentResponseDto>> response =
                controller.getPaymentsByEmail("a@a.com");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetPaymentsByEmail_NotFound() {
        when(tenantService.findByEmail("x@x.com"))
                .thenReturn(Optional.empty());

        ResponseEntity<List<PaymentResponseDto>> response =
                controller.getPaymentsByEmail("x@x.com");

        assertEquals(404, response.getStatusCodeValue());
    }

    // -------------------------------------------------------------
    // GET MAINTENANCE REQUESTS
    // -------------------------------------------------------------

    @Test
    void testGetMaintenanceRequests_Found() {
        when(tenantService.findByEmail("a@a.com"))
                .thenReturn(Optional.of(tenant));
        when(maintenanceRequestService.getRequestsByTenantId(1L))
                .thenReturn(List.of(request));

        ResponseEntity<List<MaintenanceRequest>> response =
                controller.getVillagerMaintenanceRequests("a@a.com");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetMaintenanceRequests_NotFound() {
        when(tenantService.findByEmail("x@x.com"))
                .thenReturn(Optional.empty());

        ResponseEntity<List<MaintenanceRequest>> response =
                controller.getVillagerMaintenanceRequests("x@x.com");

        assertEquals(404, response.getStatusCodeValue());
    }

    // -------------------------------------------------------------
    // CREATE MAINTENANCE REQUEST
    // -------------------------------------------------------------

    @Test
    void testCreateMaintenanceRequest_Success() {
        Map<String, Object> req = new HashMap<>();
        req.put("email", "a@a.com");
        req.put("title", "Fix AC");
        req.put("description", "Not working");
        req.put("priority", "HIGH");
        req.put("category", "ELECTRICAL");
        req.put("urgency", "EMERGENCY");
        req.put("preferredTime", "Evening");

        when(tenantService.findByEmail("a@a.com"))
                .thenReturn(Optional.of(tenant));
        when(leaseService.getLeasesByTenant(1L))
                .thenReturn(List.of(lease));
        when(maintenanceRequestService.createMaintenanceRequest(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<MaintenanceRequest> response =
                controller.createMaintenanceRequest(req);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Fix AC", response.getBody().getTitle());
    }

    @Test
    void testCreateMaintenanceRequest_TenantNotFound() {
        Map<String, Object> req = new HashMap<>();
        req.put("email", "x@x.com");

        when(tenantService.findByEmail("x@x.com"))
                .thenReturn(Optional.empty());

        ResponseEntity<MaintenanceRequest> response =
                controller.createMaintenanceRequest(req);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testCreateMaintenanceRequest_NoLease() {
        Map<String, Object> req = new HashMap<>();
        req.put("email", "a@a.com");

        when(tenantService.findByEmail("a@a.com"))
                .thenReturn(Optional.of(tenant));
        when(leaseService.getLeasesByTenant(1L))
                .thenReturn(List.of()); // No lease

        ResponseEntity<MaintenanceRequest> response =
                controller.createMaintenanceRequest(req);

        assertEquals(400, response.getStatusCodeValue());
    }

    // -------------------------------------------------------------
    // MAINTENANCE STATS
    // -------------------------------------------------------------

    @Test
    void testGetMaintenanceStats_Found() {
        when(tenantService.findByEmail("a@a.com"))
                .thenReturn(Optional.of(tenant));
        when(maintenanceRequestService.getRequestsByTenantId(1L))
                .thenReturn(List.of(request));

        ResponseEntity<Map<String, Object>> response =
                controller.getVillagerMaintenanceStats("a@a.com");

        assertEquals(200, response.getStatusCodeValue());

        Map<String, Object> stats = response.getBody();
        assertEquals(1, stats.get("total"));
        assertEquals(1L, stats.get("pending"));
        assertEquals(0L, stats.get("completed"));
    }

    @Test
    void testGetMaintenanceStats_NotFound() {
        when(tenantService.findByEmail("x@x.com"))
                .thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response =
                controller.getVillagerMaintenanceStats("x@x.com");

        assertEquals(404, response.getStatusCodeValue());
    }
}
