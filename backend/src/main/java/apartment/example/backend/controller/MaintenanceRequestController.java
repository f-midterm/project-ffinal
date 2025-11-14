package apartment.example.backend.controller;

import apartment.example.backend.dto.MaintenanceRequestDTO;
import apartment.example.backend.entity.MaintenanceRequest;
import apartment.example.backend.entity.MaintenanceRequest.Priority;
import apartment.example.backend.entity.MaintenanceRequest.Category;
import apartment.example.backend.entity.MaintenanceRequest.RequestStatus;
import apartment.example.backend.entity.Unit;
import apartment.example.backend.entity.Tenant;
import apartment.example.backend.service.MaintenanceRequestService;
import apartment.example.backend.repository.UnitRepository;
import apartment.example.backend.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/maintenance-requests")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MaintenanceRequestController {

    private final MaintenanceRequestService maintenanceRequestService;
    private final UnitRepository unitRepository;
    private final TenantRepository tenantRepository;

    // Create a new maintenance request
    @PostMapping
    public ResponseEntity<MaintenanceRequest> createMaintenanceRequest(@RequestBody MaintenanceRequest request) {
        try {
            MaintenanceRequest createdRequest = maintenanceRequestService.createMaintenanceRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Test endpoint to verify controller is loaded
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        log.info("MaintenanceRequestController test endpoint called");
        return ResponseEntity.ok("MaintenanceRequestController is working!");
    }

    // Get all maintenance requests with room and tenant info
    @GetMapping
    public ResponseEntity<List<MaintenanceRequestDTO>> getAllMaintenanceRequests() {
        log.info("Getting all maintenance requests");
        try {
            List<MaintenanceRequest> requests = maintenanceRequestService.getAllMaintenanceRequests();
            List<MaintenanceRequestDTO> dtos = requests.stream().map(request -> {
                String roomNumber = "N/A";
                String tenantName = "N/A";
                String unitType = "";
                
                if (request.getUnitId() != null) {
                    Optional<Unit> unit = unitRepository.findById(request.getUnitId());
                    if (unit.isPresent()) {
                        roomNumber = unit.get().getRoomNumber();
                        unitType = unit.get().getUnitType();
                    }
                }
                
                if (request.getTenantId() != null) {
                    Optional<Tenant> tenant = tenantRepository.findById(request.getTenantId());
                    if (tenant.isPresent()) {
                        tenantName = tenant.get().getFirstName() + " " + tenant.get().getLastName();
                    }
                }
                
                return MaintenanceRequestDTO.fromEntity(request, roomNumber, tenantName, unitType);
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error getting maintenance requests: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get maintenance request by ID
    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceRequest> getMaintenanceRequestById(@PathVariable Long id) {
        Optional<MaintenanceRequest> request = maintenanceRequestService.getMaintenanceRequestById(id);
        return request.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    // Get requests by tenant ID
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<MaintenanceRequest>> getRequestsByTenantId(@PathVariable Long tenantId) {
        List<MaintenanceRequest> requests = maintenanceRequestService.getRequestsByTenantId(tenantId);
        return ResponseEntity.ok(requests);
    }

    // Get requests by unit ID
    @GetMapping("/unit/{unitId}")
    public ResponseEntity<List<MaintenanceRequest>> getRequestsByUnitId(@PathVariable Long unitId) {
        List<MaintenanceRequest> requests = maintenanceRequestService.getRequestsByUnitId(unitId);
        return ResponseEntity.ok(requests);
    }

    // Get requests by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<MaintenanceRequest>> getRequestsByStatus(@PathVariable RequestStatus status) {
        List<MaintenanceRequest> requests = maintenanceRequestService.getRequestsByStatus(status);
        return ResponseEntity.ok(requests);
    }

    // Get requests by priority
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<MaintenanceRequest>> getRequestsByPriority(@PathVariable Priority priority) {
        List<MaintenanceRequest> requests = maintenanceRequestService.getRequestsByPriority(priority);
        return ResponseEntity.ok(requests);
    }

    // Get requests by category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<MaintenanceRequest>> getRequestsByCategory(@PathVariable Category category) {
        List<MaintenanceRequest> requests = maintenanceRequestService.getRequestsByCategory(category);
        return ResponseEntity.ok(requests);
    }

    // Get open requests
    @GetMapping("/open")
    public ResponseEntity<List<MaintenanceRequest>> getOpenRequests() {
        List<MaintenanceRequest> requests = maintenanceRequestService.getOpenRequests();
        return ResponseEntity.ok(requests);
    }

    // Get high priority requests
    @GetMapping("/high-priority")
    public ResponseEntity<List<MaintenanceRequest>> getHighPriorityRequests() {
        List<MaintenanceRequest> requests = maintenanceRequestService.getHighPriorityRequests();
        return ResponseEntity.ok(requests);
    }

    // Update maintenance request
    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceRequest> updateMaintenanceRequest(
            @PathVariable Long id, 
            @RequestBody MaintenanceRequest updatedRequest) {
        try {
            MaintenanceRequest updated = maintenanceRequestService.updateMaintenanceRequest(id, updatedRequest);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Assign maintenance request to a user
    @PutMapping("/{id}/assign")
    public ResponseEntity<MaintenanceRequest> assignMaintenanceRequest(
            @PathVariable Long id, 
            @RequestBody Map<String, Long> requestBody) {
        try {
            Long assignedToUserId = requestBody.get("assignedToUserId");
            MaintenanceRequest assigned = maintenanceRequestService.assignMaintenanceRequest(id, assignedToUserId);
            return ResponseEntity.ok(assigned);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Update status of maintenance request
    @PutMapping("/{id}/status")
    public ResponseEntity<MaintenanceRequest> updateRequestStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, String> requestBody) {
        try {
            RequestStatus status = RequestStatus.valueOf(requestBody.get("status"));
            String notes = requestBody.get("notes");
            MaintenanceRequest updated = maintenanceRequestService.updateRequestStatus(id, status, notes);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Update priority of maintenance request
    @PutMapping("/{id}/priority")
    public ResponseEntity<MaintenanceRequest> updateRequestPriority(
            @PathVariable Long id, 
            @RequestBody Map<String, String> requestBody) {
        try {
            Priority priority = Priority.valueOf(requestBody.get("priority"));
            MaintenanceRequest updated = maintenanceRequestService.updateRequestPriority(id, priority);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Complete maintenance request
    @PutMapping("/{id}/complete")
    public ResponseEntity<MaintenanceRequest> completeMaintenanceRequest(
            @PathVariable Long id, 
            @RequestBody Map<String, String> requestBody) {
        try {
            String completionNotes = requestBody.get("completionNotes");
            MaintenanceRequest completed = maintenanceRequestService.completeMaintenanceRequest(id, completionNotes);
            return ResponseEntity.ok(completed);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Reject maintenance request
    @PutMapping("/{id}/reject")
    public ResponseEntity<MaintenanceRequest> rejectMaintenanceRequest(
            @PathVariable Long id, 
            @RequestBody Map<String, String> requestBody) {
        try {
            String rejectionReason = requestBody.get("rejectionReason");
            MaintenanceRequest rejected = maintenanceRequestService.rejectMaintenanceRequest(id, rejectionReason);
            return ResponseEntity.ok(rejected);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete maintenance request
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaintenanceRequest(@PathVariable Long id) {
        try {
            maintenanceRequestService.deleteMaintenanceRequest(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get maintenance request statistics
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getMaintenanceRequestStats() {
        Map<String, Long> stats = maintenanceRequestService.getMaintenanceRequestStats();
        return ResponseEntity.ok(stats);
    }

    // Get statistics by priority
    @GetMapping("/stats/priority")
    public ResponseEntity<Map<String, Long>> getStatsByPriority() {
        Map<String, Long> stats = maintenanceRequestService.getStatsByPriority();
        return ResponseEntity.ok(stats);
    }

    // Get statistics by category
    @GetMapping("/stats/category")
    public ResponseEntity<Map<String, Long>> getStatsByCategory() {
        Map<String, Long> stats = maintenanceRequestService.getStatsByCategory();
        return ResponseEntity.ok(stats);
    }
}