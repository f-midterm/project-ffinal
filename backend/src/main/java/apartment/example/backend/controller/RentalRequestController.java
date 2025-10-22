package apartment.example.backend.controller;

import apartment.example.backend.dto.ApprovalRequest;
import apartment.example.backend.entity.RentalRequest;
import apartment.example.backend.service.RentalRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/rental-requests")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RentalRequestController {

    private final RentalRequestService rentalRequestService;

    @GetMapping
    public ResponseEntity<List<RentalRequest>> getAllRentalRequests() {
        try {
            List<RentalRequest> requests = rentalRequestService.getAllRentalRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<RentalRequest>> getPendingRequests() {
        try {
            System.out.println("=== Getting pending requests ===");
            List<RentalRequest> requests = rentalRequestService.getPendingRequests();
            System.out.println("=== Found " + requests.size() + " pending requests ===");
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            System.out.println("=== Error in getPendingRequests: " + e.getMessage() + " ===");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/unit/{unitId}")
    public ResponseEntity<List<RentalRequest>> getRequestsByUnitId(@PathVariable Long unitId) {
        try {
            List<RentalRequest> requests = rentalRequestService.getRequestsByUnitId(unitId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<RentalRequest>> getRequestsByUserEmail(@RequestParam String email) {
        try {
            List<RentalRequest> requests = rentalRequestService.getRequestsByEmail(email);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalRequest> getRequestById(@PathVariable Long id) {
        try {
            Optional<RentalRequest> request = rentalRequestService.getRequestById(id);
            return request.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<RentalRequest> createRentalRequest(@RequestBody RentalRequest rentalRequest) {
        try {
            RentalRequest savedRequest = rentalRequestService.createRentalRequest(rentalRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRequest);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/unit/{unitId}")
    public ResponseEntity<RentalRequest> createRentalRequestForUnit(
            @PathVariable Long unitId, 
            @RequestBody RentalRequest rentalRequest) {
        try {
            rentalRequest.setUnitId(unitId);
            RentalRequest savedRequest = rentalRequestService.createRentalRequest(rentalRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRequest);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<RentalRequest> approveRequest(
            @PathVariable Long id,
            @RequestBody ApprovalRequest approvalRequest) {
        try {
            RentalRequest approvedRequest = rentalRequestService.approveRequest(
                id, 
                approvalRequest.getApprovedByUserId(),
                approvalRequest.getStartDate(),
                approvalRequest.getEndDate()
            );
            return ResponseEntity.ok(approvedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<RentalRequest> rejectRequest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        try {
            String rejectionReason = (String) payload.get("reason");
            Long rejectedByUserId = null;
            if (payload.containsKey("rejectedByUserId")) {
                rejectedByUserId = Long.valueOf(payload.get("rejectedByUserId").toString());
            }
            
            RentalRequest rejectedRequest = rentalRequestService.rejectRequest(id, rejectionReason, rejectedByUserId);
            return ResponseEntity.ok(rejectedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RentalRequest> updateRentalRequest(
            @PathVariable Long id,
            @RequestBody RentalRequest rentalRequest) {
        try {
            RentalRequest updatedRequest = rentalRequestService.updateRentalRequest(id, rentalRequest);
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRentalRequest(@PathVariable Long id) {
        try {
            rentalRequestService.deleteRentalRequest(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}