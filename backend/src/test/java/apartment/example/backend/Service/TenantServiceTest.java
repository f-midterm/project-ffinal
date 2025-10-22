package apartment.example.backend.Service;

import apartment.example.backend.entity.Tenant;
import apartment.example.backend.entity.enums.TenantStatus;
import apartment.example.backend.repository.TenantRepository;
import apartment.example.backend.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
        testTenant.setPhone("081-234-5678");
        testTenant.setEmail("john.doe@example.com");
        testTenant.setEmergencyContact("Jane Doe");
        testTenant.setEmergencyPhone("082-345-6789");
        testTenant.setOccupation("Engineer");
        testTenant.setStatus(TenantStatus.ACTIVE);

        testTenant2 = new Tenant();
        testTenant2.setId(2L);
        testTenant2.setFirstName("Jane");
        testTenant2.setLastName("Smith");
        testTenant2.setPhone("083-456-7890");
        testTenant2.setEmail("jane.smith@example.com");
        testTenant2.setEmergencyContact("John Smith");
        testTenant2.setEmergencyPhone("084-567-8901");
        testTenant2.setOccupation("Doctor");
        testTenant2.setStatus(TenantStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should return all tenants")
    void getAllTenants_ShouldReturnAllTenants() {
        // Arrange
        List<Tenant> expectedTenants = Arrays.asList(testTenant, testTenant2);
        when(tenantRepository.findAll()).thenReturn(expectedTenants);

        // Act
        List<Tenant> actualTenants = tenantService.getAllTenants();

        // Assert
        assertNotNull(actualTenants);
        assertEquals(2, actualTenants.size());
        assertEquals(expectedTenants, actualTenants);
        verify(tenantRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return paginated tenants")
    void getAllTenants_WithPageable_ShouldReturnPagedTenants() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Tenant> tenantList = Arrays.asList(testTenant, testTenant2);
        Page<Tenant> expectedPage = new PageImpl<>(tenantList, pageable, tenantList.size());
        when(tenantRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<Tenant> actualPage = tenantService.getAllTenants(pageable);

        // Assert
        assertNotNull(actualPage);
        assertEquals(2, actualPage.getContent().size());
        assertEquals(expectedPage, actualPage);
        verify(tenantRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should return tenant by id when exists")
    void getTenantById_WhenExists_ShouldReturnTenant() {
        // Arrange
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));

        // Act
        Optional<Tenant> result = tenantService.getTenantById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testTenant.getId(), result.get().getId());
        assertEquals(testTenant.getEmail(), result.get().getEmail());
        verify(tenantRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when tenant id not found")
    void getTenantById_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(tenantRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Tenant> result = tenantService.getTenantById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(tenantRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should return tenant by email when exists")
    void getTenantByEmail_WhenExists_ShouldReturnTenant() {
        // Arrange
        String email = "john.doe@example.com";
        when(tenantRepository.findByEmail(email)).thenReturn(Optional.of(testTenant));

        // Act
        Optional<Tenant> result = tenantService.getTenantByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testTenant.getEmail(), result.get().getEmail());
        verify(tenantRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should return empty when email not found")
    void getTenantByEmail_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        String email = "notfound@example.com";
        when(tenantRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<Tenant> result = tenantService.getTenantByEmail(email);

        // Assert
        assertFalse(result.isPresent());
        verify(tenantRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should find tenant by email using findByEmail method")
    void findByEmail_WhenExists_ShouldReturnTenant() {
        // Arrange
        String email = "john.doe@example.com";
        when(tenantRepository.findByEmail(email)).thenReturn(Optional.of(testTenant));

        // Act
        Optional<Tenant> result = tenantService.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testTenant.getEmail(), result.get().getEmail());
        verify(tenantRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should search tenants by name")
    void searchTenantsByName_ShouldReturnMatchingTenants() {
        // Arrange
        String searchName = "John";
        List<Tenant> expectedTenants = Arrays.asList(testTenant);
        when(tenantRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(expectedTenants);

        // Act
        List<Tenant> result = tenantService.searchTenantsByName(searchName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getFirstName().contains(searchName));
        verify(tenantRepository, times(1)).findByNameContainingIgnoreCase(searchName);
    }

    @Test
    @DisplayName("Should return empty list when no name matches")
    void searchTenantsByName_WhenNoMatch_ShouldReturnEmptyList() {
        // Arrange
        String searchName = "NonExistent";
        when(tenantRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(Arrays.asList());

        // Act
        List<Tenant> result = tenantService.searchTenantsByName(searchName);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tenantRepository, times(1)).findByNameContainingIgnoreCase(searchName);
    }

    @Test
    @DisplayName("Should get tenants by phone")
    void getTenantsByPhone_ShouldReturnMatchingTenants() {
        // Arrange
        String phone = "081";
        List<Tenant> expectedTenants = Arrays.asList(testTenant);
        when(tenantRepository.findByPhoneContaining(phone)).thenReturn(expectedTenants);

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
        String email = "john";
        List<Tenant> expectedTenants = Arrays.asList(testTenant);
        when(tenantRepository.findByEmailContainingIgnoreCase(email)).thenReturn(expectedTenants);

        // Act
        List<Tenant> result = tenantService.getTenantsByEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getEmail().toLowerCase().contains(email.toLowerCase()));
        verify(tenantRepository, times(1)).findByEmailContainingIgnoreCase(email);
    }

    @Test
    @DisplayName("Should create new tenant successfully")
    void createTenant_ShouldSaveAndReturnTenant() {
        // Arrange
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        // Act
        Tenant result = tenantService.createTenant(testTenant);

        // Assert
        assertNotNull(result);
        assertEquals(testTenant.getId(), result.getId());
        assertEquals(testTenant.getEmail(), result.getEmail());
        verify(tenantRepository, times(1)).save(testTenant);
    }

    @Test
    @DisplayName("Should update tenant successfully")
    void updateTenant_WhenExists_ShouldUpdateAndReturnTenant() {
        // Arrange
        Tenant updatedDetails = new Tenant();
        updatedDetails.setFirstName("John Updated");
        updatedDetails.setLastName("Doe Updated");
        updatedDetails.setPhone("099-999-9999");
        updatedDetails.setEmail("john.updated@example.com");
        updatedDetails.setEmergencyContact("Jane Updated");
        updatedDetails.setEmergencyPhone("098-888-8888");
        updatedDetails.setOccupation("Senior Engineer");
        updatedDetails.setStatus(TenantStatus.INACTIVE);

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        // Act
        Tenant result = tenantService.updateTenant(1L, updatedDetails);

        // Assert
        assertNotNull(result);
        assertEquals("John Updated", result.getFirstName());
        assertEquals("Doe Updated", result.getLastName());
        assertEquals("099-999-9999", result.getPhone());
        assertEquals("john.updated@example.com", result.getEmail());
        assertEquals("Jane Updated", result.getEmergencyContact());
        assertEquals("098-888-8888", result.getEmergencyPhone());
        assertEquals("Senior Engineer", result.getOccupation());
        assertEquals(TenantStatus.INACTIVE, result.getStatus());
        verify(tenantRepository, times(1)).findById(1L);
        verify(tenantRepository, times(1)).save(testTenant);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent tenant")
    void updateTenant_WhenNotExists_ShouldThrowException() {
        // Arrange
        Tenant updatedDetails = new Tenant();
        when(tenantRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tenantService.updateTenant(999L, updatedDetails);
        });

        assertEquals("Tenant not found with id: 999", exception.getMessage());
        verify(tenantRepository, times(1)).findById(999L);
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    @DisplayName("Should delete tenant successfully")
    void deleteTenant_WhenExists_ShouldDeleteTenant() {
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
    void deleteTenant_WhenNotExists_ShouldThrowException() {
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
    void existsById_WhenExists_ShouldReturnTrue() {
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
    void existsById_WhenNotExists_ShouldReturnFalse() {
        // Arrange
        when(tenantRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = tenantService.existsById(999L);

        // Assert
        assertFalse(result);
        verify(tenantRepository, times(1)).existsById(999L);
    }
}