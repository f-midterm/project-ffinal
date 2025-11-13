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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
     * Get invoice by ID
     */
    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
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
