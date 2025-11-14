package apartment.example.backend.service;

import apartment.example.backend.entity.Invoice;
import apartment.example.backend.entity.Lease;
import apartment.example.backend.entity.Payment;
import apartment.example.backend.entity.enums.PaymentStatus;
import apartment.example.backend.entity.enums.PaymentType;
import apartment.example.backend.repository.InvoiceRepository;
import apartment.example.backend.repository.LeaseRepository;
import apartment.example.backend.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Invoice Service
 * 
 * Business logic for creating and managing invoices with payment line items
 */
@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private LeaseRepository leaseRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;
    
    @Value("${file.upload-dir:uploads/payment-slips}")
    private String uploadDir;

    /**
     * Create an invoice with multiple payment line items
     * 
     * @param leaseId Lease ID
     * @param invoiceDate Invoice date
     * @param dueDate Due date
     * @param paymentItems List of payment items (type and amount)
     * @param notes Optional notes
     * @return Created invoice with payments
     */
    @Transactional
    public Invoice createInvoiceWithPayments(
            Long leaseId, 
            LocalDate invoiceDate, 
            LocalDate dueDate,
            List<PaymentItem> paymentItems,
            String notes) {
        
        // Validate lease exists
        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new RuntimeException("Lease not found with id: " + leaseId));

        // Calculate total amount
        BigDecimal totalAmount = paymentItems.stream()
                .map(PaymentItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Generate invoice number: INV-YYYYMMDD-XXX
        String invoiceNumber = generateInvoiceNumber(invoiceDate);

        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setLease(lease);
        invoice.setInvoiceDate(invoiceDate);
        invoice.setDueDate(dueDate);
        invoice.setTotalAmount(totalAmount);
        invoice.setStatus(Invoice.InvoiceStatus.PENDING);
        invoice.setNotes(notes);

        // Save invoice first to get ID
        invoice = invoiceRepository.save(invoice);

        // Create payment line items
        for (PaymentItem item : paymentItems) {
            Payment payment = new Payment();
            payment.setInvoice(invoice);
            payment.setLease(lease);
            payment.setPaymentType(item.getPaymentType());
            payment.setAmount(item.getAmount());
            payment.setDueDate(dueDate);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setNotes(item.getDescription());
            
            // Generate receipt number for each payment (RENT-XXX, ELEC-XXX, etc.)
            String receiptNumber = paymentService.generateReceiptNumber(item.getPaymentType());
            payment.setReceiptNumber(receiptNumber);

            invoice.addPayment(payment);
        }

        // Save invoice with payments
        return invoiceRepository.save(invoice);
    }

    /**
     * Generate unique invoice number in format: INV-YYYYMMDD-XXX
     * 
     * Example: INV-20251113-1, INV-20251113-2, etc.
     */
    private String generateInvoiceNumber(LocalDate invoiceDate) {
        // Format date as YYYYMMDD
        String dateStr = invoiceDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // Get count of invoices for this date
        long count = invoiceRepository.countByInvoiceDate(invoiceDate);
        
        // Generate sequential number
        long sequenceNumber = count + 1;
        
        // Format: INV-YYYYMMDD-XXX
        String invoiceNumber = String.format("INV-%s-%d", dateStr, sequenceNumber);
        
        // Check if exists (race condition protection)
        while (invoiceRepository.existsByInvoiceNumber(invoiceNumber)) {
            sequenceNumber++;
            invoiceNumber = String.format("INV-%s-%d", dateStr, sequenceNumber);
        }
        
        return invoiceNumber;
    }

    /**
     * Get invoice by ID with all details
     */
    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
    }

    /**
     * Get invoice by invoice number
     */
    public Invoice getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found with number: " + invoiceNumber));
    }

    /**
     * Get all invoices for a lease
     */
    public List<Invoice> getInvoicesByLeaseId(Long leaseId) {
        return invoiceRepository.findByLeaseId(leaseId);
    }

    /**
     * Get all invoices for a tenant by email
     */
    public List<Invoice> getInvoicesByTenantEmail(String tenantEmail) {
        return invoiceRepository.findByTenantEmail(tenantEmail);
    }
    
    /**
     * Upload payment slip for an invoice
     */
    @Transactional
    public Invoice uploadPaymentSlip(Long invoiceId, MultipartFile slipFile) {
        Invoice invoice = getInvoiceById(invoiceId);
        
        // Validate invoice status
        if (invoice.getStatus() != Invoice.InvoiceStatus.PENDING && 
            invoice.getStatus() != Invoice.InvoiceStatus.REJECTED) {
            throw new RuntimeException("Invoice is not in a state that accepts payment slip uploads");
        }
        
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = slipFile.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
            String filename = String.format("slip_%s_%s%s", 
                invoiceId, 
                UUID.randomUUID().toString(), 
                extension);
            
            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(slipFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Update invoice
            invoice.setSlipUrl("/uploads/payment-slips/" + filename);
            invoice.setSlipUploadedAt(LocalDateTime.now());
            invoice.setStatus(Invoice.InvoiceStatus.WAITING_VERIFICATION);
            
            return invoiceRepository.save(invoice);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload payment slip: " + e.getMessage());
        }
    }
    
    /**
     * Verify payment slip (Admin only)
     */
    @Transactional
    public Invoice verifyPayment(Long invoiceId, boolean approved, String notes) {
        Invoice invoice = getInvoiceById(invoiceId);
        
        // Validate invoice status
        if (invoice.getStatus() != Invoice.InvoiceStatus.WAITING_VERIFICATION) {
            throw new RuntimeException("Invoice is not waiting for verification");
        }
        
        if (approved) {
            // Approve payment
            invoice.setStatus(Invoice.InvoiceStatus.PAID);
            invoice.setVerifiedAt(LocalDateTime.now());
            invoice.setVerificationNotes(notes);
            
            // Update all payment line items to PAID
            for (Payment payment : invoice.getPayments()) {
                payment.setStatus(PaymentStatus.PAID);
                payment.setPaidDate(LocalDate.now());
            }
        } else {
            // Reject payment
            invoice.setStatus(Invoice.InvoiceStatus.REJECTED);
            invoice.setVerifiedAt(LocalDateTime.now());
            invoice.setVerificationNotes(notes);
        }
        
        return invoiceRepository.save(invoice);
    }
    
    /**
     * Get all invoices waiting for verification
     */
    public List<Invoice> getInvoicesWaitingVerification() {
        return invoiceRepository.findByStatus(Invoice.InvoiceStatus.WAITING_VERIFICATION);
    }

    /**
     * Payment Item DTO
     */
    public static class PaymentItem {
        private PaymentType paymentType;
        private BigDecimal amount;
        private String description;

        public PaymentItem() {
        }

        public PaymentItem(PaymentType paymentType, BigDecimal amount, String description) {
            this.paymentType = paymentType;
            this.amount = amount;
            this.description = description;
        }

        public PaymentType getPaymentType() {
            return paymentType;
        }

        public void setPaymentType(PaymentType paymentType) {
            this.paymentType = paymentType;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
