package apartment.example.backend.Service;

import apartment.example.backend.entity.Lease;
import apartment.example.backend.entity.Tenant;
import apartment.example.backend.entity.Unit;
import apartment.example.backend.entity.enums.LeaseStatus;
import apartment.example.backend.entity.enums.UnitStatus;
import apartment.example.backend.repository.LeaseRepository;
import apartment.example.backend.repository.UnitRepository;
import apartment.example.backend.service.LeaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class LeaseServiceTest {

    @Mock
    private LeaseRepository leaseRepository;

    @Mock
    private UnitRepository unitRepository;

    @InjectMocks
    private LeaseService leaseService;

    private Lease testLease;
    private Unit testUnit;
    private Tenant testTenant;

    @BeforeEach
    void setUp() {
        testUnit = new Unit();
        testUnit.setId(1L);
        testUnit.setRoomNumber("101");
        testUnit.setStatus(UnitStatus.AVAILABLE);

        testTenant = new Tenant();
        testTenant.setId(1L);
        testTenant.setFirstName("John");
        testTenant.setLastName("Doe");

        testLease = new Lease();
        testLease.setId(1L);
        testLease.setUnit(testUnit);
        testLease.setTenant(testTenant);
        testLease.setLeaseStartDate(LocalDate.now());
        testLease.setLeaseEndDate(LocalDate.now().plusMonths(12));
        testLease.setRentAmount(new BigDecimal("1000.00"));
        testLease.setSecurityDeposit(new BigDecimal("2000.00"));
        testLease.setStatus(LeaseStatus.ACTIVE);
    }

    @Test
    void getAllLeases_ShouldReturnAllLeases() {
        // Arrange
        List<Lease> expectedLeases = Arrays.asList(testLease);
        when(leaseRepository.findAll()).thenReturn(expectedLeases);

        // Act
        List<Lease> result = leaseService.getAllLeases();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLease, result.get(0));
        verify(leaseRepository, times(1)).findAll();
    }

    @Test
    void getAllLeasesWithPageable_ShouldReturnPagedLeases() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Lease> expectedPage = new PageImpl<>(Arrays.asList(testLease));
        when(leaseRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<Lease> result = leaseService.getAllLeases(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testLease, result.getContent().get(0));
        verify(leaseRepository, times(1)).findAll(pageable);
    }

    @Test
    void getLeaseById_WhenExists_ShouldReturnLease() {
        // Arrange
        when(leaseRepository.findById(1L)).thenReturn(Optional.of(testLease));

        // Act
        Optional<Lease> result = leaseService.getLeaseById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testLease, result.get());
        verify(leaseRepository, times(1)).findById(1L);
    }

    @Test
    void getLeaseById_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(leaseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Lease> result = leaseService.getLeaseById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(leaseRepository, times(1)).findById(999L);
    }

    @Test
    void getLeasesByStatus_ShouldReturnFilteredLeases() {
        // Arrange
        List<Lease> expectedLeases = Arrays.asList(testLease);
        when(leaseRepository.findByStatus(LeaseStatus.ACTIVE)).thenReturn(expectedLeases);

        // Act
        List<Lease> result = leaseService.getLeasesByStatus(LeaseStatus.ACTIVE);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(LeaseStatus.ACTIVE, result.get(0).getStatus());
        verify(leaseRepository, times(1)).findByStatus(LeaseStatus.ACTIVE);
    }

    @Test
    void getLeasesByTenant_ShouldReturnTenantLeases() {
        // Arrange
        List<Lease> expectedLeases = Arrays.asList(testLease);
        when(leaseRepository.findByTenantId(1L)).thenReturn(expectedLeases);

        // Act
        List<Lease> result = leaseService.getLeasesByTenant(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(leaseRepository, times(1)).findByTenantId(1L);
    }

    @Test
    void getLeasesByUnit_ShouldReturnUnitLeases() {
        // Arrange
        List<Lease> expectedLeases = Arrays.asList(testLease);
        when(leaseRepository.findByUnitId(1L)).thenReturn(expectedLeases);

        // Act
        List<Lease> result = leaseService.getLeasesByUnit(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(leaseRepository, times(1)).findByUnitId(1L);
    }

    @Test
    void getActiveLeases_ShouldReturnActiveLeases() {
        // Arrange
        List<Lease> expectedLeases = Arrays.asList(testLease);
        when(leaseRepository.findByStatus(LeaseStatus.ACTIVE)).thenReturn(expectedLeases);

        // Act
        List<Lease> result = leaseService.getActiveLeases();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(LeaseStatus.ACTIVE, result.get(0).getStatus());
        verify(leaseRepository, times(1)).findByStatus(LeaseStatus.ACTIVE);
    }

    @Test
    void getActiveLeaseByUnit_WhenExists_ShouldReturnLease() {
        // Arrange
        when(leaseRepository.findByUnitIdAndStatus(1L, LeaseStatus.ACTIVE))
                .thenReturn(Optional.of(testLease));

        // Act
        Optional<Lease> result = leaseService.getActiveLeaseByUnit(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testLease, result.get());
        verify(leaseRepository, times(1)).findByUnitIdAndStatus(1L, LeaseStatus.ACTIVE);
    }

    @Test
    void getLeasesEndingSoon_ShouldReturnLeasesInRange() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30);
        List<Lease> expectedLeases = Arrays.asList(testLease);
        when(leaseRepository.findLeasesEndingBetween(startDate, endDate, LeaseStatus.ACTIVE))
                .thenReturn(expectedLeases);

        // Act
        List<Lease> result = leaseService.getLeasesEndingSoon(30);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(leaseRepository, times(1))
                .findLeasesEndingBetween(startDate, endDate, LeaseStatus.ACTIVE);
    }

    @Test
    void getExpiredLeases_ShouldReturnExpiredLeases() {
        // Arrange
        LocalDate today = LocalDate.now();
        List<Lease> expectedLeases = Arrays.asList(testLease);
        when(leaseRepository.findExpiredLeases(today, LeaseStatus.ACTIVE))
                .thenReturn(expectedLeases);

        // Act
        List<Lease> result = leaseService.getExpiredLeases();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(leaseRepository, times(1)).findExpiredLeases(today, LeaseStatus.ACTIVE);
    }

    @Test
    void createLease_WhenValidAndUnitAvailable_ShouldCreateLease() {
        // Arrange
        when(unitRepository.findById(1L)).thenReturn(Optional.of(testUnit));
        when(leaseRepository.existsOverlappingLease(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(false);
        when(leaseRepository.save(any(Lease.class))).thenReturn(testLease);
        when(unitRepository.save(any(Unit.class))).thenReturn(testUnit);

        // Act
        Lease result = leaseService.createLease(testLease);

        // Assert
        assertNotNull(result);
        assertEquals(LeaseStatus.ACTIVE, result.getStatus());
        verify(unitRepository, times(1)).findById(1L);
        verify(unitRepository, times(1)).save(testUnit);
        verify(leaseRepository, times(1)).save(testLease);
        assertEquals(UnitStatus.OCCUPIED, testUnit.getStatus());
    }

    @Test
    void createLease_WhenUnitNotFound_ShouldThrowException() {
        // Arrange
        when(unitRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> leaseService.createLease(testLease));
        verify(unitRepository, times(1)).findById(1L);
        verify(leaseRepository, never()).save(any(Lease.class));
    }

    @Test
    void createLease_WhenUnitNotAvailable_ShouldThrowException() {
        // Arrange
        testUnit.setStatus(UnitStatus.OCCUPIED);
        when(unitRepository.findById(1L)).thenReturn(Optional.of(testUnit));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaseService.createLease(testLease)
        );
        assertTrue(exception.getMessage().contains("Unit is not available for lease"));
        verify(unitRepository, times(1)).findById(1L);
        verify(leaseRepository, never()).save(any(Lease.class));
    }

    @Test
    void createLease_WhenOverlappingLeaseExists_ShouldThrowException() {
        // Arrange
        when(unitRepository.findById(1L)).thenReturn(Optional.of(testUnit));
        when(leaseRepository.existsOverlappingLease(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaseService.createLease(testLease)
        );
        assertTrue(exception.getMessage().contains("Overlapping lease exists"));
        verify(leaseRepository, never()).save(any(Lease.class));
    }

    @Test
    void activateLease_WhenPending_ShouldActivateLease() {
        // Arrange
        testLease.setStatus(LeaseStatus.PENDING);
        when(leaseRepository.findById(1L)).thenReturn(Optional.of(testLease));
        when(leaseRepository.existsOverlappingLease(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(false);
        when(leaseRepository.save(any(Lease.class))).thenReturn(testLease);
        when(unitRepository.save(any(Unit.class))).thenReturn(testUnit);

        // Act
        Lease result = leaseService.activateLease(1L);

        // Assert
        assertNotNull(result);
        assertEquals(LeaseStatus.ACTIVE, result.getStatus());
        assertEquals(UnitStatus.OCCUPIED, testUnit.getStatus());
        verify(leaseRepository, times(1)).save(testLease);
        verify(unitRepository, times(1)).save(testUnit);
    }

    @Test
    void activateLease_WhenNotPending_ShouldThrowException() {
        // Arrange
        testLease.setStatus(LeaseStatus.ACTIVE);
        when(leaseRepository.findById(1L)).thenReturn(Optional.of(testLease));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaseService.activateLease(1L)
        );
        assertTrue(exception.getMessage().contains("Only pending leases can be activated"));
        verify(leaseRepository, never()).save(any(Lease.class));
    }

    @Test
    void terminateLease_WithReason_ShouldTerminateLease() {
        // Arrange
        when(leaseRepository.findById(1L)).thenReturn(Optional.of(testLease));
        when(leaseRepository.save(any(Lease.class))).thenReturn(testLease);
        when(unitRepository.save(any(Unit.class))).thenReturn(testUnit);

        // Act
        Lease result = leaseService.terminateLease(1L, "Tenant moved out");

        // Assert
        assertNotNull(result);
        assertEquals(LeaseStatus.TERMINATED, result.getStatus());
        assertEquals(UnitStatus.AVAILABLE, testUnit.getStatus());
        verify(leaseRepository, times(1)).save(testLease);
        verify(unitRepository, times(1)).save(testUnit);
    }

    @Test
    void terminateLease_WithoutReason_ShouldUseDefaultReason() {
        // Arrange
        when(leaseRepository.findById(1L)).thenReturn(Optional.of(testLease));
        when(leaseRepository.save(any(Lease.class))).thenReturn(testLease);
        when(unitRepository.save(any(Unit.class))).thenReturn(testUnit);

        // Act
        Lease result = leaseService.terminateLease(1L);

        // Assert
        assertNotNull(result);
        assertEquals(LeaseStatus.TERMINATED, result.getStatus());
        verify(leaseRepository, times(1)).save(testLease);
    }

    @Test
    void expireLeases_ShouldExpireAllExpiredLeases() {
        // Arrange
        Lease expiredLease1 = new Lease();
        expiredLease1.setId(1L);
        expiredLease1.setUnit(testUnit);
        expiredLease1.setStatus(LeaseStatus.ACTIVE);

        Unit unit2 = new Unit();
        unit2.setId(2L);
        unit2.setRoomNumber("102");
        unit2.setStatus(UnitStatus.OCCUPIED);

        Lease expiredLease2 = new Lease();
        expiredLease2.setId(2L);
        expiredLease2.setUnit(unit2);
        expiredLease2.setStatus(LeaseStatus.ACTIVE);

        List<Lease> expiredLeases = Arrays.asList(expiredLease1, expiredLease2);

        when(leaseRepository.findExpiredLeases(any(LocalDate.class), eq(LeaseStatus.ACTIVE)))
                .thenReturn(expiredLeases);
        when(unitRepository.save(any(Unit.class))).thenReturn(testUnit);
        when(leaseRepository.saveAll(anyList())).thenReturn(expiredLeases);

        // Act
        leaseService.expireLeases();

        // Assert
        assertEquals(LeaseStatus.EXPIRED, expiredLease1.getStatus());
        assertEquals(LeaseStatus.EXPIRED, expiredLease2.getStatus());
        assertEquals(UnitStatus.AVAILABLE, testUnit.getStatus());
        assertEquals(UnitStatus.AVAILABLE, unit2.getStatus());
        verify(unitRepository, times(2)).save(any(Unit.class));
        verify(leaseRepository, times(1)).saveAll(expiredLeases);
    }

    @Test
    void updateLease_WhenValid_ShouldUpdateLease() {
        // Arrange
        Lease updatedDetails = new Lease();
        updatedDetails.setLeaseStartDate(LocalDate.now());
        updatedDetails.setLeaseEndDate(LocalDate.now().plusMonths(12));
        updatedDetails.setRentAmount(new BigDecimal("1200.00"));
        updatedDetails.setSecurityDeposit(new BigDecimal("2400.00"));

        when(leaseRepository.findById(1L)).thenReturn(Optional.of(testLease));
        when(leaseRepository.save(any(Lease.class))).thenReturn(testLease);

        // Act
        Lease result = leaseService.updateLease(1L, updatedDetails);

        // Assert
        assertNotNull(result);
        verify(leaseRepository, times(1)).findById(1L);
        verify(leaseRepository, times(1)).save(testLease);
    }

    @Test
    void updateLease_WhenActiveLeaseDatesChanged_ShouldCheckOverlap() {
        // Arrange
        testLease.setStatus(LeaseStatus.ACTIVE);
        Lease updatedDetails = new Lease();
        updatedDetails.setLeaseStartDate(LocalDate.now().plusDays(1));
        updatedDetails.setLeaseEndDate(LocalDate.now().plusMonths(12));
        updatedDetails.setRentAmount(new BigDecimal("1200.00"));
        updatedDetails.setSecurityDeposit(new BigDecimal("2400.00"));

        when(leaseRepository.findById(1L)).thenReturn(Optional.of(testLease));
        when(leaseRepository.existsOverlappingLease(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaseService.updateLease(1L, updatedDetails)
        );
        assertTrue(exception.getMessage().contains("overlapping lease"));
        verify(leaseRepository, never()).save(any(Lease.class));
    }

    @Test
    void deleteLease_WhenActive_ShouldUpdateUnitStatus() {
        // Arrange
        when(leaseRepository.findById(1L)).thenReturn(Optional.of(testLease));
        doNothing().when(leaseRepository).delete(testLease);
        when(unitRepository.save(any(Unit.class))).thenReturn(testUnit);

        // Act
        leaseService.deleteLease(1L);

        // Assert
        assertEquals(UnitStatus.AVAILABLE, testUnit.getStatus());
        verify(leaseRepository, times(1)).delete(testLease);
        verify(unitRepository, times(1)).save(testUnit);
    }

    @Test
    void deleteLease_WhenNotActive_ShouldNotUpdateUnitStatus() {
        // Arrange
        testLease.setStatus(LeaseStatus.TERMINATED);
        when(leaseRepository.findById(1L)).thenReturn(Optional.of(testLease));
        doNothing().when(leaseRepository).delete(testLease);

        // Act
        leaseService.deleteLease(1L);

        // Assert
        verify(leaseRepository, times(1)).delete(testLease);
        verify(unitRepository, never()).save(any(Unit.class));
    }

    @Test
    void deleteLease_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(leaseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> leaseService.deleteLease(999L));
        verify(leaseRepository, never()).delete(any(Lease.class));
    }

    @Test
    void existsById_WhenExists_ShouldReturnTrue() {
        // Arrange
        when(leaseRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = leaseService.existsById(1L);

        // Assert
        assertTrue(result);
        verify(leaseRepository, times(1)).existsById(1L);
    }

    @Test
    void existsById_WhenNotExists_ShouldReturnFalse() {
        // Arrange
        when(leaseRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = leaseService.existsById(999L);

        // Assert
        assertFalse(result);
        verify(leaseRepository, times(1)).existsById(999L);
    }
}