package apartment.example.backend.controller;

import apartment.example.backend.entity.Lease;
import apartment.example.backend.entity.enums.LeaseStatus;
import apartment.example.backend.service.LeaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leases")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class LeaseController {

    private final LeaseService leaseService;

    @GetMapping
    public ResponseEntity<List<Lease>> getAllLeases() {
        List<Lease> leases = leaseService.getAllLeases();
        return ResponseEntity.ok(leases);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<Lease>> getAllLeases(Pageable pageable) {
        Page<Lease> leases = leaseService.getAllLeases(pageable);
        return ResponseEntity.ok(leases);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lease> getLeaseById(@PathVariable Long id) {
        return leaseService.getLeaseById(id)
                .map(lease -> ResponseEntity.ok(lease))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Lease>> getLeasesByStatus(@PathVariable LeaseStatus status) {
        List<Lease> leases = leaseService.getLeasesByStatus(status);
        return ResponseEntity.ok(leases);
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<Lease>> getLeasesByTenant(@PathVariable Long tenantId) {
        List<Lease> leases = leaseService.getLeasesByTenant(tenantId);
        return ResponseEntity.ok(leases);
    }

    @GetMapping("/unit/{unitId}")
    public ResponseEntity<List<Lease>> getLeasesByUnit(@PathVariable Long unitId) {
        List<Lease> leases = leaseService.getLeasesByUnit(unitId);
        return ResponseEntity.ok(leases);
    }

    @GetMapping("/unit/{unitId}/active")
    public ResponseEntity<Lease> getActiveLeaseByUnit(@PathVariable Long unitId) {
        return leaseService.getActiveLeaseByUnit(unitId)
                .map(lease -> ResponseEntity.ok(lease))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ending-soon")
    public ResponseEntity<List<Lease>> getLeasesEndingSoon(@RequestParam(defaultValue = "30") int days) {
        List<Lease> leases = leaseService.getLeasesEndingSoon(days);
        return ResponseEntity.ok(leases);
    }

    @GetMapping("/expired")
    public ResponseEntity<List<Lease>> getExpiredLeases() {
        List<Lease> leases = leaseService.getExpiredLeases();
        return ResponseEntity.ok(leases);
    }

    @PostMapping
    public ResponseEntity<Lease> createLease(@RequestBody Lease lease) {
        try {
            Lease createdLease = leaseService.createLease(lease);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdLease);
        } catch (IllegalArgumentException e) {
            log.error("Error creating lease: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lease> updateLease(@PathVariable Long id, @RequestBody Lease lease) {
        try {
            Lease updatedLease = leaseService.updateLease(id, lease);
            return ResponseEntity.ok(updatedLease);
        } catch (IllegalArgumentException e) {
            log.error("Error updating lease: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error updating lease: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Lease> activateLease(@PathVariable Long id) {
        try {
            Lease activatedLease = leaseService.activateLease(id);
            return ResponseEntity.ok(activatedLease);
        } catch (IllegalArgumentException e) {
            log.error("Error activating lease: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error activating lease: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/terminate")
    public ResponseEntity<Lease> terminateLease(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String reason = request.getOrDefault("reason", "No reason provided");
            Lease terminatedLease = leaseService.terminateLease(id, reason);
            return ResponseEntity.ok(terminatedLease);
        } catch (RuntimeException e) {
            log.error("Error terminating lease: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/expire-leases")
    public ResponseEntity<String> expireLeases() {
        try {
            leaseService.expireLeases();
            return ResponseEntity.ok("Expired leases processed successfully");
        } catch (Exception e) {
            log.error("Error expiring leases: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error processing expired leases");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLease(@PathVariable Long id) {
        try {
            leaseService.deleteLease(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting lease: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> checkLeaseExists(@PathVariable Long id) {
        boolean exists = leaseService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}