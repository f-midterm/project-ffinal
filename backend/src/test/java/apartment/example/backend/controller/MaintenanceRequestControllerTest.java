package apartment.example.backend.controller;


import apartment.example.backend.dto.MaintenanceRequestDTO;
import apartment.example.backend.entity.MaintenanceRequest;
import apartment.example.backend.entity.Unit;
import apartment.example.backend.entity.Tenant;
import apartment.example.backend.service.MaintenanceRequestService;
import apartment.example.backend.repository.UnitRepository;
import apartment.example.backend.repository.TenantRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MaintenanceRequestControllerTest {

    @Mock
    private MaintenanceRequestService maintenanceRequestService;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private MaintenanceRequestController controller;

    private MaintenanceRequest sampleRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleRequest = new MaintenanceRequest();
        sampleRequest.setId(1L);
        sampleRequest.setTitle("Leaking pipe");
        sampleRequest.setDescription("Water leaking under sink");
        sampleRequest.setUnitId(10L);
        sampleRequest.setTenantId(5L);
    }


    @Test
    void testTestEndpoint() {
        ResponseEntity<String> response = controller.testEndpoint();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("MaintenanceRequestController is working!", response.getBody());
    }


    @Test
    void testCreateMaintenanceRequest_Success() {
        when(maintenanceRequestService.createMaintenanceRequest(any())).thenReturn(sampleRequest);

        ResponseEntity<MaintenanceRequest> response = controller.createMaintenanceRequest(sampleRequest);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Leaking pipe", response.getBody().getTitle());
    }


    @Test
    void testCreateMaintenanceRequest_Failure() {
        when(maintenanceRequestService.createMaintenanceRequest(any())).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<MaintenanceRequest> response = controller.createMaintenanceRequest(sampleRequest);

        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }


    @Test
    void testGetMaintenanceRequestById_Found() {
        when(maintenanceRequestService.getMaintenanceRequestById(1L)).thenReturn(Optional.of(sampleRequest));

        ResponseEntity<MaintenanceRequest> response = controller.getMaintenanceRequestById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1L, response.getBody().getId());
    }


    @Test
    void testGetMaintenanceRequestById_NotFound() {
        when(maintenanceRequestService.getMaintenanceRequestById(1L)).thenReturn(Optional.empty());

        ResponseEntity<MaintenanceRequest> response = controller.getMaintenanceRequestById(1L);

        assertEquals(404, response.getStatusCodeValue());
    }


    @Test
    void testGetAllMaintenanceRequests() {
        Unit unit = new Unit();
        unit.setId(10L);
        unit.setRoomNumber("A101");
        unit.setType("Studio");

        Tenant tenant = new Tenant();
        tenant.setId(5L);
        tenant.setFirstName("John");
        tenant.setLastName("Doe");

        when(maintenanceRequestService.getAllMaintenanceRequests()).thenReturn(List.of(sampleRequest));
        when(unitRepository.findById(10L)).thenReturn(Optional.of(unit));
        when(tenantRepository.findById(5L)).thenReturn(Optional.of(tenant));

        ResponseEntity<List<MaintenanceRequestDTO>> response = controller.getAllMaintenanceRequests();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("A101", response.getBody().get(0).getRoomNumber());
        assertEquals("John Doe", response.getBody().get(0).getTenantName());
    }


    @Test
    void testUpdateMaintenanceRequest_Success() {
        when(maintenanceRequestService.updateMaintenanceRequest(eq(1L), any())).thenReturn(sampleRequest);

        ResponseEntity<MaintenanceRequest> response = controller.updateMaintenanceRequest(1L, sampleRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Leaking pipe", response.getBody().getTitle());
    }


    @Test
    void testUpdateMaintenanceRequest_NotFound() {
        when(maintenanceRequestService.updateMaintenanceRequest(eq(1L), any()))
                .thenThrow(new RuntimeException("Not found"));

        ResponseEntity<MaintenanceRequest> response = controller.updateMaintenanceRequest(1L, sampleRequest);

        assertEquals(404, response.getStatusCodeValue());
    }


    @Test
    void testDeleteMaintenanceRequest_Success() {
        doNothing().when(maintenanceRequestService).deleteMaintenanceRequest(1L);

        ResponseEntity<Void> response = controller.deleteMaintenanceRequest(1L);

        assertEquals(204, response.getStatusCodeValue());
    }


    @Test
    void testGetMaintenanceRequestStats() {
        Map<String, Long> mockStats = new HashMap<>();
        mockStats.put("SUBMITTED", 5L);
        when(maintenanceRequestService.getMaintenanceRequestStats()).thenReturn(mockStats);

        ResponseEntity<Map<String, Long>> response = controller.getMaintenanceRequestStats();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(5L, response.getBody().get("SUBMITTED"));
    }
}