package apartment.example.backend.Service;

import apartment.example.backend.entity.Tenant;
import apartment.example.backend.entity.enums.TenantStatus;
import apartment.example.backend.repository.TenantRepository;
import apartment.example.backend.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantService Unit Tests")
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantService tenantService;

    private Tenant testTenant;
    private Tenant testTenant2;

    @BeforeEach
    void setUp() {
        testTenant = new Tenant();
        testTenant.setId(1L);
        testTenant.setFirstName("John");
        testTenant.setLastName("Doe");
        testTenant.setEmail("john.doe@example.com");
        testTenant.setPhone("0812345678");
        testTenant.setEmergencyContact("Jane Doe");
        testTenant.setEmergencyPhone("0887654321");
        testTenant.setOccupation("Engineer");
        testTenant.setStatus(TenantStatus.valueOf("ACTIVE"));

        testTenant2 = new Tenant();
        testTenant2.setId(2L);
        testTenant2.setFirstName("Jane");
        testTenant2.setLastName("Smith");
        testTenant2.setEmail("jane.smith@example.com");
        testTenant2.setPhone("0823456789");
    }

    @Test
    @DisplayName("Should get all tenants as list")
    void getAllTenants_ShouldReturnListOfTenants() {
        // Arrange
        List<Tenant> tenants = Arrays.asList(testTenant, testTenant2);
        when(tenantRepository.findAll()).thenReturn(tenants);

        // Act
        List<Tenant> result = tenantService.getAllTenants();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(tenantRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get all tenants with pagination")
    void getAllTenantsWithPageable_ShouldReturnPageOfTenants() {
        // Arrange
        List<Tenant> tenants = Arrays.asList(testTenant, testTenant2);
        Page<Tenant> page = new PageImpl<>(tenants);
        Pageable pageable = PageRequest.of(0, 10);
        when(tenantRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Tenant> result = tenantService.getAllTenants(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(tenantRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should get tenant by id when tenant exists")
    void getTenantById_WhenTenantExists_ShouldReturnTenant() {
        // Arrange
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));

        // Act
        Optional<Tenant> result = tenantService.getTenantById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
        verify(tenantRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty optional when tenant not found by id")
    void getTenantById_WhenTenantNotExists_ShouldReturnEmpty() {
        // Arrange
        when(tenantRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Tenant> result = tenantService.getTenantById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(tenantRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should get tenant by email when tenant exists")
    void getTenantByEmail_WhenTenantExists_ShouldReturnTenant() {
        // Arrange
        String email = "john.doe@example.com";
        when(tenantRepository.findByEmail(email)).thenReturn(Optional.of(testTenant));

        // Act
        Optional<Tenant> result = tenantService.getTenantByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        verify(tenantRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should find tenant by email using findByEmail method")
    void findByEmail_WhenTenantExists_ShouldReturnTenant() {
        // Arrange
        String email = "john.doe@example.com";
        when(tenantRepository.findByEmail(email)).thenReturn(Optional.of(testTenant));

        // Act
        Optional<Tenant> result = tenantService.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        verify(tenantRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should search tenants by name")
    void searchTenantsByName_ShouldReturnMatchingTenants() {
        // Arrange
        String name = "john";
        List<Tenant> tenants = Arrays.asList(testTenant);
        when(tenantRepository.findByNameContainingIgnoreCase(name)).thenReturn(tenants);

        // Act
        List<Tenant> result = tenantService.searchTenantsByName(name);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
        verify(tenantRepository, times(1)).findByNameContainingIgnoreCase(name);
    }

    @Test
    @DisplayName("Should get tenants by phone")
    void getTenantsByPhone_ShouldReturnMatchingTenants() {
        // Arrange
        String phone = "081";
        List<Tenant> tenants = Arrays.asList(testTenant);
        when(tenantRepository.findByPhoneContaining(phone)).thenReturn(tenants);

        // Act
        List<Tenant> result = tenantService.getTenantsByPhone(phone);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getPhone().contains(phone));
        verify(tenantRepository, times(1)).findByPhoneContaining(phone);
    }

    @Test
    @DisplayName("Should get tenants by email")
    void getTenantsByEmail_ShouldReturnMatchingTenants() {
        // Arrange
        String email = "example.com";
        List<Tenant> tenants = Arrays.asList(testTenant, testTenant2);
        when(tenantRepository.findByEmailContainingIgnoreCase(email)).thenReturn(tenants);

        // Act
        List<Tenant> result = tenantService.getTenantsByEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(tenantRepository, times(1)).findByEmailContainingIgnoreCase(email);
    }

    // Test removed: getTenantsByUnitId - unitId moved to Lease entity

    @Test
    @DisplayName("Should create new tenant successfully")
    void createTenant_ShouldReturnSavedTenant() {
        // Arrange
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        // Act
        Tenant result = tenantService.createTenant(testTenant);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(tenantRepository, times(1)).save(testTenant);
    }

    @Test
    @DisplayName("Should update tenant successfully when tenant exists")
    void updateTenant_WhenTenantExists_ShouldReturnUpdatedTenant() {
        // Arrange
        Tenant updatedDetails = new Tenant();
        updatedDetails.setFirstName("Johnny");
        updatedDetails.setLastName("Doe Updated");
        updatedDetails.setEmail("johnny.doe@example.com");
        updatedDetails.setPhone("0899999999");
        updatedDetails.setEmergencyContact("Jane Updated");
        updatedDetails.setEmergencyPhone("0888888888");
        updatedDetails.setOccupation("Senior Engineer");
        updatedDetails.setStatus(TenantStatus.INACTIVE);

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        // Act
        Tenant result = tenantService.updateTenant(1L, updatedDetails);

        // Assert
        assertNotNull(result);
        assertEquals("Johnny", testTenant.getFirstName());
        assertEquals("Doe Updated", testTenant.getLastName());
        assertEquals("johnny.doe@example.com", testTenant.getEmail());
        assertEquals("0899999999", testTenant.getPhone());
        assertEquals("Senior Engineer", testTenant.getOccupation());
        assertEquals(TenantStatus.INACTIVE, testTenant.getStatus());
        verify(tenantRepository, times(1)).findById(1L);
        verify(tenantRepository, times(1)).save(testTenant);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent tenant")
    void updateTenant_WhenTenantNotExists_ShouldThrowException() {
        // Arrange
        when(tenantRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tenantService.updateTenant(999L, testTenant);
        });

        assertEquals("Tenant not found with id: 999", exception.getMessage());
        verify(tenantRepository, times(1)).findById(999L);
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    @DisplayName("Should delete tenant successfully when tenant exists")
    void deleteTenant_WhenTenantExists_ShouldDeleteTenant() {
        // Arrange
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
        doNothing().when(tenantRepository).delete(testTenant);

        // Act
        tenantService.deleteTenant(1L);

        // Assert
        verify(tenantRepository, times(1)).findById(1L);
        verify(tenantRepository, times(1)).delete(testTenant);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent tenant")
    void deleteTenant_WhenTenantNotExists_ShouldThrowException() {
        // Arrange
        when(tenantRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tenantService.deleteTenant(999L);
        });

        assertEquals("Tenant not found with id: 999", exception.getMessage());
        verify(tenantRepository, times(1)).findById(999L);
        verify(tenantRepository, never()).delete(any(Tenant.class));
    }

    @Test
    @DisplayName("Should return true when tenant exists by id")
    void existsById_WhenTenantExists_ShouldReturnTrue() {
        // Arrange
        when(tenantRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = tenantService.existsById(1L);

        // Assert
        assertTrue(result);
        verify(tenantRepository, times(1)).existsById(1L);
    }

    @Test
    @DisplayName("Should return false when tenant does not exist by id")
    void existsById_WhenTenantNotExists_ShouldReturnFalse() {
        // Arrange
        when(tenantRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = tenantService.existsById(999L);

        // Assert
        assertFalse(result);
        verify(tenantRepository, times(1)).existsById(999L);
    }
}