package apartment.example.backend.controller;

import apartment.example.backend.dto.AdminDashboardDto;
import apartment.example.backend.entity.*;
import apartment.example.backend.entity.enums.LeaseStatus;
import apartment.example.backend.entity.enums.RentalRequestStatus;
import apartment.example.backend.entity.enums.UnitStatus;
import apartment.example.backend.repository.LeaseRepository;
import apartment.example.backend.repository.MaintenanceRequestRepository;
import apartment.example.backend.repository.RentalRequestRepository;
import apartment.example.backend.repository.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private LeaseRepository leaseRepository;

    @Mock
    private RentalRequestRepository rentalRequestRepository;

    @Mock
    private MaintenanceRequestRepository maintenanceRequestRepository;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Mock security context as ADMIN
        var auth = new UsernamePasswordAuthenticationToken("adminUser", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testGetDashboardData() {

        Unit u1 = new Unit(); u1.setStatus(UnitStatus.OCCUPIED);
        Unit u2 = new Unit(); u2.setStatus(UnitStatus.AVAILABLE);
        Unit u3 = new Unit(); u3.setStatus(UnitStatus.MAINTENANCE);

        when(unitRepository.findAll()).thenReturn(List.of(u1, u2, u3));


        when(rentalRequestRepository.countByStatus(RentalRequestStatus.PENDING)).thenReturn(5L);


        when(maintenanceRequestRepository.countByStatusIn(any())).thenReturn(3L);


        Lease lease = new Lease();
        lease.setId(1L);

        Unit leaseUnit = new Unit();
        leaseUnit.setId(10L);
        leaseUnit.setRoomNumber("A101");
        lease.setUnit(leaseUnit);

        Tenant tenant = new Tenant();
        tenant.setFirstName("John");
        tenant.setLastName("Doe");
        tenant.setEmail("john@example.com");
        lease.setTenant(tenant);

        lease.setEndDate(LocalDate.now().plusDays(5));

        when(leaseRepository.findLeasesEndingBetween(
                any(), any(), eq(LeaseStatus.ACTIVE)
        )).thenReturn(List.of(lease));


        ResponseEntity<AdminDashboardDto> response = adminController.getDashboardData();
        AdminDashboardDto dto = response.getBody();


        assertNotNull(dto);
        assertEquals("adminUser", dto.getAdminName());

        assertEquals(3, dto.getTotalUnits());
        assertEquals(1, dto.getOccupiedUnits());
        assertEquals(1, dto.getAvailableUnits());
        assertEquals(1, dto.getMaintenanceUnits());

        assertEquals(5, dto.getPendingRentalRequests());
        assertEquals(3, dto.getMaintenanceRequests());

        assertEquals(1, dto.getLeasesExpiringSoon());
        assertEquals(1, dto.getExpiringLeases().size());

        AdminDashboardDto.ExpiringLeaseInfo info = dto.getExpiringLeases().get(0);
        assertEquals(1L, info.getLeaseId());
        assertEquals(10L, info.getUnitId());
        assertEquals("A101", info.getUnitRoomNumber());
        assertEquals("John Doe", info.getTenantName());
    }
}
