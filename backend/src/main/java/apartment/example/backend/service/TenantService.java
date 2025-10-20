package apartment.example.backend.service;

import apartment.example.backend.entity.Tenant;
import apartment.example.backend.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TenantService {

    private final TenantRepository tenantRepository;

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Page<Tenant> getAllTenants(Pageable pageable) {
        return tenantRepository.findAll(pageable);
    }

    public Optional<Tenant> getTenantById(Long id) {
        return tenantRepository.findById(id);
    }

    public Optional<Tenant> getTenantByEmail(String email) {
        return tenantRepository.findByEmail(email);
    }

    public Optional<Tenant> findByEmail(String email) {
        return tenantRepository.findByEmail(email);
    }

    public List<Tenant> searchTenantsByName(String name) {
        return tenantRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Tenant> getTenantsByPhone(String phone) {
        return tenantRepository.findByPhoneContaining(phone);
    }

    public List<Tenant> getTenantsByEmail(String email) {
        return tenantRepository.findByEmailContainingIgnoreCase(email);
    }

    public List<Tenant> getTenantsByUnitId(Long unitId) {
        return tenantRepository.findByUnitId(unitId);
    }

    public Tenant createTenant(Tenant tenant) {
        log.info("Creating new tenant: {} {}", tenant.getFirstName(), tenant.getLastName());
        return tenantRepository.save(tenant);
    }

    public Tenant updateTenant(Long id, Tenant tenantDetails) {
        Tenant tenant = tenantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tenant not found with id: " + id));

        tenant.setFirstName(tenantDetails.getFirstName());
        tenant.setLastName(tenantDetails.getLastName());
        tenant.setPhone(tenantDetails.getPhone());
        tenant.setEmail(tenantDetails.getEmail());
        tenant.setEmergencyContact(tenantDetails.getEmergencyContact());
        tenant.setEmergencyPhone(tenantDetails.getEmergencyPhone());
        tenant.setOccupation(tenantDetails.getOccupation());
        tenant.setUnitId(tenantDetails.getUnitId());
        tenant.setMoveInDate(tenantDetails.getMoveInDate());
        tenant.setLeaseEndDate(tenantDetails.getLeaseEndDate());
        tenant.setMonthlyRent(tenantDetails.getMonthlyRent());
        tenant.setStatus(tenantDetails.getStatus());

        log.info("Updating tenant: {} {}", tenant.getFirstName(), tenant.getLastName());
        return tenantRepository.save(tenant);
    }

    public void deleteTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tenant not found with id: " + id));
        
        log.info("Deleting tenant: {} {}", tenant.getFirstName(), tenant.getLastName());
        tenantRepository.delete(tenant);
    }

    public boolean existsById(Long id) {
        return tenantRepository.existsById(id);
    }
}