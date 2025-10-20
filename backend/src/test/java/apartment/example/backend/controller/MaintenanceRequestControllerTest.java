package apartment.example.backend.controller;

import apartment.example.backend.entity.MaintenanceRequest;
import apartment.example.backend.entity.MaintenanceRequest.Priority;
import apartment.example.backend.entity.MaintenanceRequest.Category;
import apartment.example.backend.entity.MaintenanceRequest.RequestStatus;
import apartment.example.backend.entity.Tenant;
import apartment.example.backend.entity.Unit;
import apartment.example.backend.repository.TenantRepository;
import apartment.example.backend.repository.UnitRepository;
import apartment.example.backend.service.MaintenanceRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceRequestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MaintenanceRequestService maintenanceRequestService;

    @Mock
    private UnitRepository unitRepository; // <-- เพิ่มบรรทัดนี้

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private MaintenanceRequestController maintenanceRequestController;

    private ObjectMapper objectMapper;
    private MaintenanceRequest testRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(maintenanceRequestController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testRequest = new MaintenanceRequest();
        testRequest.setId(1L);
        testRequest.setTenantId(100L);
        testRequest.setUnitId(200L);
        testRequest.setTitle("Leaking Faucet");
        testRequest.setDescription("Kitchen faucet is leaking");
        testRequest.setCategory(Category.PLUMBING);
        testRequest.setPriority(Priority.MEDIUM);
        testRequest.setStatus(RequestStatus.IN_PROGRESS);
    }

    @Test
    void testEndpoint_ShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(get("/api/maintenance-requests/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("MaintenanceRequestController is working!"));
    }

    @Test
    void createMaintenanceRequest_Success() throws Exception {
        when(maintenanceRequestService.createMaintenanceRequest(any(MaintenanceRequest.class)))
                .thenReturn(testRequest);

        mockMvc.perform(post("/api/maintenance-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Leaking Faucet"))
                .andExpect(jsonPath("$.category").value("PLUMBING"));

        verify(maintenanceRequestService, times(1)).createMaintenanceRequest(any(MaintenanceRequest.class));
    }

    @Test
    void createMaintenanceRequest_Failure() throws Exception {
        when(maintenanceRequestService.createMaintenanceRequest(any(MaintenanceRequest.class)))
                .thenThrow(new RuntimeException("Creation failed"));

        mockMvc.perform(post("/api/maintenance-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());

        verify(maintenanceRequestService, times(1)).createMaintenanceRequest(any(MaintenanceRequest.class));
    }

    @Test
    void getAllMaintenanceRequests_Success() throws Exception {
        MaintenanceRequest testRequest = new MaintenanceRequest();
        testRequest.setId(1L);
        testRequest.setTitle("Leaking Faucet");
        testRequest.setUnitId(101L);      // ID ที่จะใช้เรียก unitRepository
        testRequest.setTenantId(202L);    // ID ที่จะใช้เรียก tenantRepository

        Unit testUnit = new Unit();
        testUnit.setRoomNumber("A101");

        Tenant testTenant = new Tenant();
        testTenant.setFirstName("Jane");
        testTenant.setLastName("Doe");

        // --- 2. ตั้งค่า Mock (สำคัญที่สุด) ---
        when(maintenanceRequestService.getAllMaintenanceRequests()).thenReturn(Arrays.asList(testRequest));

        // สอน mock ว่าถ้า findById(101L) ถูกเรียก ให้คืน testUnit กลับไป
        when(unitRepository.findById(101L)).thenReturn(Optional.of(testUnit)); // <-- เพิ่มบรรทัดนี้

        // สอน mock ว่าถ้า findById(202L) ถูกเรียก ให้คืน testTenant กลับไป
        when(tenantRepository.findById(202L)).thenReturn(Optional.of(testTenant)); // <-- เพิ่มบรรทัดนี้

        // --- 3. ยิง Request และตรวจสอบผลลัพธ์ ---
        mockMvc.perform(get("/api/maintenance-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Leaking Faucet"))
                .andExpect(jsonPath("$[0].roomNumber").value("A101")); // ตรวจสอบผลลัพธ์จาก mock ได้
    }

    @Test
    void getAllMaintenanceRequests_InternalServerError() throws Exception {
        when(maintenanceRequestService.getAllMaintenanceRequests())
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/maintenance-requests"))
                .andExpect(status().isInternalServerError());

        verify(maintenanceRequestService, times(1)).getAllMaintenanceRequests();
    }

    @Test
    void getMaintenanceRequestById_Found() throws Exception {
        when(maintenanceRequestService.getMaintenanceRequestById(1L))
                .thenReturn(Optional.of(testRequest));

        mockMvc.perform(get("/api/maintenance-requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Leaking Faucet"));

        verify(maintenanceRequestService, times(1)).getMaintenanceRequestById(1L);
    }

    @Test
    void getMaintenanceRequestById_NotFound() throws Exception {
        when(maintenanceRequestService.getMaintenanceRequestById(999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/maintenance-requests/999"))
                .andExpect(status().isNotFound());

        verify(maintenanceRequestService, times(1)).getMaintenanceRequestById(999L);
    }

    @Test
    void getRequestsByTenantId_Success() throws Exception {
        List<MaintenanceRequest> requests = Arrays.asList(testRequest);
        when(maintenanceRequestService.getRequestsByTenantId(100L)).thenReturn(requests);

        mockMvc.perform(get("/api/maintenance-requests/tenant/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tenantId").value(100L));

        verify(maintenanceRequestService, times(1)).getRequestsByTenantId(100L);
    }

    @Test
    void getRequestsByUnitId_Success() throws Exception {
        List<MaintenanceRequest> requests = Arrays.asList(testRequest);
        when(maintenanceRequestService.getRequestsByUnitId(200L)).thenReturn(requests);

        mockMvc.perform(get("/api/maintenance-requests/unit/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].unitId").value(200L));

        verify(maintenanceRequestService, times(1)).getRequestsByUnitId(200L);
    }

    @Test
    void getRequestsByStatus_Success() throws Exception {
        List<MaintenanceRequest> requests = Arrays.asList(testRequest);
        when(maintenanceRequestService.getRequestsByStatus(RequestStatus.IN_PROGRESS))
                .thenReturn(requests);

        mockMvc.perform(get("/api/maintenance-requests/status/IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));

        verify(maintenanceRequestService, times(1)).getRequestsByStatus(RequestStatus.IN_PROGRESS);
    }

    @Test
    void getRequestsByPriority_Success() throws Exception {
        List<MaintenanceRequest> requests = Arrays.asList(testRequest);
        when(maintenanceRequestService.getRequestsByPriority(Priority.MEDIUM))
                .thenReturn(requests);

        mockMvc.perform(get("/api/maintenance-requests/priority/MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority").value("MEDIUM"));

        verify(maintenanceRequestService, times(1)).getRequestsByPriority(Priority.MEDIUM);
    }

    @Test
    void getRequestsByCategory_Success() throws Exception {
        List<MaintenanceRequest> requests = Arrays.asList(testRequest);
        when(maintenanceRequestService.getRequestsByCategory(Category.PLUMBING))
                .thenReturn(requests);

        mockMvc.perform(get("/api/maintenance-requests/category/PLUMBING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("PLUMBING"));

        verify(maintenanceRequestService, times(1)).getRequestsByCategory(Category.PLUMBING);
    }

    @Test
    void getOpenRequests_Success() throws Exception {
        List<MaintenanceRequest> requests = Arrays.asList(testRequest);
        when(maintenanceRequestService.getOpenRequests()).thenReturn(requests);

        mockMvc.perform(get("/api/maintenance-requests/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(maintenanceRequestService, times(1)).getOpenRequests();
    }

    @Test
    void getHighPriorityRequests_Success() throws Exception {
        testRequest.setPriority(Priority.HIGH);
        List<MaintenanceRequest> requests = Arrays.asList(testRequest);
        when(maintenanceRequestService.getHighPriorityRequests()).thenReturn(requests);

        mockMvc.perform(get("/api/maintenance-requests/high-priority"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority").value("HIGH"));

        verify(maintenanceRequestService, times(1)).getHighPriorityRequests();
    }

    @Test
    void updateMaintenanceRequest_Success() throws Exception {
        MaintenanceRequest updatedRequest = new MaintenanceRequest();
        updatedRequest.setId(1L);
        updatedRequest.setTitle("Updated Title");

        when(maintenanceRequestService.updateMaintenanceRequest(eq(1L), any(MaintenanceRequest.class)))
                .thenReturn(updatedRequest);

        mockMvc.perform(put("/api/maintenance-requests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));

        verify(maintenanceRequestService, times(1)).updateMaintenanceRequest(eq(1L), any(MaintenanceRequest.class));
    }

    @Test
    void updateMaintenanceRequest_NotFound() throws Exception {
        when(maintenanceRequestService.updateMaintenanceRequest(eq(999L), any(MaintenanceRequest.class)))
                .thenThrow(new RuntimeException("Request not found"));

        mockMvc.perform(put("/api/maintenance-requests/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isNotFound());

        verify(maintenanceRequestService, times(1)).updateMaintenanceRequest(eq(999L), any(MaintenanceRequest.class));
    }

    @Test
    void assignMaintenanceRequest_Success() throws Exception {
        testRequest.setAssignedToUserId(300L);
        when(maintenanceRequestService.assignMaintenanceRequest(1L, 300L))
                .thenReturn(testRequest);

        Map<String, Long> requestBody = new HashMap<>();
        requestBody.put("assignedToUserId", 300L);

        mockMvc.perform(put("/api/maintenance-requests/1/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedToUserId").value(300L));

        verify(maintenanceRequestService, times(1)).assignMaintenanceRequest(1L, 300L);
    }

    @Test
    void assignMaintenanceRequest_NotFound() throws Exception {
        when(maintenanceRequestService.assignMaintenanceRequest(999L, 300L))
                .thenThrow(new RuntimeException("Request not found"));

        Map<String, Long> requestBody = new HashMap<>();
        requestBody.put("assignedToUserId", 300L);

        mockMvc.perform(put("/api/maintenance-requests/999/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound());

        verify(maintenanceRequestService, times(1)).assignMaintenanceRequest(999L, 300L);
    }

    @Test
    void updateRequestStatus_Success() throws Exception {
        testRequest.setStatus(RequestStatus.IN_PROGRESS);
        when(maintenanceRequestService.updateRequestStatus(1L, RequestStatus.IN_PROGRESS, "Starting work"))
                .thenReturn(testRequest);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "IN_PROGRESS");
        requestBody.put("notes", "Starting work");

        mockMvc.perform(put("/api/maintenance-requests/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(maintenanceRequestService, times(1)).updateRequestStatus(1L, RequestStatus.IN_PROGRESS, "Starting work");
    }

    @Test
    void updateRequestStatus_InvalidStatus() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "INVALID_STATUS");
        requestBody.put("notes", "Test");

        mockMvc.perform(put("/api/maintenance-requests/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRequestPriority_Success() throws Exception {
        testRequest.setPriority(Priority.HIGH);
        when(maintenanceRequestService.updateRequestPriority(1L, Priority.HIGH))
                .thenReturn(testRequest);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("priority", "HIGH");

        mockMvc.perform(put("/api/maintenance-requests/1/priority")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("HIGH"));

        verify(maintenanceRequestService, times(1)).updateRequestPriority(1L, Priority.HIGH);
    }

    @Test
    void updateRequestPriority_InvalidPriority() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("priority", "INVALID_PRIORITY");

        mockMvc.perform(put("/api/maintenance-requests/1/priority")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeMaintenanceRequest_Success() throws Exception {
        testRequest.setStatus(RequestStatus.COMPLETED);
        when(maintenanceRequestService.completeMaintenanceRequest(1L, "Fixed the leak"))
                .thenReturn(testRequest);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("completionNotes", "Fixed the leak");

        mockMvc.perform(put("/api/maintenance-requests/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(maintenanceRequestService, times(1)).completeMaintenanceRequest(1L, "Fixed the leak");
    }

    @Test
    void completeMaintenanceRequest_NotFound() throws Exception {
        when(maintenanceRequestService.completeMaintenanceRequest(999L, "Fixed the leak"))
                .thenThrow(new RuntimeException("Request not found"));

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("completionNotes", "Fixed the leak");

        mockMvc.perform(put("/api/maintenance-requests/999/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound());

        verify(maintenanceRequestService, times(1)).completeMaintenanceRequest(999L, "Fixed the leak");
    }

    @Test
    void rejectMaintenanceRequest_Success() throws Exception {
        testRequest.setStatus(RequestStatus.CANCELLED);
        when(maintenanceRequestService.rejectMaintenanceRequest(1L, "Not a valid request"))
                .thenReturn(testRequest);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("rejectionReason", "Not a valid request");

        mockMvc.perform(put("/api/maintenance-requests/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(maintenanceRequestService, times(1)).rejectMaintenanceRequest(1L, "Not a valid request");
    }

    @Test
    void rejectMaintenanceRequest_NotFound() throws Exception {
        when(maintenanceRequestService.rejectMaintenanceRequest(999L, "Not a valid request"))
                .thenThrow(new RuntimeException("Request not found"));

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("rejectionReason", "Not a valid request");

        mockMvc.perform(put("/api/maintenance-requests/999/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound());

        verify(maintenanceRequestService, times(1)).rejectMaintenanceRequest(999L, "Not a valid request");
    }

    @Test
    void deleteMaintenanceRequest_Success() throws Exception {
        doNothing().when(maintenanceRequestService).deleteMaintenanceRequest(1L);

        mockMvc.perform(delete("/api/maintenance-requests/1"))
                .andExpect(status().isNoContent());

        verify(maintenanceRequestService, times(1)).deleteMaintenanceRequest(1L);
    }

    @Test
    void deleteMaintenanceRequest_NotFound() throws Exception {
        doThrow(new RuntimeException("Request not found"))
                .when(maintenanceRequestService).deleteMaintenanceRequest(999L);

        mockMvc.perform(delete("/api/maintenance-requests/999"))
                .andExpect(status().isNotFound());

        verify(maintenanceRequestService, times(1)).deleteMaintenanceRequest(999L);
    }

    @Test
    void getMaintenanceRequestStats_Success() throws Exception {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", 100L);
        stats.put("pending", 30L);
        stats.put("completed", 50L);

        when(maintenanceRequestService.getMaintenanceRequestStats()).thenReturn(stats);

        mockMvc.perform(get("/api/maintenance-requests/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(100L))
                .andExpect(jsonPath("$.pending").value(30L))
                .andExpect(jsonPath("$.completed").value(50L));

        verify(maintenanceRequestService, times(1)).getMaintenanceRequestStats();
    }

    @Test
    void getStatsByPriority_Success() throws Exception {
        Map<String, Long> stats = new HashMap<>();
        stats.put("HIGH", 20L);
        stats.put("MEDIUM", 50L);
        stats.put("LOW", 30L);

        when(maintenanceRequestService.getStatsByPriority()).thenReturn(stats);

        mockMvc.perform(get("/api/maintenance-requests/stats/priority"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.HIGH").value(20L))
                .andExpect(jsonPath("$.MEDIUM").value(50L))
                .andExpect(jsonPath("$.LOW").value(30L));

        verify(maintenanceRequestService, times(1)).getStatsByPriority();
    }

    @Test
    void getStatsByCategory_Success() throws Exception {
        Map<String, Long> stats = new HashMap<>();
        stats.put("PLUMBING", 25L);
        stats.put("ELECTRICAL", 35L);
        stats.put("GENERAL", 40L);

        when(maintenanceRequestService.getStatsByCategory()).thenReturn(stats);

        mockMvc.perform(get("/api/maintenance-requests/stats/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PLUMBING").value(25L))
                .andExpect(jsonPath("$.ELECTRICAL").value(35L))
                .andExpect(jsonPath("$.GENERAL").value(40L));

        verify(maintenanceRequestService, times(1)).getStatsByCategory();
    }
}