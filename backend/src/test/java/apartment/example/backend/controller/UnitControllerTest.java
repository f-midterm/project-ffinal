package apartment.example.backend.controller;

import apartment.example.backend.entity.Unit;
import apartment.example.backend.entity.enums.UnitStatus;
import apartment.example.backend.service.LeaseService;
import apartment.example.backend.service.TenantService;
import apartment.example.backend.service.UnitService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnitControllerTest {

    @Mock
    private UnitService unitService;
    @Mock
    private TenantService tenantService;
    @Mock
    private LeaseService leaseService;

    @InjectMocks
    private UnitController unitController;

    private Unit mockUnit;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUnit = new Unit();
        mockUnit.setId(1L);
        mockUnit.setRoomNumber("A101");
        mockUnit.setFloor(1);
        mockUnit.setStatus(UnitStatus.AVAILABLE);
        mockUnit.setType("Studio");
        mockUnit.setRentAmount(new BigDecimal("7500"));
    }

    @Test
    void testGetAllUnits() {
        when(unitService.getAllUnits()).thenReturn(List.of(mockUnit));

        ResponseEntity<List<Unit>> response = unitController.getAllUnits();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("A101", response.getBody().get(0).getRoomNumber());
    }

    @Test
    void testGetUnitById_Found() {
        when(unitService.getUnitById(1L)).thenReturn(Optional.of(mockUnit));

        ResponseEntity<Unit> response = unitController.getUnitById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("A101", response.getBody().getRoomNumber());
    }

    @Test
    void testGetUnitById_NotFound() {
        when(unitService.getUnitById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Unit> response = unitController.getUnitById(99L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testCreateUnit_Success() {
        when(unitService.createUnit(any(Unit.class))).thenReturn(mockUnit);

        ResponseEntity<Unit> response = unitController.createUnit(mockUnit);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("A101", response.getBody().getRoomNumber());
    }

    @Test
    void testCreateUnit_DuplicateRoom_ThrowsError() {
        when(unitService.createUnit(any(Unit.class))).thenThrow(new IllegalArgumentException("Unit with room number already exists"));

        ResponseEntity<Unit> response = unitController.createUnit(mockUnit);

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testUpdateUnitStatus_Success() {
        Map<String, String> request = Map.of("status", "OCCUPIED");
        when(unitService.getUnitById(1L)).thenReturn(Optional.of(mockUnit));

        ResponseEntity<Unit> response = unitController.updateUnitStatus(1L, request);

        assertEquals(200, response.getStatusCodeValue());
        verify(unitService, times(1)).updateUnitStatus(1L, UnitStatus.OCCUPIED);
    }

    @Test
    void testUpdateUnitStatus_InvalidStatus() {
        Map<String, String> request = Map.of("status", "INVALID");

        ResponseEntity<Unit> response = unitController.updateUnitStatus(1L, request);

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testDeleteUnit_Success() {
        doNothing().when(unitService).deleteUnit(1L);

        ResponseEntity<Void> response = unitController.deleteUnit(1L);

        assertEquals(204, response.getStatusCodeValue());
        verify(unitService, times(1)).deleteUnit(1L);
    }

    @Test
    void testDeleteUnit_NotFound() {
        doThrow(new RuntimeException("Not found")).when(unitService).deleteUnit(99L);

        ResponseEntity<Void> response = unitController.deleteUnit(99L);

        assertEquals(404, response.getStatusCodeValue());
    }
}