package apartment.example.backend.controller;

import apartment.example.backend.dto.ApprovalRequest;
import apartment.example.backend.entity.RentalRequest;
import apartment.example.backend.entity.RentalRequestStatus;
import apartment.example.backend.service.RentalRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for RentalRequestController
 * Tests critical rental request management endpoints
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RentalRequestController Unit Tests")
class RentalRequestControllerTest {

    @Mock
    private RentalRequestService rentalRequestService;

    @InjectMocks
    private RentalRequestController rentalRequestController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private RentalRequest testRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rentalRequestController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Setup test data
        testRequest = createTestRentalRequest();
    }

    private RentalRequest createTestRentalRequest() {
        RentalRequest request = new RentalRequest();
        request.setId(1L);
        request.setUnitId(100L);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPhone("0812345678");
        request.setOccupation("Engineer");
        request.setEmergencyContact("Jane Doe");
        request.setEmergencyPhone("0898765432");
        request.setLeaseDurationMonths(12);
        request.setRequestDate(LocalDateTime.now());
        request.setStatus(RentalRequestStatus.PENDING);
        return request;
    }

    // ==================== GET PENDING REQUESTS TESTS ====================

    @Test
    @DisplayName("Should get all pending rental requests successfully")
    void testGetPendingRequests_Success() throws Exception {
        // Arrange
        List<RentalRequest> pendingRequests = Arrays.asList(testRequest);
        when(rentalRequestService.getPendingRequests()).thenReturn(pendingRequests);

        // Act & Assert
        mockMvc.perform(get("/rental-requests/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(rentalRequestService, times(1)).getPendingRequests();
    }

    @Test
    @DisplayName("Should return empty list when no pending requests")
    void testGetPendingRequests_EmptyList() throws Exception {
        // Arrange
        when(rentalRequestService.getPendingRequests()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/rental-requests/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(rentalRequestService, times(1)).getPendingRequests();
    }

    // ==================== GET BY ID TESTS ====================

    @Test
    @DisplayName("Should get rental request by ID successfully")
    void testGetRequestById_Success() throws Exception {
        // Arrange
        when(rentalRequestService.getRequestById(1L)).thenReturn(Optional.of(testRequest));

        // Act & Assert
        mockMvc.perform(get("/rental-requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(rentalRequestService, times(1)).getRequestById(1L);
    }

    @Test
    @DisplayName("Should return 404 when rental request not found")
    void testGetRequestById_NotFound() throws Exception {
        // Arrange
        when(rentalRequestService.getRequestById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/rental-requests/999"))
                .andExpect(status().isNotFound());

        verify(rentalRequestService, times(1)).getRequestById(999L);
    }

    // ==================== CREATE RENTAL REQUEST TESTS ====================

    @Test
    @DisplayName("Should create rental request successfully")
    void testCreateRentalRequest_Success() throws Exception {
        // Arrange
        when(rentalRequestService.createRentalRequest(any(RentalRequest.class)))
                .thenReturn(testRequest);

        // Act & Assert
        mockMvc.perform(post("/rental-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(rentalRequestService, times(1)).createRentalRequest(any(RentalRequest.class));
    }

    @Test
    @DisplayName("Should return 500 when create rental request fails")
    void testCreateRentalRequest_InternalError() throws Exception {
        // Arrange
        when(rentalRequestService.createRentalRequest(any(RentalRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/rental-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isInternalServerError());

        verify(rentalRequestService, times(1)).createRentalRequest(any(RentalRequest.class));
    }

    // ==================== APPROVE REQUEST TESTS ====================

    @Test
    @DisplayName("Should approve rental request successfully")
    void testApproveRequest_Success() throws Exception {
        // Arrange
        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setApprovedByUserId(10L);
        approvalRequest.setStartDate(LocalDate.now());
        approvalRequest.setEndDate(LocalDate.now().plusMonths(12));

        RentalRequest approvedRequest = createTestRentalRequest();
        approvedRequest.setStatus(RentalRequestStatus.APPROVED);
        approvedRequest.setApprovedByUserId(10L);
        approvedRequest.setApprovedDate(LocalDateTime.now());

        when(rentalRequestService.approveRequest(eq(1L), eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(approvedRequest);

        // Act & Assert
        mockMvc.perform(put("/rental-requests/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedByUserId").value(10));

        verify(rentalRequestService, times(1))
                .approveRequest(eq(1L), eq(10L), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("Should return 404 when approving non-existent request")
    void testApproveRequest_NotFound() throws Exception {
        // Arrange
        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setApprovedByUserId(10L);
        approvalRequest.setStartDate(LocalDate.now());
        approvalRequest.setEndDate(LocalDate.now().plusMonths(12));

        when(rentalRequestService.approveRequest(eq(999L), anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new RuntimeException("Rental request not found"));

        // Act & Assert
        mockMvc.perform(put("/rental-requests/999/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isNotFound());

        verify(rentalRequestService, times(1))
                .approveRequest(eq(999L), anyLong(), any(LocalDate.class), any(LocalDate.class));
    }

    // ==================== REJECT REQUEST TESTS ====================

    @Test
    @DisplayName("Should reject rental request successfully")
    void testRejectRequest_Success() throws Exception {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        payload.put("reason", "Unit no longer available");
        payload.put("rejectedByUserId", 10L);

        RentalRequest rejectedRequest = createTestRentalRequest();
        rejectedRequest.setStatus(RentalRequestStatus.REJECTED);
        rejectedRequest.setRejectionReason("Unit no longer available");

        when(rentalRequestService.rejectRequest(eq(1L), anyString(), eq(10L)))
                .thenReturn(rejectedRequest);

        // Act & Assert
        mockMvc.perform(put("/rental-requests/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("Unit no longer available"));

        verify(rentalRequestService, times(1))
                .rejectRequest(eq(1L), eq("Unit no longer available"), eq(10L));
    }

    @Test
    @DisplayName("Should reject request without rejectedByUserId")
    void testRejectRequest_WithoutUserId() throws Exception {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        payload.put("reason", "Incomplete documents");

        RentalRequest rejectedRequest = createTestRentalRequest();
        rejectedRequest.setStatus(RentalRequestStatus.REJECTED);
        rejectedRequest.setRejectionReason("Incomplete documents");

        when(rentalRequestService.rejectRequest(eq(1L), eq("Incomplete documents"), isNull()))
                .thenReturn(rejectedRequest);

        // Act & Assert
        mockMvc.perform(put("/rental-requests/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(rentalRequestService, times(1))
                .rejectRequest(eq(1L), eq("Incomplete documents"), isNull());
    }

    // ==================== SEARCH BY EMAIL TESTS ====================

    @Test
    @DisplayName("Should search rental requests by email successfully")
    void testGetRequestsByUserEmail_Success() throws Exception {
        // Arrange
        List<RentalRequest> requests = Arrays.asList(testRequest);
        when(rentalRequestService.getRequestsByEmail("john.doe@example.com"))
                .thenReturn(requests);

        // Act & Assert
        mockMvc.perform(get("/rental-requests/search")
                        .param("email", "john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));

        verify(rentalRequestService, times(1)).getRequestsByEmail("john.doe@example.com");
    }

    // ==================== DELETE REQUEST TESTS ====================

    @Test
    @DisplayName("Should delete rental request successfully")
    void testDeleteRentalRequest_Success() throws Exception {
        // Arrange
        doNothing().when(rentalRequestService).deleteRentalRequest(1L);

        // Act & Assert
        mockMvc.perform(delete("/rental-requests/1"))
                .andExpect(status().isNoContent());

        verify(rentalRequestService, times(1)).deleteRentalRequest(1L);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent request")
    void testDeleteRentalRequest_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Rental request not found"))
                .when(rentalRequestService).deleteRentalRequest(999L);

        // Act & Assert
        mockMvc.perform(delete("/rental-requests/999"))
                .andExpect(status().isNotFound());

        verify(rentalRequestService, times(1)).deleteRentalRequest(999L);
    }

    // ==================== DIRECT METHOD CALL TESTS ====================

    @Test
    @DisplayName("Should test approve request direct method call")
    void testApproveRequest_DirectCall() {
        // Arrange
        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setApprovedByUserId(10L);
        approvalRequest.setStartDate(LocalDate.now());
        approvalRequest.setEndDate(LocalDate.now().plusMonths(12));

        RentalRequest approvedRequest = createTestRentalRequest();
        approvedRequest.setStatus(RentalRequestStatus.APPROVED);

        when(rentalRequestService.approveRequest(eq(1L), eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(approvedRequest);

        // Act
        ResponseEntity<RentalRequest> response =
                rentalRequestController.approveRequest(1L, approvalRequest);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(RentalRequestStatus.APPROVED);

        verify(rentalRequestService, times(1))
                .approveRequest(eq(1L), eq(10L), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("Should test reject request direct method call")
    void testRejectRequest_DirectCall() {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        payload.put("reason", "Test rejection");
        payload.put("rejectedByUserId", 10);

        RentalRequest rejectedRequest = createTestRentalRequest();
        rejectedRequest.setStatus(RentalRequestStatus.REJECTED);

        when(rentalRequestService.rejectRequest(eq(1L), anyString(), anyLong()))
                .thenReturn(rejectedRequest);

        // Act
        ResponseEntity<RentalRequest> response =
                rentalRequestController.rejectRequest(1L, payload);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(RentalRequestStatus.REJECTED);

        verify(rentalRequestService, times(1))
                .rejectRequest(eq(1L), anyString(), anyLong());
    }

    @Test
    @DisplayName("Should test get request by ID direct method call")
    void testGetRequestById_DirectCall() {
        // Arrange
        when(rentalRequestService.getRequestById(1L)).thenReturn(Optional.of(testRequest));

        // Act
        ResponseEntity<RentalRequest> response = rentalRequestController.getRequestById(1L);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);

        verify(rentalRequestService, times(1)).getRequestById(1L);
    }

    @Test
    @DisplayName("Should verify service calls with correct parameters")
    void testServiceCallVerification() {
        // Arrange
        when(rentalRequestService.getRequestById(1L)).thenReturn(Optional.of(testRequest));

        // Act
        rentalRequestController.getRequestById(1L);

        // Assert - Verify exact parameter
        verify(rentalRequestService).getRequestById(argThat(id -> id.equals(1L)));
    }
}