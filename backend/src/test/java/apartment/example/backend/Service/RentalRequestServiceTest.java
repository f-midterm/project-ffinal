package apartment.example.backend.Service;

import apartment.example.backend.entity.*;
import apartment.example.backend.entity.enums.LeaseStatus;
import apartment.example.backend.entity.enums.UnitStatus;
import apartment.example.backend.repository.*;
import apartment.example.backend.service.RentalRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RentalRequestServiceTest {

    @Mock private RentalRequestRepository rentalRequestRepository;
    @Mock private UserRepository userRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private UnitRepository unitRepository;
    @Mock private LeaseRepository leaseRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RentalRequestService rentalRequestService;

    private RentalRequest request;
    private Unit unit;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        unit = new Unit();
        unit.setId(1L);
        unit.setStatus(UnitStatus.AVAILABLE);
        unit.setRentAmount(BigDecimal.valueOf(5000));

        request = new RentalRequest();
        request.setId(10L);
        request.setUnitId(1L);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
    }

   
    @Test
    void testCreateRentalRequest_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(rentalRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RentalRequest result = rentalRequestService.createRentalRequest(request);

        assertEquals(RentalRequestStatus.PENDING, result.getStatus());
        verify(rentalRequestRepository).save(any());
    }


    @Test
    void testCreateRentalRequest_UnitNotAvailable_ThrowsException() {
        unit.setStatus(UnitStatus.OCCUPIED);
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));

        assertThrows(IllegalArgumentException.class, () ->
                rentalRequestService.createRentalRequest(request));
    }


    @Test
    void testApproveRequest_SimpleSuccess() {
        request.setStatus(RentalRequestStatus.PENDING);
        when(rentalRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(rentalRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RentalRequest result = rentalRequestService.approveRequest(10L, 99L);

        assertEquals(RentalRequestStatus.APPROVED, result.getStatus());
        assertEquals(99L, result.getApprovedByUserId());
        verify(rentalRequestRepository).save(request);
    }


    @Test
    void testApproveRequest_WithLease_Success() {
        when(rentalRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(tenantRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPwd");
        when(rentalRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RentalRequest result = rentalRequestService.approveRequest(
                10L, 101L, LocalDate.now(), LocalDate.now().plusMonths(6));

        assertEquals(RentalRequestStatus.APPROVED, result.getStatus());
        verify(tenantRepository).save(any(Tenant.class));
        verify(unitRepository).save(any(Unit.class));
        verify(leaseRepository).save(any(Lease.class));
        verify(userRepository).save(any(User.class)); // new VILLAGER user created
    }


    @Test
    void testRejectRequest_Success() {
        when(rentalRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(rentalRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RentalRequest result = rentalRequestService.rejectRequest(10L, "No documents", 50L);

        assertEquals(RentalRequestStatus.REJECTED, result.getStatus());
        assertEquals("No documents", result.getRejectionReason());
        verify(rentalRequestRepository).save(request);
    }


    @Test
    void testDeleteRentalRequest_Success() {
        when(rentalRequestRepository.existsById(10L)).thenReturn(true);
        rentalRequestService.deleteRentalRequest(10L);
        verify(rentalRequestRepository).deleteById(10L);
    }


    @Test
    void testDeleteRentalRequest_NotFound_ThrowsException() {
        when(rentalRequestRepository.existsById(10L)).thenReturn(false);
        assertThrows(RuntimeException.class, () ->
                rentalRequestService.deleteRentalRequest(10L));
    }
}
