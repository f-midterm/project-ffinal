package apartment.example.backend.controller;

import apartment.example.backend.entity.Lease;
import apartment.example.backend.entity.enums.LeaseStatus;
import apartment.example.backend.service.LeaseService;
import apartment.example.backend.service.PdfService;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LeaseControllerTest {

    @Mock
    private LeaseService leaseService;

    @Mock
    private PdfService pdfService;

    @InjectMocks
    private LeaseController leaseController;

    private Lease mockLease;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockLease = new Lease();
        mockLease.setId(1L);
    }

    @Test
    void testGetAllLeases() {
        when(leaseService.getAllLeases()).thenReturn(List.of(mockLease));

        ResponseEntity<List<Lease>> response = leaseController.getAllLeases();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetAllLeasesPaged() {
        Page<Lease> page = new PageImpl<>(List.of(mockLease));
        when(leaseService.getAllLeases(any(PageRequest.class))).thenReturn(page);

        ResponseEntity<Page<Lease>> response = leaseController.getAllLeases(PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getContent().size());
    }

    @Test
    void testGetLeaseById_found() {
        when(leaseService.getLeaseById(1L)).thenReturn(Optional.of(mockLease));

        ResponseEntity<Lease> response = leaseController.getLeaseById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockLease, response.getBody());
    }

    @Test
    void testGetLeaseById_notFound() {
        when(leaseService.getLeaseById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Lease> response = leaseController.getLeaseById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetLeasesByStatus() {
        when(leaseService.getLeasesByStatus(LeaseStatus.ACTIVE)).thenReturn(List.of(mockLease));

        ResponseEntity<List<Lease>> response = leaseController.getLeasesByStatus(LeaseStatus.ACTIVE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetLeasesByTenant() {
        when(leaseService.getLeasesByTenant(2L)).thenReturn(List.of(mockLease));

        ResponseEntity<List<Lease>> response = leaseController.getLeasesByTenant(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetLeasesByUnit() {
        when(leaseService.getLeasesByUnit(3L)).thenReturn(List.of(mockLease));

        ResponseEntity<List<Lease>> response = leaseController.getLeasesByUnit(3L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetActiveLeaseByUnit_found() {
        when(leaseService.getActiveLeaseByUnit(3L)).thenReturn(Optional.of(mockLease));

        ResponseEntity<Lease> response = leaseController.getActiveLeaseByUnit(3L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetActiveLeaseByUnit_notFound() {
        when(leaseService.getActiveLeaseByUnit(3L)).thenReturn(Optional.empty());

        ResponseEntity<Lease> response = leaseController.getActiveLeaseByUnit(3L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCreateLease_success() {
        when(leaseService.createLease(any())).thenReturn(mockLease);

        ResponseEntity<Lease> response = leaseController.createLease(mockLease);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testCreateLease_invalid() {
        when(leaseService.createLease(any())).thenThrow(new IllegalArgumentException("Invalid"));

        ResponseEntity<Lease> response = leaseController.createLease(mockLease);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateLease_success() {
        when(leaseService.updateLease(eq(1L), any())).thenReturn(mockLease);

        ResponseEntity<Lease> response = leaseController.updateLease(1L, mockLease);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateLease_notFound() {
        when(leaseService.updateLease(eq(1L), any())).thenThrow(new RuntimeException("Not found"));

        ResponseEntity<Lease> response = leaseController.updateLease(1L, mockLease);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testActivateLease_success() {
        when(leaseService.activateLease(1L)).thenReturn(mockLease);

        ResponseEntity<Lease> response = leaseController.activateLease(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testActivateLease_notFound() {
        when(leaseService.activateLease(1L)).thenThrow(new RuntimeException("Not found"));

        ResponseEntity<Lease> response = leaseController.activateLease(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testTerminateLease_success() {
        when(leaseService.terminateLease(eq(1L), anyString())).thenReturn(mockLease);

        Map<String, String> req = Map.of("reason", "Testing");
        ResponseEntity<Lease> response = leaseController.terminateLease(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testTerminateLease_notFound() {
        when(leaseService.terminateLease(eq(1L), anyString())).thenThrow(new RuntimeException("Not found"));

        Map<String, String> req = Map.of("reason", "Testing");
        ResponseEntity<Lease> response = leaseController.terminateLease(1L, req);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testExpireLeases_success() {
        ResponseEntity<String> response = leaseController.expireLeases();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteLease_success() {
        ResponseEntity<Void> response = leaseController.deleteLease(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testCheckLeaseExists() {
        when(leaseService.existsById(1L)).thenReturn(true);

        ResponseEntity<Boolean> response = leaseController.checkLeaseExists(1L);

        assertEquals(true, response.getBody());
    }
}
