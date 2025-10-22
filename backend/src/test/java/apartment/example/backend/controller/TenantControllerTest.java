package apartment.example.backend.controller;

import apartment.example.backend.dto.TenantResponseDto;
import apartment.example.backend.entity.Tenant;
import apartment.example.backend.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantControllerTest {

    @Mock
    private TenantService tenantService;

    @InjectMocks
    private TenantController tenantController;

    private Tenant tenant;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tenant = new Tenant();
        tenant.setId(1L);
        tenant.setFirstName("John");
        tenant.setLastName("Doe");
        tenant.setPhone("0123456789");
        tenant.setEmail("john@example.com");
    }


    @Test
    void testGetAllTenants() {
        when(tenantService.getAllTenants()).thenReturn(List.of(tenant));

        ResponseEntity<List<TenantResponseDto>> response = tenantController.getAllTenants();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("John", response.getBody().get(0).getFirstName());
    }


    @Test
    void testGetAllTenantsPaged() {
        Page<Tenant> page = new PageImpl<>(List.of(tenant));
        when(tenantService.getAllTenants(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<TenantResponseDto>> response = tenantController.getAllTenants(mock(Pageable.class));

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getTotalElements());
    }


    @Test
    void testGetTenantById_Found() {
        when(tenantService.getTenantById(1L)).thenReturn(Optional.of(tenant));

        ResponseEntity<TenantResponseDto> response = tenantController.getTenantById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("John", response.getBody().getFirstName());
    }


    @Test
    void testGetTenantById_NotFound() {
        when(tenantService.getTenantById(99L)).thenReturn(Optional.empty());

        ResponseEntity<TenantResponseDto> response = tenantController.getTenantById(99L);

        assertEquals(404, response.getStatusCodeValue());
    }


    @Test
    void testCreateTenant_Success() {
        when(tenantService.createTenant(any(Tenant.class))).thenReturn(tenant);

        ResponseEntity<TenantResponseDto> response = tenantController.createTenant(tenant);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("John", response.getBody().getFirstName());
    }


    @Test
    void testCreateTenant_BadRequest() {
        when(tenantService.createTenant(any(Tenant.class)))
                .thenThrow(new IllegalArgumentException("Invalid tenant data"));

        ResponseEntity<TenantResponseDto> response = tenantController.createTenant(tenant);

        assertEquals(400, response.getStatusCodeValue());
    }


    @Test
    void testDeleteTenant_Success() {
        doNothing().when(tenantService).deleteTenant(1L);

        ResponseEntity<Void> response = tenantController.deleteTenant(1L);

        assertEquals(204, response.getStatusCodeValue());
    }


    @Test
    void testDeleteTenant_NotFound() {
        doThrow(new RuntimeException("Tenant not found")).when(tenantService).deleteTenant(1L);

        ResponseEntity<Void> response = tenantController.deleteTenant(1L);

        assertEquals(404, response.getStatusCodeValue());
    }


    @Test
    void testCheckTenantExists() {
        when(tenantService.existsById(1L)).thenReturn(true);

        ResponseEntity<Boolean> response = tenantController.checkTenantExists(1L);

        assertTrue(response.getBody());
    }


    @Test
    void testCheckEmailExists() {
        when(tenantService.getTenantByEmail("john@example.com")).thenReturn(Optional.of(tenant));

        ResponseEntity<Boolean> response = tenantController.checkEmailExists("john@example.com");

        assertTrue(response.getBody());
    }
}