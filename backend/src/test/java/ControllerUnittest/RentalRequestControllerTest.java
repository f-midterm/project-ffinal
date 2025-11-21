package apartment.example.backend.controller;

import apartment.example.backend.dto.AcknowledgeResponseDto;
import apartment.example.backend.dto.ApprovalRequest;
import apartment.example.backend.dto.MyLatestRequestDto;
import apartment.example.backend.entity.RentalRequest;
import apartment.example.backend.entity.enums.RentalRequestStatus;
import apartment.example.backend.security.JwtUtil;
import apartment.example.backend.service.RentalRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RentalRequestControllerTest {

    private RentalRequestService rentalRequestService;
    private JwtUtil jwtUtil;
    private RentalRequestController controller;

    @BeforeEach
    void setUp() {
        rentalRequestService = mock(RentalRequestService.class);
        jwtUtil = mock(JwtUtil.class);
        controller = new RentalRequestController(rentalRequestService, jwtUtil);
    }

    @Test
    void testGetAllRentalRequests() {
        RentalRequest request = new RentalRequest();
        request.setId(1L);
        when(rentalRequestService.getAllRentalRequests()).thenReturn(List.of(request));

        ResponseEntity<List<RentalRequest>> response = controller.getAllRentalRequests();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetRequestById_found() {
        RentalRequest request = new RentalRequest();
        request.setId(1L);
        when(rentalRequestService.getRequestById(1L)).thenReturn(Optional.of(request));

        ResponseEntity<RentalRequest> response = controller.getRequestById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testGetRequestById_notFound() {
        when(rentalRequestService.getRequestById(1L)).thenReturn(Optional.empty());

        ResponseEntity<RentalRequest> response = controller.getRequestById(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCreateRentalRequest_success() {
        RentalRequest request = new RentalRequest();
        request.setUnitId(101L);
        request.setEmail("user@example.com");

        RentalRequest saved = new RentalRequest();
        saved.setId(1L);
        when(rentalRequestService.createRentalRequest(request)).thenReturn(saved);

        ResponseEntity<RentalRequest> response = controller.createRentalRequest(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testApproveRequest_success() {
        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setApprovedByUserId(10L);
        approvalRequest.setStartDate(LocalDate.now());
        approvalRequest.setEndDate(LocalDate.now().plusDays(30));

        RentalRequest approved = new RentalRequest();
        approved.setId(1L);

        when(rentalRequestService.approveRequest(
                eq(1L), eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(approved);

        ResponseEntity<?> response = controller.approveRequest(1L, approvalRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof RentalRequest);
    }

    @Test
    void testRejectRequest_success() {
        RentalRequest rejected = new RentalRequest();
        rejected.setId(1L);
        when(rentalRequestService.rejectRequest(eq(1L), eq("Not allowed"), eq(5L)))
                .thenReturn(rejected);

        ResponseEntity<RentalRequest> response = controller.rejectRequest(1L,
                Map.of("reason", "Not allowed", "rejectedByUserId", 5L));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testGetMyLatestRequest_success() {
        String token = "Bearer dummy.token";
        Long userId = 10L;
        MyLatestRequestDto dto = new MyLatestRequestDto();
        dto.setStatus(RentalRequestStatus.PENDING);

        when(jwtUtil.extractUsername(anyString())).thenReturn("user@example.com");
        when(jwtUtil.validateToken(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(userId);
        when(rentalRequestService.getMyLatestRequest(userId)).thenReturn(dto);

        ResponseEntity<?> response = controller.getMyLatestRequest(token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void testAcknowledgeRejection_success() {
        String token = "Bearer dummy.token";
        Long userId = 10L;
        AcknowledgeResponseDto ack = new AcknowledgeResponseDto();
        ack.setMessage("Acknowledged");

        when(jwtUtil.extractUsername(anyString())).thenReturn("user@example.com");
        when(jwtUtil.validateToken(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(userId);
        when(rentalRequestService.acknowledgeRejection(1L, userId)).thenReturn(ack);

        ResponseEntity<?> response = controller.acknowledgeRejection(1L, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ack, response.getBody());
    }

    @Test
    void testCreateAuthenticatedRentalRequest_success() {
        String token = "Bearer dummy.token";
        Long userId = 10L;
        RentalRequest request = new RentalRequest();
        request.setUnitId(101L);
        RentalRequest saved = new RentalRequest();
        saved.setId(1L);

        when(jwtUtil.extractUsername(anyString())).thenReturn("user@example.com");
        when(jwtUtil.validateToken(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(userId);
        when(rentalRequestService.createRentalRequestWithUser(request, userId)).thenReturn(saved);

        ResponseEntity<?> response = controller.createAuthenticatedRentalRequest(request, token);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(saved, response.getBody());
    }
}
