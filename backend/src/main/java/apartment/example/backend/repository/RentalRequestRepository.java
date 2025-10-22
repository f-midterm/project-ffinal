package apartment.example.backend.repository;

import apartment.example.backend.entity.RentalRequest;
import apartment.example.backend.entity.RentalRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalRequestRepository extends JpaRepository<RentalRequest, Long> {
    
    List<RentalRequest> findByStatus(RentalRequestStatus status);
    
    List<RentalRequest> findByUnitId(Long unitId);
    
    @Query("SELECT r FROM RentalRequest r WHERE r.status = 'PENDING' ORDER BY r.requestDate ASC")
    List<RentalRequest> findPendingRequests();
    
    @Query("SELECT r FROM RentalRequest r WHERE r.unitId = :unitId AND r.status = 'PENDING'")
    List<RentalRequest> findPendingRequestsByUnitId(@Param("unitId") Long unitId);
    
    @Query("SELECT r FROM RentalRequest r WHERE r.email = :email")
    List<RentalRequest> findByEmail(@Param("email") String email);
}