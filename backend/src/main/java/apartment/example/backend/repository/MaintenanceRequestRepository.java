package apartment.example.backend.repository;

import apartment.example.backend.entity.MaintenanceRequest;
import apartment.example.backend.entity.MaintenanceRequest.Priority;
import apartment.example.backend.entity.MaintenanceRequest.Category;
import apartment.example.backend.entity.MaintenanceRequest.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {
    
    // Find by tenant ID
    List<MaintenanceRequest> findByTenantId(Long tenantId);
    
    // Find by unit ID
    List<MaintenanceRequest> findByUnitId(Long unitId);
    
    // Find by status
    List<MaintenanceRequest> findByStatus(RequestStatus status);
    
    // Find by priority
    List<MaintenanceRequest> findByPriority(Priority priority);
    
    // Find by category
    List<MaintenanceRequest> findByCategory(Category category);
    
    // Find open requests (not completed or cancelled)
    @Query("SELECT m FROM MaintenanceRequest m WHERE m.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<MaintenanceRequest> findOpenRequests();
    
    // Find high priority requests
    List<MaintenanceRequest> findByPriorityOrderBySubmittedDateDesc(Priority priority);
    
    // Count by status
    long countByStatus(RequestStatus status);
    
    // Count by priority
    long countByPriority(Priority priority);
    
    // Count by category
    long countByCategory(Category category);
}