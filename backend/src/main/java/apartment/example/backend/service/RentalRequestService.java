package apartment.example.backend.service;

import apartment.example.backend.entity.*;
import apartment.example.backend.entity.enums.LeaseStatus;
import apartment.example.backend.entity.enums.UnitStatus;
import apartment.example.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RentalRequestService {

    @Autowired
    private RentalRequestRepository rentalRequestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private UnitRepository unitRepository;
    
    @Autowired
    private LeaseRepository leaseRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<RentalRequest> getAllRentalRequests() {
        return rentalRequestRepository.findAll();
    }

    public List<RentalRequest> getPendingRequests() {
        try {
            return rentalRequestRepository.findByStatus(RentalRequestStatus.PENDING);
        } catch (Exception e) {
            // Log the error and return empty list for now
            System.out.println("Error getting pending requests: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<RentalRequest> getRequestsByUnitId(Long unitId) {
        return rentalRequestRepository.findByUnitId(unitId);
    }

    public List<RentalRequest> getPendingRequestsByUnitId(Long unitId) {
        return rentalRequestRepository.findPendingRequestsByUnitId(unitId);
    }

    public List<RentalRequest> getRequestsByEmail(String email) {
        return rentalRequestRepository.findByEmail(email);
    }

    public Optional<RentalRequest> getRequestById(Long id) {
        return rentalRequestRepository.findById(id);
    }

    public RentalRequest createRentalRequest(RentalRequest rentalRequest) {
        // Check user role restrictions
        Optional<User> optionalUser = userRepository.findByEmail(rentalRequest.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            
            // VILLAGER users cannot make new bookings (one booking per person rule)
            if (user.getRole() == User.Role.VILLAGER) {
                throw new IllegalArgumentException("You are already an approved tenant and cannot submit new rental requests. You can access your dashboard to view your lease information.");
            }
            
            // Implement strict 1-to-1 rule: USER can only have ONE request at a time
            if (user.getRole() == User.Role.USER) {
                List<RentalRequest> existingRequests = rentalRequestRepository.findByEmail(rentalRequest.getEmail());
                
                // Check for any PENDING or APPROVED requests
                boolean hasActiveRequest = existingRequests.stream()
                    .anyMatch(req -> req.getStatus() == RentalRequestStatus.PENDING || 
                                   req.getStatus() == RentalRequestStatus.APPROVED);
                
                if (hasActiveRequest) {
                    throw new IllegalArgumentException("You already have an active rental request. Each person can only submit one request at a time. Please wait for admin decision or check your status.");
                }
                
                // Only allow new request if all previous requests were REJECTED
                boolean hasOnlyRejectedRequests = existingRequests.stream()
                    .allMatch(req -> req.getStatus() == RentalRequestStatus.REJECTED);
                
                if (!existingRequests.isEmpty() && !hasOnlyRejectedRequests) {
                    throw new IllegalArgumentException("You can only submit a new rental request if your previous request was rejected by the admin.");
                }
            }
        }
        
        // Check that the unit is available
        Optional<Unit> unitOptional = unitRepository.findById(rentalRequest.getUnitId());
        if (unitOptional.isPresent()) {
            Unit unit = unitOptional.get();
            if (unit.getStatus() != UnitStatus.AVAILABLE) {
                throw new IllegalArgumentException("Selected unit is not available for rent. Please choose an available unit.");
            }
        } else {
            throw new IllegalArgumentException("Selected unit does not exist.");
        }
        
        // Set default values
        rentalRequest.setStatus(RentalRequestStatus.PENDING);
        if (rentalRequest.getRequestDate() == null) {
            rentalRequest.setRequestDate(LocalDateTime.now());
        }
        
        return rentalRequestRepository.save(rentalRequest);
    }

    public RentalRequest approveRequest(Long id, Long approvedByUserId) {
        Optional<RentalRequest> optionalRequest = rentalRequestRepository.findById(id);
        if (optionalRequest.isPresent()) {
            RentalRequest request = optionalRequest.get();
            request.setStatus(RentalRequestStatus.APPROVED);
            request.setApprovedByUserId(approvedByUserId);
            request.setApprovedDate(LocalDateTime.now());
            request.setRejectionReason(null); // Clear rejection reason if previously rejected
            return rentalRequestRepository.save(request);
        }
        throw new RuntimeException("Rental request not found with id: " + id);
    }

    public RentalRequest approveRequest(Long id, Long approvedByUserId, LocalDate startDate, LocalDate endDate) {
        Optional<RentalRequest> optionalRequest = rentalRequestRepository.findById(id);
        if (optionalRequest.isPresent()) {
            RentalRequest request = optionalRequest.get();
            
            // Approve the rental request
            request.setStatus(RentalRequestStatus.APPROVED);
            request.setApprovedByUserId(approvedByUserId);
            request.setApprovedDate(LocalDateTime.now());
            request.setRejectionReason(null);
            
            // Create/Update Tenant
            Tenant tenant = createOrUpdateTenant(request);
            
            // Get Unit and update status
            Unit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new RuntimeException("Unit not found"));
            unit.setStatus(UnitStatus.OCCUPIED);
            unitRepository.save(unit);
            
            // Create Lease
            createLease(tenant, unit, request, startDate, endDate);
            
            // Update user role to VILLAGER
            updateUserRoleToVillager(request.getEmail());
            
            return rentalRequestRepository.save(request);
        }
        throw new RuntimeException("Rental request not found with id: " + id);
    }
    
    private Tenant createOrUpdateTenant(RentalRequest request) {
        Tenant tenant = new Tenant();
        tenant.setFirstName(request.getFirstName());
        tenant.setLastName(request.getLastName());
        tenant.setEmail(request.getEmail());
        tenant.setPhone(request.getPhone());
        tenant.setUnitId(request.getUnitId());
        tenant.setMoveInDate(LocalDate.now());
        tenant.setMonthlyRent(request.getMonthlyRent());
        return tenantRepository.save(tenant);
    }
    
    private void createLease(Tenant tenant, Unit unit, RentalRequest request, LocalDate startDate, LocalDate endDate) {
        Lease lease = new Lease();
        lease.setTenant(tenant);
        lease.setUnit(unit);
        lease.setStartDate(startDate);
        lease.setEndDate(endDate);
        lease.setRentAmount(request.getMonthlyRent());
        lease.setStatus(LeaseStatus.ACTIVE);
        leaseRepository.save(lease);
    }
    
    private void updateUserRoleToVillager(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            // Update existing user role to VILLAGER
            User user = optionalUser.get();
            user.setRole(User.Role.VILLAGER);
            userRepository.save(user);
        } else {
            // Create new user account with VILLAGER role for approved rental applicant
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(email); // Use email as username
            newUser.setPassword(passwordEncoder.encode("defaultPassword123")); // Set a default password
            newUser.setRole(User.Role.VILLAGER);
            userRepository.save(newUser);
        }
    }

    public RentalRequest rejectRequest(Long id, String rejectionReason, Long rejectedByUserId) {
        Optional<RentalRequest> optionalRequest = rentalRequestRepository.findById(id);
        if (optionalRequest.isPresent()) {
            RentalRequest request = optionalRequest.get();
            request.setStatus(RentalRequestStatus.REJECTED);
            request.setRejectionReason(rejectionReason);
            request.setApprovedByUserId(rejectedByUserId); // Track who rejected it
            request.setApprovedDate(LocalDateTime.now()); // Use this field for rejection date too
            return rentalRequestRepository.save(request);
        }
        throw new RuntimeException("Rental request not found with id: " + id);
    }

    public RentalRequest updateRentalRequest(Long id, RentalRequest updatedRequest) {
        Optional<RentalRequest> optionalRequest = rentalRequestRepository.findById(id);
        if (optionalRequest.isPresent()) {
            RentalRequest existingRequest = optionalRequest.get();
            
            // Update fields (excluding ID and system fields)
            existingRequest.setFirstName(updatedRequest.getFirstName());
            existingRequest.setLastName(updatedRequest.getLastName());
            existingRequest.setEmail(updatedRequest.getEmail());
            existingRequest.setPhone(updatedRequest.getPhone());
            existingRequest.setOccupation(updatedRequest.getOccupation());
            existingRequest.setEmergencyContact(updatedRequest.getEmergencyContact());
            existingRequest.setEmergencyPhone(updatedRequest.getEmergencyPhone());
            existingRequest.setLeaseDurationMonths(updatedRequest.getLeaseDurationMonths());
            existingRequest.setMonthlyRent(updatedRequest.getMonthlyRent());
            existingRequest.setTotalAmount(updatedRequest.getTotalAmount());
            existingRequest.setNotes(updatedRequest.getNotes());
            
            return rentalRequestRepository.save(existingRequest);
        }
        throw new RuntimeException("Rental request not found with id: " + id);
    }

    public void deleteRentalRequest(Long id) {
        if (rentalRequestRepository.existsById(id)) {
            rentalRequestRepository.deleteById(id);
        } else {
            throw new RuntimeException("Rental request not found with id: " + id);
        }
    }
}