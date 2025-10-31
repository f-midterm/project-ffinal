package apartment.example.backend.service;

import apartment.example.backend.entity.Lease;
import apartment.example.backend.entity.Tenant;
import apartment.example.backend.entity.Unit;
import apartment.example.backend.entity.User;
import apartment.example.backend.entity.enums.LeaseStatus;
import apartment.example.backend.entity.enums.TenantStatus;
import apartment.example.backend.entity.enums.UnitStatus;
import apartment.example.backend.repository.LeaseRepository;
import apartment.example.backend.repository.TenantRepository;
import apartment.example.backend.repository.UnitRepository;
import apartment.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeaseService {

    private final LeaseRepository leaseRepository;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    public List<Lease> getAllLeases() {
        return leaseRepository.findAll();
    }

    public Page<Lease> getAllLeases(Pageable pageable) {
        return leaseRepository.findAll(pageable);
    }

    public Optional<Lease> getLeaseById(Long id) {
        return leaseRepository.findById(id);
    }

    public List<Lease> getLeasesByStatus(LeaseStatus status) {
        return leaseRepository.findByStatus(status);
    }

    public List<Lease> getLeasesByTenant(Long tenantId) {
        return leaseRepository.findByTenantId(tenantId);
    }

    public List<Lease> getLeasesByUnit(Long unitId) {
        return leaseRepository.findByUnitId(unitId);
    }

    public List<Lease> getActiveLeases() {
        return leaseRepository.findByStatus(LeaseStatus.ACTIVE);
    }

    public Optional<Lease> getLeaseByUnitId(Long unitId) {
        // Since Unit has One-to-One relationship with Lease, get the active lease
        return leaseRepository.findByUnitIdAndStatus(unitId, LeaseStatus.ACTIVE);
    }

    public Optional<Lease> getActiveLeaseByUnit(Long unitId) {
        return leaseRepository.findByUnitIdAndStatus(unitId, LeaseStatus.ACTIVE);
    }

    public List<Lease> getLeasesEndingSoon(int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);
        return leaseRepository.findLeasesEndingBetween(startDate, endDate, LeaseStatus.ACTIVE);
    }

    public List<Lease> getExpiredLeases() {
        return leaseRepository.findExpiredLeases(LocalDate.now(), LeaseStatus.ACTIVE);
    }

    @Transactional
    public Lease createLease(Lease lease) {
        // Validate that the unit exists
        Unit unit = unitRepository.findById(lease.getUnit().getId())
            .orElseThrow(() -> new RuntimeException("Unit not found"));
        
        // Validate that the unit has AVAILABLE status
        if (unit.getStatus() != UnitStatus.AVAILABLE) {
            throw new IllegalArgumentException("Unit is not available for lease. Current status: " + unit.getStatus());
        }

        // Check for overlapping leases
        if (hasOverlappingLease(lease.getUnit().getId(), 
                               lease.getStartDate(), 
                               lease.getEndDate())) {
            throw new IllegalArgumentException("Overlapping lease exists for this unit during the specified period");
        }

        // Set lease status to ACTIVE and update unit status to OCCUPIED
        lease.setStatus(LeaseStatus.ACTIVE);
        unit.setStatus(UnitStatus.OCCUPIED);
        unitRepository.save(unit);

        log.info("Creating new lease for unit: {}", unit.getRoomNumber());
        return leaseRepository.save(lease);
    }

    @Transactional
    public Lease activateLease(Long leaseId) {
        Lease lease = leaseRepository.findById(leaseId)
            .orElseThrow(() -> new RuntimeException("Lease not found with id: " + leaseId));

        if (lease.getStatus() != LeaseStatus.PENDING) {
            throw new IllegalArgumentException("Only pending leases can be activated");
        }

        // Check for overlapping active leases
        if (hasOverlappingLease(lease.getUnit().getId(), 
                               lease.getLeaseStartDate(), 
                               lease.getLeaseEndDate())) {
            throw new IllegalArgumentException("Cannot activate lease due to overlapping active lease");
        }

        lease.setStatus(LeaseStatus.ACTIVE);
        
        // Update unit status
        Unit unit = lease.getUnit();
        unit.setStatus(UnitStatus.OCCUPIED);
        unitRepository.save(unit);

        log.info("Activated lease for unit: {}", unit.getRoomNumber());
        return leaseRepository.save(lease);
    }

    @Transactional
    public Lease terminateLease(Long leaseId, String reason) {
        Lease lease = leaseRepository.findById(leaseId)
            .orElseThrow(() -> new RuntimeException("Lease not found with id: " + leaseId));

        lease.setStatus(LeaseStatus.TERMINATED);
        Lease savedLease = leaseRepository.save(lease);
        
        log.info("Terminated lease #{} with reason: {}", leaseId, reason);
        
        // Perform immediate cleanup
        performLeaseCleanup(lease, "TERMINATED");
        
        return savedLease;
    }

    @Transactional
    public Lease terminateLease(Long leaseId) {
        return terminateLease(leaseId, "Terminated by system");
    }
    
    /**
     * Shared cleanup logic for EXPIRED and TERMINATED leases
     * Handles: unit→AVAILABLE, user VILLAGER→USER (with safety check), tenant→INACTIVE
     * 
     * @param lease The lease to cleanup
     * @param source The source of cleanup (EXPIRED, TERMINATED, etc.)
     */
    @Transactional
    protected void performLeaseCleanup(Lease lease, String source) {
        Unit unit = lease.getUnit();
        Tenant tenant = lease.getTenant();
        
        // 1. Update unit to AVAILABLE
        unit.setStatus(UnitStatus.AVAILABLE);
        unitRepository.save(unit);
        log.info("[{}] Unit {} now AVAILABLE", source, unit.getRoomNumber());
        
        // 2. Check for other active leases (safety check for multi-unit tenants)
        long activeLeaseCount = leaseRepository.countByTenantIdAndStatus(tenant.getId(), LeaseStatus.ACTIVE);
        
        // 3. Only downgrade and deactivate if no other active leases
        if (activeLeaseCount == 0) {
            // Downgrade user role VILLAGER → USER
            Optional<User> userOptional = userRepository.findByEmail(tenant.getEmail());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (user.getRole() == User.Role.VILLAGER) {
                    user.setRole(User.Role.USER);
                    userRepository.save(user);
                    log.info("[{}] Downgraded user {} from VILLAGER to USER (can book again!)", source, user.getEmail());
                }
            } else {
                log.warn("[{}] User not found for tenant email: {}", source, tenant.getEmail());
            }
            
            // Set tenant to INACTIVE
            tenant.setStatus(TenantStatus.INACTIVE);
            tenantRepository.save(tenant);
            log.info("[{}] Tenant {} {} now INACTIVE", source, tenant.getFirstName(), tenant.getLastName());
        } else {
            log.info("[{}] Tenant {} still has {} active lease(s), keeping VILLAGER role", 
                source, tenant.getEmail(), activeLeaseCount);
        }
    }

    @Scheduled(cron = "0 0 2 * * *")  // Run daily at 2 AM
    @Transactional
    public void expireLeases() {
        List<Lease> expiredLeases = getExpiredLeases();
        log.info("Starting lease expiration job, found {} lease(s) to process", expiredLeases.size());
        
        for (Lease lease : expiredLeases) {
            lease.setStatus(LeaseStatus.EXPIRED);
            leaseRepository.save(lease);
            
            log.info("Expired lease #{} for unit {} - Tenant: {} {}, Email: {}", 
                    lease.getId(), lease.getUnit().getRoomNumber(), 
                    lease.getTenant().getFirstName(), lease.getTenant().getLastName(), 
                    lease.getTenant().getEmail());
            
            // Use shared cleanup logic
            performLeaseCleanup(lease, "AUTO_EXPIRED");
        }
        
        log.info("Lease expiration job completed, processed {} lease(s)", expiredLeases.size());
    }

    public Lease updateLease(Long id, Lease leaseDetails) {
        Lease lease = leaseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lease not found with id: " + id));

        // Validate if dates are being changed for active lease
        if (lease.getStatus() == LeaseStatus.ACTIVE && 
            (!lease.getLeaseStartDate().equals(leaseDetails.getLeaseStartDate()) ||
             !lease.getLeaseEndDate().equals(leaseDetails.getLeaseEndDate()))) {
            
            // Check for overlapping leases with new dates
            if (hasOverlappingLease(lease.getUnit().getId(), 
                                   leaseDetails.getLeaseStartDate(), 
                                   leaseDetails.getLeaseEndDate())) {
                throw new IllegalArgumentException("Cannot update lease dates due to overlapping lease");
            }
        }

        lease.setLeaseStartDate(leaseDetails.getLeaseStartDate());
        lease.setLeaseEndDate(leaseDetails.getLeaseEndDate());
        lease.setRentAmount(leaseDetails.getRentAmount());
        lease.setSecurityDeposit(leaseDetails.getSecurityDeposit());
        // Removed billingCycle, contractFilePath, and notes as they don't exist in database
        // lease.setBillingCycle(leaseDetails.getBillingCycle());
        // lease.setContractFilePath(leaseDetails.getContractFilePath());
        // lease.setNotes(leaseDetails.getNotes());

        log.info("Updating lease for unit: {}", lease.getUnit().getRoomNumber());
        return leaseRepository.save(lease);
    }

    public void deleteLease(Long id) {
        Lease lease = leaseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lease not found with id: " + id));
        
        // If active lease, update unit status
        if (lease.getStatus() == LeaseStatus.ACTIVE) {
            Unit unit = lease.getUnit();
            unit.setStatus(UnitStatus.AVAILABLE);
            unitRepository.save(unit);
        }
        
        log.info("Deleting lease for unit: {}", lease.getUnit().getRoomNumber());
        leaseRepository.delete(lease);
    }

    private boolean hasOverlappingLease(Long unitId, LocalDate startDate, LocalDate endDate) {
        return leaseRepository.existsOverlappingLease(unitId, startDate, endDate);
    }

    public boolean existsById(Long id) {
        return leaseRepository.existsById(id);
    }
}