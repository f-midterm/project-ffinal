package apartment.example.backend.controller;

import apartment.example.backend.dto.TenantResponseDto;
import apartment.example.backend.entity.Lease;
import apartment.example.backend.entity.Tenant;
import apartment.example.backend.entity.Unit;
import apartment.example.backend.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantControllerTest {

    @Mock
    private TenantService tenantService;

    @InjectMocks
    private TenantController controller;

    private Tenant sampleTenant;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);


        Lease lease = new Lease();
        Unit unit = new Unit();
        unit.setId(101L); // unitId
        lease.setUnit(unit);
        lease.setStartDate(LocalDate.of(2023, 1, 1));
        lease.setEndDate(LocalDate.of(2024, 1, 1));
        lease.setRentAmount(new BigDecimal("15000"));
        lease.setStatus(apartment.example.backend.entity.enums.LeaseStatus.ACTIVE);

        sampleTenant = new Tenant();
        sampleTenant.setId(1L);
        sampleTenant.setFirstName("John");
        sampleTenant.setLastName("Doe");
        sampleTenant.setEmail("john@example.com");
        sampleTenant.setPhone("0123456789");
        sampleTenant.setStatus(apartment.example.backend.entity.enums.TenantStatus.ACTIVE);
        sampleTenant.setLeases(List.of(lease));


        sampleTenant.setEmergencyContact("Jane Doe");
        sampleTenant.setEmergencyPhone("0987654321");
        sampleTenant.setOccupation("Engineer");
    }

    @Test
    void testGetAllTenants() {
        when(tenantService.getAllTenants()).thenReturn(List.of(sampleTenant));

        ResponseEntity<List<TenantResponseDto>> response = controller.getAllTenants();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("John", response.getBody().get(0).getFirstName());
    }

    @Test
    void testGetTenantById_found() {
        when(tenantService.getTenantById(1L)).thenReturn(Optional.of(sampleTenant));

        ResponseEntity<TenantResponseDto> response = controller.getTenantById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John", response.getBody().getFirstName());
    }

    @Test
    void testGetTenantById_notFound() {
        when(tenantService.getTenantById(2L)).thenReturn(Optional.empty());

        ResponseEntity<TenantResponseDto> response = controller.getTenantById(2L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCreateTenant() {
        when(tenantService.createTenant(sampleTenant)).thenReturn(sampleTenant);

        ResponseEntity<TenantResponseDto> response = controller.createTenant(sampleTenant);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("John", response.getBody().getFirstName());
    }

    @Test
    void testUpdateTenant() {
        when(tenantService.updateTenant(1L, sampleTenant)).thenReturn(sampleTenant);

        ResponseEntity<TenantResponseDto> response = controller.updateTenant(1L, sampleTenant);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John", response.getBody().getFirstName());
    }

    @Test
    void testDeleteTenant_success() {
        doNothing().when(tenantService).deleteTenant(1L);

        ResponseEntity<?> response = controller.deleteTenant(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testCheckTenantExists_true() {
        when(tenantService.existsById(1L)).thenReturn(true);

        ResponseEntity<Boolean> response = controller.checkTenantExists(1L);

        assertTrue(response.getBody());
    }

    @Test
    void testCheckEmailExists_false() {
        when(tenantService.getTenantByEmail("unknown@example.com")).thenReturn(Optional.empty());

        ResponseEntity<Boolean> response = controller.checkEmailExists("unknown@example.com");

        assertFalse(response.getBody());
    }

    @Test
    void testGetAllTenantsPaged() {
        Page<Tenant> page = new PageImpl<>(List.of(sampleTenant));
        when(tenantService.getAllTenants(PageRequest.of(0, 10))).thenReturn(page);

        ResponseEntity<Page<TenantResponseDto>> response = controller.getAllTenants(PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }
}
