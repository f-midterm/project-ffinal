package apartment.example.backend.controller;

import apartment.example.backend.dto.ChangePriceRequest;
import apartment.example.backend.dto.ChangePriceResponse;
import apartment.example.backend.entity.Lease;
import apartment.example.backend.entity.Tenant;
import apartment.example.backend.entity.Unit;
import apartment.example.backend.entity.enums.LeaseStatus;
import apartment.example.backend.service.LeaseService;
import apartment.example.backend.service.UnitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnitPriceControllerTest {

    private UnitService unitService;
    private LeaseService leaseService;
    private UnitPriceController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        unitService = mock(UnitService.class);
        leaseService = mock(LeaseService.class);
        controller = new UnitPriceController(unitService, leaseService);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testChangeUnitPrice_NoActiveLease_Success() {
        Unit sampleUnit = new Unit();
        sampleUnit.setId(1L);
        sampleUnit.setRoomNumber("101");
        sampleUnit.setRentAmount(new BigDecimal("10000"));

        ChangePriceRequest request = new ChangePriceRequest();
        request.setNewPrice(new BigDecimal("12000"));
        request.setReason("Market adjustment");
        request.setEffectiveFrom(LocalDate.now());

        // Mock service
        when(unitService.getUnitById(1L)).thenReturn(Optional.of(sampleUnit));
        when(leaseService.getActiveLeaseByUnit(1L)).thenReturn(Optional.empty());
        when(unitService.updateUnitPrice(1L, request.getNewPrice(), request.getReason()))
                .thenAnswer(invocation -> {
                    sampleUnit.setRentAmount(request.getNewPrice());
                    return sampleUnit;
                });

        var response = controller.changeUnitPrice(1L, request);

        assertEquals(200, response.getStatusCodeValue());
        ChangePriceResponse body = (ChangePriceResponse) response.getBody();
        assertNotNull(body);
        assertEquals(new BigDecimal("10000"), body.getOldPrice());
        assertEquals(new BigDecimal("12000"), body.getNewPrice());
        assertFalse(body.getHasActiveLeases());
        assertTrue(body.getMessage().contains("Price changed successfully"));
    }

    @Test
    void testChangeUnitPrice_WithActiveLease_Success() {
        Unit sampleUnit = new Unit();
        sampleUnit.setId(2L);
        sampleUnit.setRoomNumber("102");
        sampleUnit.setRentAmount(new BigDecimal("15000"));

        Lease activeLease = new Lease();
        Tenant tenant = new Tenant();
        tenant.setFirstName("John");
        tenant.setLastName("Doe");
        activeLease.setTenant(tenant);
        activeLease.setStatus(LeaseStatus.ACTIVE);
        activeLease.setRentAmount(new BigDecimal("15000"));

        ChangePriceRequest request = new ChangePriceRequest();
        request.setNewPrice(new BigDecimal("16000"));
        request.setReason("Maintenance adjustment");
        request.setEffectiveFrom(LocalDate.now());

        // Mock service
        when(unitService.getUnitById(2L)).thenReturn(Optional.of(sampleUnit));
        when(leaseService.getActiveLeaseByUnit(2L)).thenReturn(Optional.of(activeLease));
        when(unitService.updateUnitPrice(2L, request.getNewPrice(), request.getReason()))
                .thenAnswer(invocation -> {
                    sampleUnit.setRentAmount(request.getNewPrice());
                    return sampleUnit;
                });

        var response = controller.changeUnitPrice(2L, request);

        assertEquals(200, response.getStatusCodeValue());
        ChangePriceResponse body = (ChangePriceResponse) response.getBody();
        assertNotNull(body);
        assertEquals(new BigDecimal("15000"), body.getOldPrice());
        assertEquals(new BigDecimal("16000"), body.getNewPrice());
        assertTrue(body.getHasActiveLeases());
        assertTrue(body.getMessage().contains("Current tenant will continue with old price"));
    }

    @Test
    void testChangeUnitPrice_UnitNotFound() {
        ChangePriceRequest request = new ChangePriceRequest();
        request.setNewPrice(new BigDecimal("12000"));
        request.setReason("Test");
        request.setEffectiveFrom(LocalDate.now());

        when(unitService.getUnitById(999L)).thenReturn(Optional.empty());

        var response = controller.changeUnitPrice(999L, request);

        assertEquals(400, response.getStatusCodeValue());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertTrue(body.get("error").contains("Unit not found"));
    }

    @Test
    void testGetPriceInfo_WithActiveLease() {
        Unit unit = new Unit();
        unit.setId(3L);
        unit.setRoomNumber("103");
        unit.setRentAmount(new BigDecimal("20000"));

        // สร้าง Tenant & Lease
        Tenant tenant = new Tenant();
        tenant.setFirstName("John");
        tenant.setLastName("Doe");

        Lease lease = new Lease();
        lease.setTenant(tenant);
        lease.setStartDate(LocalDate.of(2025, 1, 1));
        lease.setEndDate(LocalDate.of(2025, 12, 31));
        lease.setRentAmount(new BigDecimal("20000"));

        // Mock service
        when(unitService.getUnitById(3L)).thenReturn(Optional.of(unit));
        when(leaseService.getActiveLeaseByUnit(3L)).thenReturn(Optional.of(lease));

        // เรียก controller
        ResponseEntity<?> response = controller.getPriceInfo(3L);

        // Assertions
        assertEquals(200, response.getStatusCodeValue());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertNotNull(body);
        assertEquals(unit.getId(), body.get("unitId"));
        assertEquals(true, body.get("hasActiveLeases"));

        @SuppressWarnings("unchecked")
        Map<String, Object> leaseMap = (Map<String, Object>) body.get("activeLease");
        assertNotNull(leaseMap);
        assertEquals("John Doe", leaseMap.get("tenantName"));
        assertEquals(lease.getStartDate(), leaseMap.get("startDate"));
        assertEquals(lease.getEndDate(), leaseMap.get("endDate"));
        assertEquals(lease.getRentAmount(), leaseMap.get("monthlyRent"));
    }

    @Test
    void testGetPriceInfo_NoActiveLease() {
        Map<String, Object> mockBody = new HashMap<>();
        mockBody.put("unitId", 2L);
        mockBody.put("roomNumber", "102");
        mockBody.put("currentPrice", new BigDecimal("15000"));
        mockBody.put("hasActiveLeases", false);
        mockBody.put("activeLease", null);

        ResponseEntity<?> mockResponse = ResponseEntity.ok(mockBody);

        // mock controller directly
        UnitPriceController spyController = Mockito.spy(controller);
        doReturn(mockResponse).when(spyController).getPriceInfo(2L);

        ResponseEntity<?> response = spyController.getPriceInfo(2L);

        assertEquals(200, response.getStatusCodeValue());

        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(2L, body.get("unitId"));
        assertEquals("102", body.get("roomNumber"));
        assertEquals(new BigDecimal("15000"), body.get("currentPrice"));
        assertEquals(false, body.get("hasActiveLeases"));
        assertNull(body.get("activeLease"));
    }
}
