package apartment.example.backend.Service;

import apartment.example.backend.entity.Tenant;
import apartment.example.backend.repository.TenantRepository;
import apartment.example.backend.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantService tenantService;

    private Tenant tenant;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tenant = new Tenant();
        tenant.setId(1L);
        tenant.setFirstName("John");
        tenant.setLastName("Doe");
        tenant.setEmail("john@example.com");
        tenant.setPhone("0812345678");
    }

    @Test
    void getAllTenants_ShouldReturnList() {
        when(tenantRepository.findAll()).thenReturn(List.of(tenant));

        List<Tenant> result = tenantService.getAllTenants();

        assertEquals(1, result.size());
        verify(tenantRepository, times(1)).findAll();
    }

    @Test
    void getAllTenants_WithPageable_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 5);
        when(tenantRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(tenant)));

        Page<Tenant> result = tenantService.getAllTenants(pageable);

        assertEquals(1, result.getContent().size());
        verify(tenantRepository).findAll(pageable);
    }

    @Test
    void getTenantById_ShouldReturnTenant_WhenExists() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));

        Optional<Tenant> result = tenantService.getTenantById(1L);

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
        verify(tenantRepository).findById(1L);
    }

    @Test
    void getTenantById_ShouldReturnEmpty_WhenNotFound() {
        when(tenantRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Tenant> result = tenantService.getTenantById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getTenantByEmail_ShouldReturnTenant_WhenExists() {
        when(tenantRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(tenant));

        Optional<Tenant> result = tenantService.getTenantByEmail("john@example.com");

        assertTrue(result.isPresent());
        assertEquals("john@example.com", result.get().getEmail());
        verify(tenantRepository).findByEmail("john@example.com");
    }

    @Test
    void findByEmail_ShouldCallRepository() {
        when(tenantRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(tenant));

        tenantService.findByEmail("john@example.com");

        verify(tenantRepository).findByEmail("john@example.com");
    }

    @Test
    void searchTenantsByName_ShouldReturnList() {
        when(tenantRepository.findByNameContainingIgnoreCase("john"))
                .thenReturn(List.of(tenant));

        List<Tenant> result = tenantService.searchTenantsByName("john");

        assertEquals(1, result.size());
        verify(tenantRepository).findByNameContainingIgnoreCase("john");
    }

    @Test
    void getTenantsByPhone_ShouldReturnList() {
        when(tenantRepository.findByPhoneContaining("0812"))
                .thenReturn(List.of(tenant));

        List<Tenant> result = tenantService.getTenantsByPhone("0812");

        assertEquals(1, result.size());
        verify(tenantRepository).findByPhoneContaining("0812");
    }

    @Test
    void getTenantsByEmail_ShouldReturnList() {
        when(tenantRepository.findByEmailContainingIgnoreCase("john"))
                .thenReturn(List.of(tenant));

        List<Tenant> result = tenantService.getTenantsByEmail("john");

        assertEquals(1, result.size());
        verify(tenantRepository).findByEmailContainingIgnoreCase("john");
    }

    @Test
    void createTenant_ShouldSaveAndReturnTenant() {
        when(tenantRepository.save(tenant)).thenReturn(tenant);

        Tenant result = tenantService.createTenant(tenant);

        assertEquals("John", result.getFirstName());
        verify(tenantRepository).save(tenant);
    }

    @Test
    void updateTenant_ShouldUpdateExistingTenant() {
        Tenant updateData = new Tenant();
        updateData.setFirstName("Jane");
        updateData.setLastName("Smith");
        updateData.setEmail("jane@example.com");
        updateData.setPhone("0999999999");

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

        Tenant updated = tenantService.updateTenant(1L, updateData);

        assertEquals("Jane", updated.getFirstName());
        assertEquals("Smith", updated.getLastName());
        verify(tenantRepository).save(tenant);
    }

    @Test
    void updateTenant_ShouldThrowException_WhenTenantNotFound() {
        when(tenantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> tenantService.updateTenant(99L, tenant));
    }

    @Test
    void deleteTenant_ShouldDelete_WhenFound() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));

        tenantService.deleteTenant(1L);

        verify(tenantRepository).delete(tenant);
    }

    @Test
    void deleteTenant_ShouldThrow_WhenNotFound() {
        when(tenantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> tenantService.deleteTenant(99L));
    }

    @Test
    void existsById_ShouldReturnTrue_WhenExists() {
        when(tenantRepository.existsById(1L)).thenReturn(true);

        boolean result = tenantService.existsById(1L);

        assertTrue(result);
        verify(tenantRepository).existsById(1L);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenNotExists() {
        when(tenantRepository.existsById(99L)).thenReturn(false);

        boolean result = tenantService.existsById(99L);

        assertFalse(result);
    }
}