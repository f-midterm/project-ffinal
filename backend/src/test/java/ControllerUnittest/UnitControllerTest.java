package apartment.example.backend.controller;

import apartment.example.backend.entity.Lease;
import apartment.example.backend.entity.Tenant;
import apartment.example.backend.entity.Unit;
import apartment.example.backend.entity.enums.LeaseStatus;
import apartment.example.backend.entity.enums.UnitStatus;
import apartment.example.backend.service.LeaseService;
import apartment.example.backend.service.TenantService;
import apartment.example.backend.service.UnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnitControllerTest {

    private UnitService unitService;
    private TenantService tenantService;
    private LeaseService leaseService;
    private UnitController controller;

    private Unit sampleUnit;
    private Lease sampleLease;
    private Tenant sampleTenant;

    @BeforeEach
    void setUp() {
        unitService = mock(UnitService.class);
        tenantService = mock(TenantService.class);
        leaseService = mock(LeaseService.class);

        controller = new UnitController(unitService, tenantService, leaseService);

        sampleUnit = new Unit();
        sampleUnit.setId(1L);
        sampleUnit.setRoomNumber("101A");
        sampleUnit.setFloor(1);
        sampleUnit.setStatus(UnitStatus.OCCUPIED);
        sampleUnit.setRentAmount(new BigDecimal("12000"));

        sampleTenant = new Tenant();
        sampleTenant.setId(1L);
        sampleTenant.setFirstName("John");
        sampleTenant.setLastName("Doe");

        sampleLease = new Lease();
        sampleLease.setId(1L);
        sampleLease.setUnit(sampleUnit);
        sampleLease.setTenant(sampleTenant);
        sampleLease.setStatus(LeaseStatus.ACTIVE);
    }

    @Test
    void testGetAllUnits() {
        when(unitService.getAllUnits()).thenReturn(List.of(sampleUnit));

        ResponseEntity<List<Unit>> response = controller.getAllUnits();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(sampleUnit.getId(), response.getBody().get(0).getId());

        verify(unitService).getAllUnits();
    }

    @Test
    void testGetUnitById() {
        when(unitService.getUnitById(1L)).thenReturn(Optional.of(sampleUnit));

        ResponseEntity<Unit> response = controller.getUnitById(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(sampleUnit.getId(), response.getBody().getId());
    }

    @Test
    void testGetUnitByRoomNumber() {
        when(unitService.getUnitByRoomNumber("101A")).thenReturn(Optional.of(sampleUnit));

        ResponseEntity<Unit> response = controller.getUnitByRoomNumber("101A");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(sampleUnit.getRoomNumber(), response.getBody().getRoomNumber());
    }

    @Test
    void testGetUnitsByFloor() {
        when(unitService.getUnitsByFloor(1)).thenReturn(List.of(sampleUnit));

        ResponseEntity<List<Unit>> response = controller.getUnitsByFloor(1);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetUnitsByStatus() {
        when(unitService.getUnitsByStatus(UnitStatus.OCCUPIED)).thenReturn(List.of(sampleUnit));

        ResponseEntity<List<Unit>> response = controller.getUnitsByStatus(UnitStatus.OCCUPIED);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(UnitStatus.OCCUPIED, response.getBody().get(0).getStatus());
    }

    @Test
    void testGetAvailableUnits() {
        when(unitService.getAvailableUnits()).thenReturn(List.of(sampleUnit));

        ResponseEntity<List<Unit>> response = controller.getAvailableUnits();
        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetUnitsByRentRange() {
        when(unitService.getUnitsByRentRange(new BigDecimal("10000"), new BigDecimal("15000")))
                .thenReturn(List.of(sampleUnit));

        ResponseEntity<List<Unit>> response = controller.getUnitsByRentRange(new BigDecimal("10000"), new BigDecimal("15000"));
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetDashboard() {
        when(unitService.countUnitsByStatus(UnitStatus.AVAILABLE)).thenReturn(0L);
        when(unitService.countUnitsByStatus(UnitStatus.OCCUPIED)).thenReturn(1L);
        when(unitService.countUnitsByStatus(UnitStatus.MAINTENANCE)).thenReturn(0L);
        when(unitService.countUnitsByStatus(UnitStatus.RESERVED)).thenReturn(0L);
        when(unitService.countUnitsByFloor(1)).thenReturn(1L);
        when(unitService.countUnitsByFloor(2)).thenReturn(0L);

        List<Unit> units = Collections.singletonList(new Unit());
        when(unitService.getAllUnits()).thenReturn(units);


        ResponseEntity<Map<String, Object>> response = controller.getDashboard();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(Long.valueOf(1), ((Integer) response.getBody().get("totalUnits")).longValue());
        assertEquals(Long.valueOf(1), response.getBody().get("occupiedUnits"));

    }

    @Test
    void testCreateUnit() {
        when(unitService.createUnit(sampleUnit)).thenReturn(sampleUnit);

        ResponseEntity<Unit> response = controller.createUnit(sampleUnit);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(sampleUnit, response.getBody());
    }

    @Test
    void testUpdateUnit() {
        when(unitService.updateUnit(1L, sampleUnit)).thenReturn(sampleUnit);

        ResponseEntity<Unit> response = controller.updateUnit(1L, sampleUnit);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(sampleUnit, response.getBody());
    }

    @Test
    void testUpdateUnitStatus() {
        Map<String, String> request = Map.of("status", "OCCUPIED");
        when(unitService.getUnitById(1L)).thenReturn(Optional.of(sampleUnit));

        ResponseEntity<Unit> response = controller.updateUnitStatus(1L, request);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(UnitStatus.OCCUPIED, response.getBody().getStatus());
    }

    @Test
    void testDeleteUnit() {
        doNothing().when(unitService).deleteUnit(1L);

        ResponseEntity<Void> response = controller.deleteUnit(1L);
        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void testCheckUnitExists() {
        when(unitService.existsById(1L)).thenReturn(true);

        ResponseEntity<Boolean> response = controller.checkUnitExists(1L);
        assertTrue(response.getBody());
    }

    @Test
    void testCheckRoomNumberExists() {
        when(unitService.existsByRoomNumber("101A")).thenReturn(true);

        ResponseEntity<Boolean> response = controller.checkRoomNumberExists("101A");
        assertTrue(response.getBody());
    }

    @Test
    void testGetUnitsWithDetails() {
        when(unitService.getAllUnits()).thenReturn(List.of(sampleUnit));
        when(leaseService.getAllLeases()).thenReturn(List.of(sampleLease));

        ResponseEntity<List<Map<String, Object>>> response = controller.getUnitsWithDetails();
        assertEquals(200, response.getStatusCodeValue());
        List<Map<String, Object>> detailed = response.getBody();
        assertNotNull(detailed);
        assertEquals(1, detailed.size());
        Map<String, Object> details = detailed.get(0);
        assertEquals(sampleUnit, details.get("unit"));
        assertEquals(sampleLease, details.get("lease"));
        assertEquals(sampleTenant, details.get("tenant"));
    }

    @Test
    void testGetUnitDetails() {
        when(unitService.getUnitById(1L)).thenReturn(Optional.of(sampleUnit));
        when(leaseService.getActiveLeaseByUnit(1L)).thenReturn(Optional.of(sampleLease));

        ResponseEntity<Map<String, Object>> response = controller.getUnitDetails(1L);
        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> details = response.getBody();
        assertEquals(sampleUnit, details.get("unit"));
        assertEquals(sampleLease, details.get("lease"));
        assertEquals(sampleTenant, details.get("tenant"));
    }

    @Test
    void testMaintenanceTest() {
        ResponseEntity<String> response = controller.maintenanceTest();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Maintenance functionality test - working!", response.getBody());
    }
}
