package apartment.example.backend.service;

import apartment.example.backend.entity.Lease;
import apartment.example.backend.entity.Unit;
import apartment.example.backend.entity.enums.LeaseStatus;
import apartment.example.backend.entity.enums.UnitStatus;
import apartment.example.backend.repository.LeaseRepository;
import apartment.example.backend.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        // Removed notes field since it doesn't exist in database
        // lease.setNotes(lease.getNotes() + "\nTermination reason: " + reason);

        // Update unit status to available
        Unit unit = lease.getUnit();
        unit.setStatus(UnitStatus.AVAILABLE);
        unitRepository.save(unit);

        log.info("Terminated lease for unit: {} with reason: {}", unit.getRoomNumber(), reason);
        return leaseRepository.save(lease);
    }

    @Transactional
    public Lease terminateLease(Long leaseId) {
        return terminateLease(leaseId, "Terminated by system");
    }

    @Transactional
    public void expireLeases() {
        List<Lease> expiredLeases = getExpiredLeases();
        for (Lease lease : expiredLeases) {
            lease.setStatus(LeaseStatus.EXPIRED);
            
            // Update unit status to available
            Unit unit = lease.getUnit();
            unit.setStatus(UnitStatus.AVAILABLE);
            unitRepository.save(unit);
            
            log.info("Expired lease for unit: {}", unit.getRoomNumber());
        }
        leaseRepository.saveAll(expiredLeases);
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