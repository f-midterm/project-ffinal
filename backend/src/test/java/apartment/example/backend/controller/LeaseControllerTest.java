package apartment.example.backend.controller;

import apartment.example.backend.entity.Lease;
import apartment.example.backend.entity.enums.LeaseStatus;
import apartment.example.backend.service.LeaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeaseControllerTest {

    @Mock
    private LeaseService leaseService;

    @InjectMocks
    private LeaseController leaseController;

    private Lease lease;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lease = new Lease();
        lease.setId(1L);
        lease.setRentAmount(BigDecimal.valueOf(8000));
        lease.setStartDate(LocalDate.now());
        lease.setEndDate(LocalDate.now().plusMonths(6));
        lease.setStatus(LeaseStatus.ACTIVE);
    }

    @Test
    void testGetAllLeases() {
        when(leaseService.getAllLeases()).thenReturn(List.of(lease));

        ResponseEntity<List<Lease>> response = leaseController.getAllLeases();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        verify(leaseService, times(1)).getAllLeases();
    }

    @Test
    void testGetLeaseById_Found() {
        when(leaseService.getLeaseById(1L)).thenReturn(Optional.of(lease));

        ResponseEntity<Lease> response = leaseController.getLeaseById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(lease, response.getBody());
    }

    @Test
    void testGetLeaseById_NotFound() {
        when(leaseService.getLeaseById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Lease> response = leaseController.getLeaseById(1L);

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testCreateLease_Success() {
        when(leaseService.createLease(any(Lease.class))).thenReturn(lease);

        ResponseEntity<Lease> response = leaseController.createLease(lease);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(lease, response.getBody());
        verify(leaseService, times(1)).createLease(lease);
    }

    @Test
    void testCreateLease_BadRequest() {
        when(leaseService.createLease(any(Lease.class))).thenThrow(new IllegalArgumentException("Invalid data"));

        ResponseEntity<Lease> response = leaseController.createLease(lease);

        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testUpdateLease_Success() {
        when(leaseService.updateLease(eq(1L), any(Lease.class))).thenReturn(lease);

        ResponseEntity<Lease> response = leaseController.updateLease(1L, lease);

        assertEquals(200, response.getStatusCodeValue());
        verify(leaseService).updateLease(1L, lease);
    }

    @Test
    void testUpdateLease_NotFound() {
        when(leaseService.updateLease(eq(1L), any(Lease.class)))
                .thenThrow(new RuntimeException("Not found"));

        ResponseEntity<Lease> response = leaseController.updateLease(1L, lease);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testTerminateLease_Success() {
        when(leaseService.terminateLease(eq(1L), anyString())).thenReturn(lease);

        ResponseEntity<Lease> response = leaseController.terminateLease(1L, Map.of("reason", "End of contract"));

        assertEquals(200, response.getStatusCodeValue());
        verify(leaseService).terminateLease(1L, "End of contract");
    }

    @Test
    void testDeleteLease_Success() {
        ResponseEntity<Void> response = leaseController.deleteLease(1L);

        assertEquals(204, response.getStatusCodeValue());
        verify(leaseService).deleteLease(1L);
    }

    @Test
    void testDeleteLease_NotFound() {
        doThrow(new RuntimeException("Not found")).when(leaseService).deleteLease(1L);

        ResponseEntity<Void> response = leaseController.deleteLease(1L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testCheckLeaseExists() {
        when(leaseService.existsById(1L)).thenReturn(true);

        ResponseEntity<Boolean> response = leaseController.checkLeaseExists(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody());
    }
}