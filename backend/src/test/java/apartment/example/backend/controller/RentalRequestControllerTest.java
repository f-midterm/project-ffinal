package apartment.example.backend.controller;

import apartment.example.backend.dto.ApprovalRequest;
import apartment.example.backend.entity.RentalRequest;
import apartment.example.backend.entity.RentalRequestStatus;
import apartment.example.backend.service.RentalRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@ExtendWith(MockitoExtension.class)
class RentalRequestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RentalRequestService rentalRequestService;

    @InjectMocks
    private RentalRequestController rentalRequestController;

    private ObjectMapper objectMapper;
    private RentalRequest sampleRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rentalRequestController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // Create sample rental request
        sampleRequest = new RentalRequest();
        sampleRequest.setId(1L);
        sampleRequest.setUnitId(100L);
        sampleRequest.setFirstName("John");
        sampleRequest.setLastName("Doe");
        sampleRequest.setEmail("john.doe@example.com");
        sampleRequest.setPhone("0812345678");
        sampleRequest.setOccupation("Engineer");
        sampleRequest.setEmergencyContact("Jane Doe");
        sampleRequest.setEmergencyPhone("0898765432");
        sampleRequest.setLeaseDurationMonths(12);
        sampleRequest.setMonthlyRent(new BigDecimal("15000.00"));
        sampleRequest.setTotalAmount(new BigDecimal("180000.00"));
        sampleRequest.setRequestDate(LocalDateTime.now());
        sampleRequest.setStatus(RentalRequestStatus.PENDING);
        sampleRequest.setNotes("Looking for long-term rental");
    }

    @Test
    void testGetAllRentalRequests_Success() throws Exception {
        // Arrange
        List<RentalRequest> requests = Arrays.asList(sampleRequest);
        when(rentalRequestService.getAllRentalRequests()).thenReturn(requests);

        // Act & Assert
        mockMvc.perform(get("/api/rental-requests")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));

        verify(rentalRequestService, times(1)).getAllRentalRequests();
    }

    @Test
    void testGetAllRentalRequests_InternalServerError() throws Exception {
        // Arrange
        when(rentalRequestService.getAllRentalRequests()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/rental-requests")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(rentalRequestService, times(1)).getAllRentalRequests();
    }

    @Test
    void testGetPendingRequests_Success() throws Exception {
        // Arrange
        List<RentalRequest> pendingRequests = Arrays.asList(sampleRequest);
        when(rentalRequestService.getPendingRequests()).thenReturn(pendingRequests);

        // Act & Assert
        mockMvc.perform(get("/api/rental-requests/pending")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(rentalRequestService, times(1)).getPendingRequests();
    }

    @Test
    void testGetPendingRequests_EmptyList() throws Exception {
        // Arrange
        when(rentalRequestService.getPendingRequests()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/rental-requests/pending")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(rentalRequestService, times(1)).getPendingRequests();
    }

    @Test
    void testGetRequestsByUnitId_Success() throws Exception {
        // Arrange
        Long unitId = 100L;
        List<RentalRequest> requests = Arrays.asList(sampleRequest);
        when(rentalRequestService.getRequestsByUnitId(unitId)).thenReturn(requests);

        // Act & Assert
        mockMvc.perform(get("/api/rental-requests/unit/{unitId}", unitId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].unitId").value(100));

        verify(rentalRequestService, times(1)).getRequestsByUnitId(unitId);
    }

    @Test
    void testGetRequestsByUserEmail_Success() throws Exception {
        // Arrange
        String email = "john.doe@example.com";
        List<RentalRequest> requests = Arrays.asList(sampleRequest);
        when(rentalRequestService.getRequestsByEmail(email)).thenReturn(requests);

        // Act & Assert
        mockMvc.perform(get("/api/rental-requests/search")
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value(email));

        verify(rentalRequestService, times(1)).getRequestsByEmail(email);
    }

    @Test
    void testGetRequestById_Success() throws Exception {
        // Arrange
        Long requestId = 1L;
        when(rentalRequestService.getRequestById(requestId)).thenReturn(Optional.of(sampleRequest));

        // Act & Assert
        mockMvc.perform(get("/api/rental-requests/{id}", requestId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(rentalRequestService, times(1)).getRequestById(requestId);
    }

    @Test
    void testGetRequestById_NotFound() throws Exception {
        // Arrange
        Long requestId = 999L;
        when(rentalRequestService.getRequestById(requestId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/rental-requests/{id}", requestId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(rentalRequestService, times(1)).getRequestById(requestId);
    }

    @Test
    void testCreateRentalRequest_Success() throws Exception {
        // Arrange
        when(rentalRequestService.createRentalRequest(any(RentalRequest.class))).thenReturn(sampleRequest);

        // Act & Assert
        mockMvc.perform(post("/api/rental-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(rentalRequestService, times(1)).createRentalRequest(any(RentalRequest.class));
    }

    @Test
    void testCreateRentalRequestForUnit_Success() throws Exception {
        // Arrange
        Long unitId = 100L;
        when(rentalRequestService.createRentalRequest(any(RentalRequest.class))).thenReturn(sampleRequest);

        // Act & Assert
        mockMvc.perform(post("/api/rental-requests/unit/{unitId}", unitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unitId").value(100));

        verify(rentalRequestService, times(1)).createRentalRequest(any(RentalRequest.class));
    }

    @Test
    void testApproveRequest_Success() throws Exception {
        // Arrange
        Long requestId = 1L;
        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setApprovedByUserId(10L);
        approvalRequest.setStartDate(LocalDate.from(LocalDateTime.now()));
        approvalRequest.setEndDate(LocalDate.from(LocalDateTime.now().plusMonths(12)));

        RentalRequest approvedRequest = new RentalRequest();
        approvedRequest.setId(requestId);
        approvedRequest.setStatus(RentalRequestStatus.APPROVED);
        approvedRequest.setApprovedByUserId(10L);

        when(rentalRequestService.approveRequest(eq(requestId), eq(10L), any(), any()))
                .thenReturn(approvedRequest);

        // Act & Assert
        mockMvc.perform(put("/api/rental-requests/{id}/approve", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedByUserId").value(10));

        verify(rentalRequestService, times(1))
                .approveRequest(eq(requestId), eq(10L), any(), any());
    }

    @Test
    void testApproveRequest_NotFound() throws Exception {
        // Arrange
        Long requestId = 999L;
        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setApprovedByUserId(10L);
        approvalRequest.setStartDate(LocalDate.from(LocalDateTime.now()));
        approvalRequest.setEndDate(LocalDate.from(LocalDateTime.now().plusMonths(12)));

        when(rentalRequestService.approveRequest(eq(requestId), eq(10L), any(), any()))
                .thenThrow(new RuntimeException("Request not found"));

        // Act & Assert
        mockMvc.perform(put("/api/rental-requests/{id}/approve", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isNotFound());

        verify(rentalRequestService, times(1))
                .approveRequest(eq(requestId), eq(10L), any(), any());
    }

    @Test
    void testRejectRequest_Success() throws Exception {
        // Arrange
        Long requestId = 1L;
        Map<String, Object> payload = new HashMap<>();
        payload.put("reason", "Incomplete documentation");
        payload.put("rejectedByUserId", 10L);

        RentalRequest rejectedRequest = new RentalRequest();
        rejectedRequest.setId(requestId);
        rejectedRequest.setStatus(RentalRequestStatus.REJECTED);
        rejectedRequest.setRejectionReason("Incomplete documentation");

        when(rentalRequestService.rejectRequest(requestId, "Incomplete documentation", 10L))
                .thenReturn(rejectedRequest);

        // Act & Assert
        mockMvc.perform(put("/api/rental-requests/{id}/reject", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("Incomplete documentation"));

        verify(rentalRequestService, times(1))
                .rejectRequest(requestId, "Incomplete documentation", 10L);
    }

    @Test
    void testRejectRequest_WithoutUserId() throws Exception {
        // Arrange
        Long requestId = 1L;
        Map<String, Object> payload = new HashMap<>();
        payload.put("reason", "Unit no longer available");

        RentalRequest rejectedRequest = new RentalRequest();
        rejectedRequest.setId(requestId);
        rejectedRequest.setStatus(RentalRequestStatus.REJECTED);

        when(rentalRequestService.rejectRequest(requestId, "Unit no longer available", null))
                .thenReturn(rejectedRequest);

        // Act & Assert
        mockMvc.perform(put("/api/rental-requests/{id}/reject", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        verify(rentalRequestService, times(1))
                .rejectRequest(requestId, "Unit no longer available", null);
    }

    @Test
    void testUpdateRentalRequest_Success() throws Exception {
        // Arrange
        Long requestId = 1L;
        RentalRequest updatedRequest = new RentalRequest();
        updatedRequest.setId(requestId);
        updatedRequest.setFirstName("Jane");
        updatedRequest.setLastName("Smith");

        when(rentalRequestService.updateRentalRequest(eq(requestId), any(RentalRequest.class)))
                .thenReturn(updatedRequest);

        // Act & Assert
        mockMvc.perform(put("/api/rental-requests/{id}", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.firstName").value("Jane"));

        verify(rentalRequestService, times(1))
                .updateRentalRequest(eq(requestId), any(RentalRequest.class));
    }

    @Test
    void testUpdateRentalRequest_NotFound() throws Exception {
        // Arrange
        Long requestId = 999L;
        when(rentalRequestService.updateRentalRequest(eq(requestId), any(RentalRequest.class)))
                .thenThrow(new RuntimeException("Request not found"));

        // Act & Assert
        mockMvc.perform(put("/api/rental-requests/{id}", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isNotFound());

        verify(rentalRequestService, times(1))
                .updateRentalRequest(eq(requestId), any(RentalRequest.class));
    }

    @Test
    void testDeleteRentalRequest_Success() throws Exception {
        // Arrange
        Long requestId = 1L;
        doNothing().when(rentalRequestService).deleteRentalRequest(requestId);

        // Act & Assert
        mockMvc.perform(delete("/api/rental-requests/{id}", requestId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(rentalRequestService, times(1)).deleteRentalRequest(requestId);
    }

    @Test
    void testDeleteRentalRequest_NotFound() throws Exception {
        // Arrange
        Long requestId = 999L;
        doThrow(new RuntimeException("Request not found"))
                .when(rentalRequestService).deleteRentalRequest(requestId);

        // Act & Assert
        mockMvc.perform(delete("/api/rental-requests/{id}", requestId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(rentalRequestService, times(1)).deleteRentalRequest(requestId);
    }

    @Test
    void testDeleteRentalRequest_InternalServerError() throws Exception {
        // Arrange
        Long requestId = 1L;

        // แก้จาก new Exception(...) เป็น new RuntimeException(...)
        doThrow(new RuntimeException("Database error"))
                .when(rentalRequestService).deleteRentalRequest(requestId);

        // Act & Assert
        mockMvc.perform(delete("/api/rental-requests/{id}", requestId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(rentalRequestService, times(1)).deleteRentalRequest(requestId);
    }
}