package apartment.example.backend.repository;

import apartment.example.backend.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Invoice Repository
 * 
 * Handles database operations for Invoice entities
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * Find invoice by invoice number
     */
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    /**
     * Find all invoices for a specific lease
     */
    List<Invoice> findByLeaseId(Long leaseId);

    /**
     * Count invoices by invoice date (for generating unique invoice numbers)
     */
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.invoiceDate = :invoiceDate")
    long countByInvoiceDate(@Param("invoiceDate") LocalDate invoiceDate);

    /**
     * Find latest invoice number for a specific date (for generating next invoice number)
     */
    @Query("SELECT i FROM Invoice i WHERE i.invoiceDate = :invoiceDate ORDER BY i.createdAt DESC")
    List<Invoice> findByInvoiceDateOrderByCreatedAtDesc(@Param("invoiceDate") LocalDate invoiceDate);

    /**
     * Check if invoice number already exists
     */
    boolean existsByInvoiceNumber(String invoiceNumber);
}
