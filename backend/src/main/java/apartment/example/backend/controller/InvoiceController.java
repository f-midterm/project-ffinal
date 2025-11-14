package apartment.example.backend.controller;

import apartment.example.backend.entity.Invoice;
import apartment.example.backend.entity.enums.PaymentType;
import apartment.example.backend.service.InvoiceService;
import apartment.example.backend.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Invoice Controller
 * 
 * REST API endpoints for invoice management
 */
@RestController
@RequestMapping("/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;
    
    @Autowired
    private PdfService pdfService;

    /**
     * Create a new invoice with payment line items
     * 
     * POST /invoices/create
     * 
     * Request Body:
     * {
     *   "leaseId": 1,
     *   "invoiceDate": "2025-11-13",
     *   "dueDate": "2025-11-28",
     *   "rentAmount": 5000,
     *   "electricityAmount": 800,
     *   "waterAmount": 200,
     *   "notes": "ค่าเช่าประจำเดือน พฤศจิกายน 2025"
     * }
     * 
     * Response: Invoice object with invoice number and payment line items
     */
    @PostMapping("/create")
    public ResponseEntity<Invoice> createInvoice(@RequestBody CreateInvoiceRequest request) {
        try {
            // Build payment items list
            List<InvoiceService.PaymentItem> paymentItems = new ArrayList<>();

            if (request.getRentAmount() != null && request.getRentAmount().compareTo(BigDecimal.ZERO) > 0) {
                paymentItems.add(new InvoiceService.PaymentItem(
                    PaymentType.RENT,
                    request.getRentAmount(),
                    "ค่าเช่า"
                ));
            }

            if (request.getElectricityAmount() != null && request.getElectricityAmount().compareTo(BigDecimal.ZERO) > 0) {
                paymentItems.add(new InvoiceService.PaymentItem(
                    PaymentType.ELECTRICITY,
                    request.getElectricityAmount(),
                    "ค่าไฟฟ้า"
                ));
            }

            if (request.getWaterAmount() != null && request.getWaterAmount().compareTo(BigDecimal.ZERO) > 0) {
                paymentItems.add(new InvoiceService.PaymentItem(
                    PaymentType.WATER,
                    request.getWaterAmount(),
                    "ค่าน้ำ"
                ));
            }

            if (paymentItems.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Create invoice with payments
            Invoice invoice = invoiceService.createInvoiceWithPayments(
                request.getLeaseId(),
                request.getInvoiceDate(),
                request.getDueDate(),
                paymentItems,
                request.getNotes()
            );

            return ResponseEntity.ok(invoice);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Get invoice by ID
     * 
     * GET /invoices/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        try {
            Invoice invoice = invoiceService.getInvoiceById(id);
            return ResponseEntity.ok(invoice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get invoice by invoice number
     * 
     * GET /invoices/number/{invoiceNumber}
     */
    @GetMapping("/number/{invoiceNumber}")
    public ResponseEntity<Invoice> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        try {
            Invoice invoice = invoiceService.getInvoiceByNumber(invoiceNumber);
            return ResponseEntity.ok(invoice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all invoices for a lease
     * 
     * GET /invoices/lease/{leaseId}
     */
    @GetMapping("/lease/{leaseId}")
    public ResponseEntity<List<Invoice>> getInvoicesByLeaseId(@PathVariable Long leaseId) {
        List<Invoice> invoices = invoiceService.getInvoicesByLeaseId(leaseId);
        return ResponseEntity.ok(invoices);
    }

    /**
     * Get all invoices for a tenant by email
     * 
     * GET /invoices/tenant/{tenantEmail}
     */
    @GetMapping("/tenant/{tenantEmail}")
    public ResponseEntity<List<Invoice>> getInvoicesByTenantEmail(@PathVariable String tenantEmail) {
        try {
            List<Invoice> invoices = invoiceService.getInvoicesByTenantEmail(tenantEmail);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Download Invoice PDF
     * 
     * GET /invoices/{id}/pdf
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        try {
            Invoice invoice = invoiceService.getInvoiceById(id);
            
            byte[] pdfBytes = pdfService.generateInvoicePdf(invoice);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", invoice.getInvoiceNumber() + ".pdf");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create Invoice Request DTO
     */
    public static class CreateInvoiceRequest {
        private Long leaseId;
        private LocalDate invoiceDate;
        private LocalDate dueDate;
        private BigDecimal rentAmount;
        private BigDecimal electricityAmount;
        private BigDecimal waterAmount;
        private String notes;

        // Getters and Setters
        public Long getLeaseId() {
            return leaseId;
        }

        public void setLeaseId(Long leaseId) {
            this.leaseId = leaseId;
        }

        public LocalDate getInvoiceDate() {
            return invoiceDate;
        }

        public void setInvoiceDate(LocalDate invoiceDate) {
            this.invoiceDate = invoiceDate;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
        }

        public BigDecimal getRentAmount() {
            return rentAmount;
        }

        public void setRentAmount(BigDecimal rentAmount) {
            this.rentAmount = rentAmount;
        }

        public BigDecimal getElectricityAmount() {
            return electricityAmount;
        }

        public void setElectricityAmount(BigDecimal electricityAmount) {
            this.electricityAmount = electricityAmount;
        }

        public BigDecimal getWaterAmount() {
            return waterAmount;
        }

        public void setWaterAmount(BigDecimal waterAmount) {
            this.waterAmount = waterAmount;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
    
    /**
     * Upload payment slip for an invoice
     * 
     * POST /invoices/{id}/upload-slip
     */
    @PostMapping("/{id}/upload-slip")
    public ResponseEntity<?> uploadPaymentSlip(
            @PathVariable Long id,
            @RequestParam("slip") MultipartFile slipFile) {
        try {
            if (slipFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please select a file"));
            }
            
            // Validate file type
            String contentType = slipFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed"));
            }
            
            // Validate file size (5MB max)
            if (slipFile.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "File size must not exceed 5MB"));
            }
            
            Invoice invoice = invoiceService.uploadPaymentSlip(id, slipFile);
            return ResponseEntity.ok(invoice);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Verify payment slip (Admin only)
     * 
     * POST /invoices/{id}/verify
     * 
     * Request Body:
     * {
     *   "approved": true,
     *   "notes": "Payment verified successfully"
     * }
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<?> verifyPayment(
            @PathVariable Long id,
            @RequestBody VerifyPaymentRequest request) {
        try {
            Invoice invoice = invoiceService.verifyPayment(id, request.isApproved(), request.getNotes());
            return ResponseEntity.ok(invoice);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get all invoices waiting for verification (Admin only)
     * 
     * GET /invoices/waiting-verification
     */
    @GetMapping("/waiting-verification")
    public ResponseEntity<List<Invoice>> getInvoicesWaitingVerification() {
        List<Invoice> invoices = invoiceService.getInvoicesWaitingVerification();
        return ResponseEntity.ok(invoices);
    }
    
    // Request DTO for payment verification
    static class VerifyPaymentRequest {
        private boolean approved;
        private String notes;
        
        public boolean isApproved() {
            return approved;
        }
        
        public void setApproved(boolean approved) {
            this.approved = approved;
        }
        
        public String getNotes() {
            return notes;
        }
        
        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
}
