package apartment.example.backend.Service;

import apartment.example.backend.entity.MaintenanceRequest;
import apartment.example.backend.entity.MaintenanceRequest.Priority;
import apartment.example.backend.entity.MaintenanceRequest.Category;
import apartment.example.backend.entity.MaintenanceRequest.RequestStatus;
import apartment.example.backend.repository.MaintenanceRequestRepository;
import apartment.example.backend.service.MaintenanceRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceRequestServiceTest {

    @Mock
    private MaintenanceRequestRepository maintenanceRequestRepository;

    @InjectMocks
    private MaintenanceRequestService maintenanceRequestService;

    private MaintenanceRequest testRequest;

    @BeforeEach
    void setUp() {
        testRequest = new MaintenanceRequest();
        testRequest.setId(1L);
        testRequest.setTenantId(100L);
        testRequest.setUnitId(200L);
        testRequest.setTitle("Fix leaking faucet");
        testRequest.setDescription("Kitchen faucet is leaking");
        testRequest.setPriority(Priority.HIGH);
        testRequest.setCategory(Category.PLUMBING);
        testRequest.setStatus(RequestStatus.SUBMITTED);
        testRequest.setSubmittedDate(LocalDateTime.now());
    }

    // ==================== CREATE ====================

    @Test
    void createMaintenanceRequest_ShouldSetSubmittedDateAndStatus() {
        // Arrange
        MaintenanceRequest newRequest = new MaintenanceRequest();
        newRequest.setTitle("New request");
        newRequest.setDescription("Test description");

        when(maintenanceRequestRepository.save(any(MaintenanceRequest.class)))
                .thenReturn(newRequest);

        // Act
        MaintenanceRequest result = maintenanceRequestService.createMaintenanceRequest(newRequest);

        // Assert
        assertNotNull(result.getSubmittedDate());
        assertEquals(RequestStatus.SUBMITTED, result.getStatus());
        verify(maintenanceRequestRepository, times(1)).save(newRequest);
    }

    // ==================== READ/GET ====================

    @Test
    void getAllMaintenanceRequests_ShouldReturnAllRequests() {
        // Arrange
        List<MaintenanceRequest> requests = Arrays.asList(testRequest, new MaintenanceRequest());
        when(maintenanceRequestRepository.findAll()).thenReturn(requests);

        // Act
        List<MaintenanceRequest> result = maintenanceRequestService.getAllMaintenanceRequests();

        // Assert
        assertEquals(2, result.size());
        verify(maintenanceRequestRepository, times(1)).findAll();
    }

    @Test
    void getMaintenanceRequestById_WhenExists_ShouldReturnRequest() {
        // Arrange
        when(maintenanceRequestRepository.findById(1L))
                .thenReturn(Optional.of(testRequest));

        // Act
        Optional<MaintenanceRequest> result = maintenanceRequestService.getMaintenanceRequestById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testRequest.getId(), result.get().getId());
        verify(maintenanceRequestRepository, times(1)).findById(1L);
    }

    @Test
    void getMaintenanceRequestById_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(maintenanceRequestRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act
        Optional<MaintenanceRequest> result = maintenanceRequestService.getMaintenanceRequestById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getRequestsByTenantId_ShouldReturnTenantRequests() {
        // Arrange
        List<MaintenanceRequest> requests = Arrays.asList(testRequest);
        when(maintenanceRequestRepository.findByTenantId(100L)).thenReturn(requests);

        // Act
        List<MaintenanceRequest> result = maintenanceRequestService.getRequestsByTenantId(100L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getTenantId());
        verify(maintenanceRequestRepository, times(1)).findByTenantId(100L);
    }

    @Test
    void getRequestsByUnitId_ShouldReturnUnitRequests() {
        // Arrange
        List<MaintenanceRequest> requests = Arrays.asList(testRequest);
        when(maintenanceRequestRepository.findByUnitId(200L)).thenReturn(requests);

        // Act
        List<MaintenanceRequest> result = maintenanceRequestService.getRequestsByUnitId(200L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(200L, result.get(0).getUnitId());
        verify(maintenanceRequestRepository, times(1)).findByUnitId(200L);
    }

    @Test
    void getRequestsByStatus_ShouldReturnRequestsWithStatus() {
        // Arrange
        List<MaintenanceRequest> requests = Arrays.asList(testRequest);
        when(maintenanceRequestRepository.findByStatus(RequestStatus.SUBMITTED))
                .thenReturn(requests);

        // Act
        List<MaintenanceRequest> result = maintenanceRequestService
                .getRequestsByStatus(RequestStatus.SUBMITTED);

        // Assert
        assertEquals(1, result.size());
        assertEquals(RequestStatus.SUBMITTED, result.get(0).getStatus());
    }

    @Test
    void getRequestsByPriority_ShouldReturnRequestsWithPriority() {
        // Arrange
        List<MaintenanceRequest> requests = Arrays.asList(testRequest);
        when(maintenanceRequestRepository.findByPriority(Priority.HIGH))
                .thenReturn(requests);

        // Act
        List<MaintenanceRequest> result = maintenanceRequestService
                .getRequestsByPriority(Priority.HIGH);

        // Assert
        assertEquals(1, result.size());
        assertEquals(Priority.HIGH, result.get(0).getPriority());
    }

    @Test
    void getRequestsByCategory_ShouldReturnRequestsWithCategory() {
        // Arrange
        List<MaintenanceRequest> requests = Arrays.asList(testRequest);
        when(maintenanceRequestRepository.findByCategory(Category.PLUMBING))
                .thenReturn(requests);

        // Act
        List<MaintenanceRequest> result = maintenanceRequestService
                .getRequestsByCategory(Category.PLUMBING);

        // Assert
        assertEquals(1, result.size());
        assertEquals(Category.PLUMBING, result.get(0).getCategory());
    }

    @Test
    void getOpenRequests_ShouldReturnOpenRequests() {
        // Arrange
        List<MaintenanceRequest> requests = Arrays.asList(testRequest);
        when(maintenanceRequestRepository.findOpenRequests()).thenReturn(requests);

        // Act
        List<MaintenanceRequest> result = maintenanceRequestService.getOpenRequests();

        // Assert
        assertEquals(1, result.size());
        verify(maintenanceRequestRepository, times(1)).findOpenRequests();
    }

    @Test
    void getHighPriorityRequests_ShouldReturnHighPriorityRequests() {
        // Arrange
        List<MaintenanceRequest> requests = Arrays.asList(testRequest);
        when(maintenanceRequestRepository
                .findByPriorityOrderBySubmittedDateDesc(Priority.HIGH))
                .thenReturn(requests);

        // Act
        List<MaintenanceRequest> result = maintenanceRequestService.getHighPriorityRequests();

        // Assert
        assertEquals(1, result.size());
        verify(maintenanceRequestRepository, times(1))
                .findByPriorityOrderBySubmittedDateDesc(Priority.HIGH);
    }

    // ==================== UPDATE ====================

    @Test
    void updateMaintenanceRequest_WhenExists_ShouldUpdateFields() {
        // Arrange
        MaintenanceRequest updateData = new MaintenanceRequest();
        updateData.setTitle("Updated title");
        updateData.setDescription("Updated description");
        updateData.setPriority(Priority.URGENT);
        updateData.setCategory(Category.ELECTRICAL);

        when(maintenanceRequestRepository.findById(1L))
                .thenReturn(Optional.of(testRequest));
        when(maintenanceRequestRepository.save(any(MaintenanceRequest.class)))
                .thenReturn(testRequest);

        // Act
        MaintenanceRequest result = maintenanceRequestService
                .updateMaintenanceRequest(1L, updateData);

        // Assert
        assertEquals("Updated title", result.getTitle());
        assertEquals("Updated description", result.getDescription());
        assertEquals(Priority.URGENT, result.getPriority());
        assertEquals(Category.ELECTRICAL, result.getCategory());
        verify(maintenanceRequestRepository, times(1)).save(testRequest);
    }

    @Test
    void updateMaintenanceRequest_WhenNotExists_ShouldThrowException() {
        // Arrange
        MaintenanceRequest updateData = new MaintenanceRequest();
        when(maintenanceRequestRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            maintenanceRequestService.updateMaintenanceRequest(999L, updateData);
        });

        assertTrue(exception.getMessage().contains("Request not found"));
    }

    @Test
    void updateMaintenanceRequest_WithNullFields_ShouldNotUpdateNullFields() {
        // Arrange
        MaintenanceRequest updateData = new MaintenanceRequest();
        updateData.setTitle("New title");
        // ตั้งค่า priority, category กลับเป็น null เพื่อทดสอบว่าไม่ถูกอัปเดต
        updateData.setPriority(null);
        updateData.setCategory(null);
        updateData.setDescription(null);

        String originalDescription = testRequest.getDescription();
        Priority originalPriority = testRequest.getPriority();
        Category originalCategory = testRequest.getCategory();

        when(maintenanceRequestRepository.findById(1L))
                .thenReturn(Optional.of(testRequest));
        when(maintenanceRequestRepository.save(any(MaintenanceRequest.class)))
                .thenReturn(testRequest);

        // Act
        MaintenanceRequest result = maintenanceRequestService
                .updateMaintenanceRequest(1L, updateData);

        // Assert
        assertEquals("New title", result.getTitle()); // อัปเดต
        assertEquals(originalDescription, result.getDescription()); // ไม่เปลี่ยน
        assertEquals(originalPriority, result.getPriority()); // ไม่เปลี่ยน
        assertEquals(originalCategory, result.getCategory()); // ไม่เปลี่ยน
    }

    @Test
    void assignMaintenanceRequest_ShouldUpdateAssignedUserAndStatus() {
        // Arrange
        when(maintenanceRequestRepository.findById(1L))
                .thenReturn(Optional.of(testRequest));
        when(maintenanceRequestRepository.save(any(MaintenanceRequest.class)))
                .thenReturn(testRequest);

        // Act
        MaintenanceRequest result = maintenanceRequestService
                .assignMaintenanceRequest(1L, 500L);

        // Assert
        assertEquals(500L, result.getAssignedToUserId());
        assertEquals(RequestStatus.IN_PROGRESS, result.getStatus());
        verify(maintenanceRequestRepository, times(1)).save(testRequest);
    }

    @Test
    void assignMaintenanceRequest_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(maintenanceRequestRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            maintenanceRequestService.assignMaintenanceRequest(999L, 500L);
        });
    }

    @Test
    void updateRequestStatus_ShouldUpdateStatusAndNotes() {
        // Arrange
        when(maintenanceRequestRepository.findById(1L))
                .thenReturn(Optional.of(testRequest));
        when(maintenanceRequestRepository.save(any(MaintenanceRequest.class)))
                .thenReturn(testRequest);

        // Act
        MaintenanceRequest result = maintenanceRequestService
                .updateRequestStatus(1L, RequestStatus.IN_PROGRESS, "Work in progress");

        // Assert
        assertEquals(RequestStatus.IN_PROGRESS, result.getStatus());
        assertEquals("Work in progress", result.getCompletionNotes());
        verify(maintenanceRequestRepository, times(1)).save(testRequest);
    }

    @Test
    void updateRequestStatus_ToCompleted_ShouldSetCompletedDate() {
        // Arrange
        when(maintenanceRequestRepository.findById(1L))
                .thenReturn(Optional.of(testRequest));
        when(maintenanceRequestRepository.save(any(MaintenanceRequest.class)))
                .thenReturn(testRequest);

        // Act
        MaintenanceRequest result = maintenanceRequestService
                .updateRequestStatus(1L, RequestStatus.COMPLETED, "Work completed");

        // Assert
        assertEquals(RequestStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getCompletedDate());
    }

    @Test
    void updateRequestPriority_ShouldUpdatePriority() {
        // Arrange
        when(maintenanceRequestRepository.findById(1L))
                .thenReturn(Optional.of(testRequest));
        when(maintenanceRequestRepository.save(any(MaintenanceRequest.class)))
                .thenReturn(testRequest);

        // Act
        MaintenanceRequest result = maintenanceRequestService
                .updateRequestPriority(1L, Priority.URGENT);

        // Assert
        assertEquals(Priority.URGENT, result.getPriority());
        verify(maintenanceRequestRepository, times(1)).save(testRequest);
    }

    @Test
    void completeMaintenanceRequest_ShouldSetStatusAndCompletedDate() {
        // Arrange
        when(maintenanceRequestRepository.findById(1L))
                .thenReturn(Optional.of(testRequest));
        when(maintenanceRequestRepository.save(any(MaintenanceRequest.class)))
                .thenReturn(testRequest);

        // Act
        MaintenanceRequest result = maintenanceRequestService
                .completeMaintenanceRequest(1L, "Successfully fixed");

        // Assert
        assertEquals(RequestStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getCompletedDate());
        assertEquals("Successfully fixed", result.getCompletionNotes());
    }

    @Test
    void rejectMaintenanceRequest_ShouldSetCancelledStatusAndReason() {
        // Arrange
        when(maintenanceRequestRepository.findById(1L))
                .thenReturn(Optional.of(testRequest));
        when(maintenanceRequestRepository.save(any(MaintenanceRequest.class)))
                .thenReturn(testRequest);

        // Act
        MaintenanceRequest result = maintenanceRequestService
                .rejectMaintenanceRequest(1L, "Not a valid request");

        // Assert
        assertEquals(RequestStatus.CANCELLED, result.getStatus());
        assertEquals("Not a valid request", result.getCompletionNotes());
    }

    // ==================== DELETE ====================

    @Test
    void deleteMaintenanceRequest_ShouldCallRepositoryDelete() {
        // Arrange
        doNothing().when(maintenanceRequestRepository).deleteById(1L);

        // Act
        maintenanceRequestService.deleteMaintenanceRequest(1L);

        // Assert
        verify(maintenanceRequestRepository, times(1)).deleteById(1L);
    }

    // ==================== STATISTICS ====================

    @Test
    void getMaintenanceRequestStats_ShouldReturnAllStats() {
        // Arrange
        List<MaintenanceRequest> allRequests = Arrays.asList(testRequest, new MaintenanceRequest());
        when(maintenanceRequestRepository.findAll()).thenReturn(allRequests);
        when(maintenanceRequestRepository.countByStatus(any(RequestStatus.class))).thenReturn(1L);

        // Act
        Map<String, Long> stats = maintenanceRequestService.getMaintenanceRequestStats();

        // Assert
        assertEquals(2L, stats.get("TOTAL"));
        assertTrue(stats.containsKey("SUBMITTED"));
        assertTrue(stats.containsKey("IN_PROGRESS"));
        assertTrue(stats.containsKey("COMPLETED"));
        assertTrue(stats.containsKey("CANCELLED"));
        verify(maintenanceRequestRepository, times(1)).findAll();
    }

    @Test
    void getStatsByPriority_ShouldReturnPriorityStats() {
        // Arrange
        when(maintenanceRequestRepository.countByPriority(Priority.LOW)).thenReturn(5L);
        when(maintenanceRequestRepository.countByPriority(Priority.MEDIUM)).thenReturn(10L);
        when(maintenanceRequestRepository.countByPriority(Priority.HIGH)).thenReturn(3L);
        when(maintenanceRequestRepository.countByPriority(Priority.URGENT)).thenReturn(2L);

        // Act
        Map<String, Long> stats = maintenanceRequestService.getStatsByPriority();

        // Assert
        assertEquals(5L, stats.get("LOW"));
        assertEquals(10L, stats.get("MEDIUM"));
        assertEquals(3L, stats.get("HIGH"));
        assertEquals(2L, stats.get("URGENT"));
    }

    @Test
    void getStatsByCategory_ShouldReturnCategoryStats() {
        // Arrange
        when(maintenanceRequestRepository.countByCategory(any(Category.class)))
                .thenReturn(2L);

        // Act
        Map<String, Long> stats = maintenanceRequestService.getStatsByCategory();

        // Assert
        assertEquals(Category.values().length, stats.size());
        assertTrue(stats.containsKey("PLUMBING"));
        assertTrue(stats.containsKey("ELECTRICAL"));
        assertTrue(stats.containsKey("HVAC"));
        verify(maintenanceRequestRepository, times(Category.values().length))
                .countByCategory(any(Category.class));
    }
}