package apartment.example.backend.repository;

import apartment.example.backend.entity.Lease;
import apartment.example.backend.entity.enums.LeaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaseRepository extends JpaRepository<Lease, Long> {
    
    List<Lease> findByStatus(LeaseStatus status);
    
    List<Lease> findByTenantId(Long tenantId);
    
    List<Lease> findByUnitId(Long unitId);
    
    Optional<Lease> findByUnitIdAndStatus(Long unitId, LeaseStatus status);
    
    @Query("SELECT l FROM Lease l WHERE l.endDate BETWEEN :startDate AND :endDate AND l.status = :status")
    List<Lease> findLeasesEndingBetween(@Param("startDate") LocalDate startDate, 
                                       @Param("endDate") LocalDate endDate, 
                                       @Param("status") LeaseStatus status);
    
    @Query("SELECT l FROM Lease l WHERE l.endDate < :date AND l.status = :status")
    List<Lease> findExpiredLeases(@Param("date") LocalDate date, @Param("status") LeaseStatus status);
    
    @Query("SELECT COUNT(l) > 0 FROM Lease l WHERE l.unit.id = :unitId AND l.status = 'ACTIVE' AND " +
           "((l.startDate <= :endDate AND l.endDate >= :startDate))")
    boolean existsOverlappingLease(@Param("unitId") Long unitId, 
                                  @Param("startDate") LocalDate startDate, 
                                  @Param("endDate") LocalDate endDate);
}