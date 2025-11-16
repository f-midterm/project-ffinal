package apartment.example.backend.controller;

import apartment.example.backend.dto.MaintenanceRequestDTO;
import apartment.example.backend.entity.MaintenanceRequest;
import apartment.example.backend.entity.MaintenanceRequest.Priority;
import apartment.example.backend.entity.MaintenanceRequest.Category;
import apartment.example.backend.entity.MaintenanceRequest.RequestStatus;
import apartment.example.backend.entity.MaintenanceRequestItem;
import apartment.example.backend.entity.Unit;
import apartment.example.backend.entity.Tenant;
import apartment.example.backend.entity.User;
import apartment.example.backend.service.MaintenanceRequestService;
import apartment.example.backend.service.MaintenanceRequestItemService;
import apartment.example.backend.repository.UnitRepository;
import apartment.example.backend.repository.TenantRepository;
import apartment.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final MaintenanceRequestItemService itemService;
    private final UnitRepository unitRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    // Create a new maintenance request
    @PostMapping
    public ResponseEntity<MaintenanceRequest> createMaintenanceRequest(@RequestBody MaintenanceRequest request) {
        try {
            log.info("Creating maintenance request: title={}, category={}, priority={}, unitId={}, tenantId={}, createdByUserId={}", 
                    request.getTitle(), request.getCategory(), request.getPriority(), 
                    request.getUnitId(), request.getTenantId(), request.getCreatedByUserId());
            MaintenanceRequest createdRequest = maintenanceRequestService.createMaintenanceRequest(request);
            log.info("Successfully created maintenance request with ID: {}", createdRequest.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
        } catch (Exception e) {
            log.error("Error creating maintenance request: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Upload attachments for a maintenance request
     * POST /maintenance-requests/{id}/upload-attachments
     */
    @PostMapping("/{id}/upload-attachments")
    public ResponseEntity<?> uploadAttachments(
            @PathVariable Long id,
            @RequestParam("files") MultipartFile[] files) {
        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please select at least one file"));
            }

            // Validate total size (15MB)
            long totalSize = 0;
            for (MultipartFile file : files) {
                totalSize += file.getSize();
            }
            if (totalSize > 15 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "Total file size must not exceed 15MB"));
            }

            // Validate file types
            for (MultipartFile file : files) {
                String contentType = file.getContentType();
                if (contentType == null || 
                    (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
                    return ResponseEntity.badRequest().body(
                        Map.of("error", "Only image files and PDF are allowed"));
                }
            }

            MaintenanceRequest request = maintenanceRequestService.uploadAttachments(id, files);
            return ResponseEntity.ok(request);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
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

    // Get my maintenance requests (for logged-in user)
    @GetMapping("/my-requests")
    public ResponseEntity<List<MaintenanceRequestDTO>> getMyMaintenanceRequests(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String username = authentication.getName();
            log.info("Getting maintenance requests for user: {}", username);

            // Get user by username
            List<User> users = userRepository.findByUsername(username);
            if (users.isEmpty()) {
                log.warn("User not found: {}", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            User user = users.get(0);
            
            // Get requests by createdByUserId
            List<MaintenanceRequest> requests = maintenanceRequestService.getRequestsByCreatedByUserId(user.getId());

            // Convert to DTO with room and tenant info
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

            log.info("Found {} maintenance requests for user {}", dtos.size(), username);
            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            log.error("Error getting my maintenance requests", e);
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

    // ============================================
    // MAINTENANCE REQUEST ITEMS ENDPOINTS
    // ============================================

    /**
     * Get all items (stock used) for a maintenance request
     */
    @GetMapping("/{requestId}/items")
    public ResponseEntity<List<MaintenanceRequestItem>> getRequestItems(@PathVariable Long requestId) {
        log.info("GET /maintenance-requests/{}/items - Get items for request", requestId);
        List<MaintenanceRequestItem> items = itemService.getItemsByRequestId(requestId);
        return ResponseEntity.ok(items);
    }

    /**
     * Add items to a maintenance request
     */
    @PostMapping("/{requestId}/items")
    public ResponseEntity<List<MaintenanceRequestItem>> addItemsToRequest(
            @PathVariable Long requestId,
            @RequestBody List<MaintenanceRequestItem> items) {
        log.info("POST /maintenance-requests/{}/items - Adding {} items", requestId, items.size());
        try {
            List<MaintenanceRequestItem> addedItems = itemService.addItemsToRequest(requestId, items);
            return ResponseEntity.status(HttpStatus.CREATED).body(addedItems);
        } catch (IllegalArgumentException e) {
            log.error("Error adding items: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add a single item to a maintenance request
     */
    @PostMapping("/{requestId}/items/single")
    public ResponseEntity<MaintenanceRequestItem> addSingleItem(
            @PathVariable Long requestId,
            @RequestBody Map<String, Object> payload) {
        log.info("POST /maintenance-requests/{}/items/single", requestId);
        try {
            Long stockId = Long.valueOf(payload.get("stockId").toString());
            Integer quantity = Integer.valueOf(payload.get("quantity").toString());
            String notes = payload.get("notes") != null ? payload.get("notes").toString() : null;

            MaintenanceRequestItem item = itemService.addItemToRequest(requestId, stockId, quantity, notes);
            return ResponseEntity.status(HttpStatus.CREATED).body(item);
        } catch (IllegalArgumentException e) {
            log.error("Error adding item: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update item quantity
     */
    @PatchMapping("/items/{itemId}/quantity")
    public ResponseEntity<MaintenanceRequestItem> updateItemQuantity(
            @PathVariable Long itemId,
            @RequestBody Map<String, Integer> payload) {
        log.info("PATCH /maintenance-requests/items/{}/quantity", itemId);
        try {
            Integer newQuantity = payload.get("quantity");
            if (newQuantity == null || newQuantity <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            MaintenanceRequestItem updatedItem = itemService.updateItemQuantity(itemId, newQuantity);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            log.error("Error updating item quantity: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove an item from a request
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Map<String, String>> removeItem(@PathVariable Long itemId) {
        log.info("DELETE /maintenance-requests/items/{}", itemId);
        try {
            itemService.removeItem(itemId);
            return ResponseEntity.ok(Map.of("message", "Item removed successfully"));
        } catch (Exception e) {
            log.error("Error removing item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Calculate total cost for items in a request
     */
    @GetMapping("/{requestId}/items/cost")
    public ResponseEntity<Map<String, Object>> calculateItemsCost(@PathVariable Long requestId) {
        log.info("GET /maintenance-requests/{}/items/cost", requestId);
        try {
            java.math.BigDecimal totalCost = itemService.calculateTotalCost(requestId);
            return ResponseEntity.ok(Map.of(
                "requestId", requestId,
                "totalCost", totalCost,
                "currency", "THB"
            ));
        } catch (Exception e) {
            log.error("Error calculating cost: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}