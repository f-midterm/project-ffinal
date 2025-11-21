package apartment.example.backend.controller;

import apartment.example.backend.dto.MaintenanceRequestDTO;
import apartment.example.backend.dto.MaintenanceScheduleDTO;
import apartment.example.backend.entity.*;
import apartment.example.backend.entity.MaintenanceRequest.Priority;
import apartment.example.backend.entity.MaintenanceRequest.Category;
import apartment.example.backend.entity.MaintenanceRequest.RequestStatus;
import apartment.example.backend.entity.enums.LeaseStatus;
import apartment.example.backend.repository.*;
import apartment.example.backend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceRequestControllerTest {

    @Mock
    private MaintenanceRequestService maintenanceRequestService;

    @Mock
    private MaintenanceRequestItemService itemService;

    @Mock
    private MaintenanceScheduleService maintenanceScheduleService;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LeaseRepository leaseRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MaintenanceRequestController controller;

    private MaintenanceRequest testRequest;
    private Unit testUnit;
    private Tenant testTenant;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUnit = new Unit();
        testUnit.setId(1L);
        testUnit.setRoomNumber("101");
        testUnit.setUnitType("Studio");

        testTenant = new Tenant();
        testTenant.setId(1L);
        testTenant.setFirstName("John");
        testTenant.setLastName("Doe");
        testTenant.setEmail("john.doe@example.com");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("johndoe");
        testUser.setEmail("john.doe@example.com");

        testRequest = new MaintenanceRequest();
        testRequest.setId(1L);
        testRequest.setTitle("Fix AC");
        testRequest.setDescription("AC not cooling");
        testRequest.setCategory(Category.HVAC);
        testRequest.setPriority(Priority.HIGH);
        testRequest.setStatus(RequestStatus.SUBMITTED);
        testRequest.setUnitId(1L);
        testRequest.setTenantId(1L);
        testRequest.setCreatedByUserId(1L);
        testRequest.setCreatedAt(LocalDateTime.now());
    }

    // ==================== CREATE REQUEST TESTS ====================

    @Test
    void createMaintenanceRequest_Success() {
        when(maintenanceRequestService.createMaintenanceRequest(any(MaintenanceRequest.class)))
                .thenReturn(testRequest);

        ResponseEntity<MaintenanceRequest> response = controller.createMaintenanceRequest(testRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testRequest.getId(), response.getBody().getId());
        verify(maintenanceRequestService, times(1)).createMaintenanceRequest(any(MaintenanceRequest.class));
    }

    @Test
    void createMaintenanceRequest_Error() {
        when(maintenanceRequestService.createMaintenanceRequest(any(MaintenanceRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<MaintenanceRequest> response = controller.createMaintenanceRequest(testRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ==================== UPLOAD ATTACHMENTS TESTS ====================

    @Test
    void uploadAttachments_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test image".getBytes()
        );
        MultipartFile[] files = {file};

        when(maintenanceRequestService.uploadAttachments(eq(1L), any()))
                .thenReturn(testRequest);

        ResponseEntity<?> response = controller.uploadAttachments(1L, files);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(maintenanceRequestService, times(1)).uploadAttachments(eq(1L), any());
    }

    @Test
    void uploadAttachments_NoFiles() {
        MultipartFile[] files = {};

        ResponseEntity<?> response = controller.uploadAttachments(1L, files);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(maintenanceRequestService, never()).uploadAttachments(anyLong(), any());
    }

    @Test
    void uploadAttachments_ExceedSizeLimit() {
        byte[] largeContent = new byte[16 * 1024 * 1024]; // 16MB
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.jpg", "image/jpeg", largeContent
        );
        MultipartFile[] files = {file};

        ResponseEntity<?> response = controller.uploadAttachments(1L, files);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(maintenanceRequestService, never()).uploadAttachments(anyLong(), any());
    }

    @Test
    void uploadAttachments_InvalidFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes()
        );
        MultipartFile[] files = {file};

        ResponseEntity<?> response = controller.uploadAttachments(1L, files);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(maintenanceRequestService, never()).uploadAttachments(anyLong(), any());
    }

    // ==================== GET ALL REQUESTS TESTS ====================

    @Test
    void getAllMaintenanceRequests_Success() {
        List<MaintenanceRequest> requests = Arrays.asList(testRequest);
        when(maintenanceRequestService.getAllMaintenanceRequests()).thenReturn(requests);
        when(unitRepository.findById(1L)).thenReturn(Optional.of(testUnit));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(tenantRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testTenant));

        ResponseEntity<List<MaintenanceRequestDTO>> response = controller.getAllMaintenanceRequests();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("101", response.getBody().get(0).getRoomNumber());
        assertEquals("John Doe", response.getBody().get(0).getTenantName());
    }

    @Test
    void getAllMaintenanceRequests_EmptyList() {
        when(maintenanceRequestService.getAllMaintenanceRequests()).thenReturn(Collections.emptyList());

        ResponseEntity<List<MaintenanceRequestDTO>> response = controller.getAllMaintenanceRequests();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // ==================== GET MY REQUESTS TESTS ====================

    @Test
    void getMyMaintenanceRequests_Success() {
        when(authentication.getName()).thenReturn("johndoe");
        when(userRepository.findByUsername("johndoe")).thenReturn(Arrays.asList(testUser));
        when(maintenanceRequestService.getRequestsByCreatedByUserId(1L))
                .thenReturn(Arrays.asList(testRequest));
        when(unitRepository.findById(1L)).thenReturn(Optional.of(testUnit));
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));

        ResponseEntity<List<MaintenanceRequestDTO>> response =
                controller.getMyMaintenanceRequests(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getMyMaintenanceRequests_Unauthorized() {
        ResponseEntity<List<MaintenanceRequestDTO>> response =
                controller.getMyMaintenanceRequests(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getMyMaintenanceRequests_UserNotFound() {
        when(authentication.getName()).thenReturn("unknownuser");
        when(userRepository.findByUsername("unknownuser")).thenReturn(Collections.emptyList());

        ResponseEntity<List<MaintenanceRequestDTO>> response =
                controller.getMyMaintenanceRequests(authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== GET BY ID TEST ====================

    @Test
    void getMaintenanceRequestById_Found() {
        when(maintenanceRequestService.getMaintenanceRequestById(1L))
                .thenReturn(Optional.of(testRequest));

        ResponseEntity<MaintenanceRequest> response = controller.getMaintenanceRequestById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void getMaintenanceRequestById_NotFound() {
        when(maintenanceRequestService.getMaintenanceRequestById(999L))
                .thenReturn(Optional.empty());

        ResponseEntity<MaintenanceRequest> response = controller.getMaintenanceRequestById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== UPDATE TESTS ====================

    @Test
    void updateMaintenanceRequest_Success() {
        MaintenanceRequest updatedRequest = new MaintenanceRequest();
        updatedRequest.setTitle("Updated Title");

        when(maintenanceRequestService.updateMaintenanceRequest(eq(1L), any()))
                .thenReturn(updatedRequest);

        ResponseEntity<MaintenanceRequest> response =
                controller.updateMaintenanceRequest(1L, updatedRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void updateMaintenanceRequest_NotFound() {
        when(maintenanceRequestService.updateMaintenanceRequest(eq(999L), any()))
                .thenThrow(new RuntimeException("Not found"));

        ResponseEntity<MaintenanceRequest> response =
                controller.updateMaintenanceRequest(999L, testRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== STATUS UPDATE TESTS ====================

    @Test
    void updateRequestStatus_Success() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "IN_PROGRESS");
        requestBody.put("notes", "Started work");

        testRequest.setStatus(RequestStatus.IN_PROGRESS);
        when(maintenanceRequestService.updateRequestStatus(1L, RequestStatus.IN_PROGRESS, "Started work"))
                .thenReturn(testRequest);

        ResponseEntity<MaintenanceRequest> response =
                controller.updateRequestStatus(1L, requestBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(RequestStatus.IN_PROGRESS, response.getBody().getStatus());
    }

    @Test
    void updateRequestStatus_InvalidStatus() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "INVALID_STATUS");

        ResponseEntity<MaintenanceRequest> response =
                controller.updateRequestStatus(1L, requestBody);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ==================== PRIORITY UPDATE TEST ====================

    @Test
    void updateRequestPriority_Success() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("priority", "URGENT");

        testRequest.setPriority(Priority.URGENT);
        when(maintenanceRequestService.updateRequestPriority(1L, Priority.URGENT))
                .thenReturn(testRequest);

        ResponseEntity<MaintenanceRequest> response =
                controller.updateRequestPriority(1L, requestBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Priority.URGENT, response.getBody().getPriority());
    }

    // ==================== ASSIGN REQUEST TEST ====================

    @Test
    void assignMaintenanceRequest_Success() {
        Map<String, Long> requestBody = new HashMap<>();
        requestBody.put("assignedToUserId", 2L);

        testRequest.setAssignedToUserId(2L);
        when(maintenanceRequestService.assignMaintenanceRequest(1L, 2L))
                .thenReturn(testRequest);

        ResponseEntity<MaintenanceRequest> response =
                controller.assignMaintenanceRequest(1L, requestBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2L, response.getBody().getAssignedToUserId());
    }

    // ==================== COMPLETE/REJECT TESTS ====================

    @Test
    void completeMaintenanceRequest_Success() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("completionNotes", "Fixed successfully");

        testRequest.setStatus(RequestStatus.COMPLETED);
        when(maintenanceRequestService.completeMaintenanceRequest(1L, "Fixed successfully"))
                .thenReturn(testRequest);

        ResponseEntity<MaintenanceRequest> response =
                controller.completeMaintenanceRequest(1L, requestBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void rejectMaintenanceRequest_Success() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("rejectionReason", "Invalid request");

        testRequest.setStatus(RequestStatus.CANCELLED);
        when(maintenanceRequestService.rejectMaintenanceRequest(1L, "Invalid request"))
                .thenReturn(testRequest);

        ResponseEntity<MaintenanceRequest> response =
                controller.rejectMaintenanceRequest(1L, requestBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ==================== DELETE TEST ====================

    @Test
    void deleteMaintenanceRequest_Success() {
        doNothing().when(maintenanceRequestService).deleteMaintenanceRequest(1L);

        ResponseEntity<Void> response = controller.deleteMaintenanceRequest(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(maintenanceRequestService, times(1)).deleteMaintenanceRequest(1L);
    }

    @Test
    void deleteMaintenanceRequest_NotFound() {
        doThrow(new RuntimeException("Not found"))
                .when(maintenanceRequestService).deleteMaintenanceRequest(999L);

        ResponseEntity<Void> response = controller.deleteMaintenanceRequest(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== STATISTICS TESTS ====================

    @Test
    void getMaintenanceRequestStats_Success() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", 100L);
        stats.put("pending", 20L);
        stats.put("completed", 80L);

        when(maintenanceRequestService.getMaintenanceRequestStats()).thenReturn(stats);

        ResponseEntity<Map<String, Long>> response = controller.getMaintenanceRequestStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(100L, response.getBody().get("total"));
    }

    @Test
    void getStatsByPriority_Success() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("HIGH", 30L);
        stats.put("MEDIUM", 50L);
        stats.put("LOW", 20L);

        when(maintenanceRequestService.getStatsByPriority()).thenReturn(stats);

        ResponseEntity<Map<String, Long>> response = controller.getStatsByPriority();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(30L, response.getBody().get("HIGH"));
    }

    // ==================== ITEMS TESTS ====================

    @Test
    void getRequestItems_Success() {
        List<MaintenanceRequestItem> items = Arrays.asList(new MaintenanceRequestItem());
        when(itemService.getItemsByRequestId(1L)).thenReturn(items);

        ResponseEntity<List<MaintenanceRequestItem>> response = controller.getRequestItems(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void addSingleItem_Success() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("stockId", 1L);
        payload.put("quantity", 5);
        payload.put("notes", "Test item");

        MaintenanceRequestItem item = new MaintenanceRequestItem();
        when(itemService.addItemToRequest(1L, 1L, 5, "Test item")).thenReturn(item);

        ResponseEntity<MaintenanceRequestItem> response =
                controller.addSingleItem(1L, payload);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void calculateItemsCost_Success() {
        BigDecimal cost = new BigDecimal("1500.00");
        when(itemService.calculateTotalCost(1L)).thenReturn(cost);

        ResponseEntity<Map<String, Object>> response = controller.calculateItemsCost(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cost, response.getBody().get("totalCost"));
        assertEquals("THB", response.getBody().get("currency"));
    }



    @Test
    void selectTimeSlot_Success() {
        Map<String, String> payload = new HashMap<>();
        payload.put("preferredDate", "2025-11-20");
        payload.put("preferredTime", "08:00");

        // Create a fresh request with correct status
        MaintenanceRequest pendingRequest = new MaintenanceRequest();
        pendingRequest.setId(1L);
        pendingRequest.setStatus(RequestStatus.PENDING_TENANT_CONFIRMATION);

        when(maintenanceRequestService.getMaintenanceRequestById(1L))
                .thenReturn(Optional.of(pendingRequest));

        // Create updated request to return
        MaintenanceRequest updatedRequest = new MaintenanceRequest();
        updatedRequest.setId(1L);
        updatedRequest.setStatus(RequestStatus.SUBMITTED);
        updatedRequest.setPreferredTime("2025-11-20 08:00");

        when(maintenanceRequestService.updateMaintenanceRequest(eq(1L), any(MaintenanceRequest.class)))
                .thenReturn(updatedRequest);

        ResponseEntity<?> response = controller.selectTimeSlot(1L, payload, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(maintenanceRequestService, times(1)).updateMaintenanceRequest(eq(1L), any(MaintenanceRequest.class));
    }

    @Test
    void selectTimeSlot_MissingDate() {
        Map<String, String> payload = new HashMap<>();
        payload.put("preferredTime", "08:00");

        ResponseEntity<?> response = controller.selectTimeSlot(1L, payload, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void selectTimeSlot_WrongStatus() {
        Map<String, String> payload = new HashMap<>();
        payload.put("preferredDate", "2025-11-20");
        payload.put("preferredTime", "08:00");

        testRequest.setStatus(RequestStatus.COMPLETED);
        when(maintenanceRequestService.getMaintenanceRequestById(1L))
                .thenReturn(Optional.of(testRequest));

        ResponseEntity<?> response = controller.selectTimeSlot(1L, payload, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getAvailableTimeSlots_Success() {
        when(authentication.getName()).thenReturn("johndoe");
        when(userRepository.findByUsername("johndoe")).thenReturn(Arrays.asList(testUser));

        Lease lease = new Lease();
        lease.setUnit(testUnit);
        when(leaseRepository.findByStatusAndTenantEmail(LeaseStatus.ACTIVE, "john.doe@example.com"))
                .thenReturn(Arrays.asList(lease));

        when(maintenanceRequestService.getAllMaintenanceRequests())
                .thenReturn(Collections.emptyList());

        ResponseEntity<?> response = controller.getAvailableTimeSlots("2025-11-20", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}