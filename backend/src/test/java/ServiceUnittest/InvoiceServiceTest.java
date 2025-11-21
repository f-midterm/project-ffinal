package apartment.example.backend.service;

import apartment.example.backend.entity.Invoice;
import apartment.example.backend.entity.Lease;
import apartment.example.backend.entity.Payment;
import apartment.example.backend.entity.enums.InvoiceType;
import apartment.example.backend.entity.enums.PaymentStatus;
import apartment.example.backend.entity.enums.PaymentType;
import apartment.example.backend.repository.InvoiceRepository;
import apartment.example.backend.repository.LeaseRepository;
import apartment.example.backend.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceServiceTest {

    @InjectMocks
    private InvoiceService invoiceService;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private LeaseRepository leaseRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateInvoiceWithPayments() {
        Lease lease = new Lease();
        lease.setId(1L);

        InvoiceService.PaymentItem item = new InvoiceService.PaymentItem(PaymentType.RENT, BigDecimal.valueOf(1000), "Rent");
        List<InvoiceService.PaymentItem> items = List.of(item);

        when(leaseRepository.findById(1L)).thenReturn(Optional.of(lease));
        when(invoiceRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(paymentService.generateReceiptNumber(PaymentType.RENT)).thenReturn("RENT-001");

        Invoice invoice = invoiceService.createInvoiceWithPayments(1L, LocalDate.now(), LocalDate.now().plusDays(7), items, "Test notes");

        assertNotNull(invoice);
        assertEquals(BigDecimal.valueOf(1000), invoice.getTotalAmount());
        assertEquals("Test notes", invoice.getNotes());
        verify(invoiceRepository, times(2)).save(any()); // save invoice twice (before & after adding payments)
    }

    @Test
    void testGetInvoiceById() {
        Invoice invoice = new Invoice();
        invoice.setId(1L);

        when(invoiceRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(invoice));

        Invoice result = invoiceService.getInvoiceById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetInvoicesByLeaseId() {
        Invoice invoice = new Invoice();
        invoice.setId(1L);

        when(invoiceRepository.findByLeaseId(1L)).thenReturn(List.of(invoice));

        List<Invoice> invoices = invoiceService.getInvoicesByLeaseId(1L);
        assertEquals(1, invoices.size());
    }

    @Test
    void testVerifyPaymentApproved() {
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setStatus(Invoice.InvoiceStatus.WAITING_VERIFICATION);
        Payment payment = new Payment();
        invoice.addPayment(payment);

        when(invoiceRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(invoice)).thenReturn(invoice);

        Invoice result = invoiceService.verifyPayment(1L, true, "Approved");
        assertEquals(Invoice.InvoiceStatus.PAID, result.getStatus());
        assertEquals(PaymentStatus.PAID, result.getPayments().get(0).getStatus());
    }

    @Test
    void testDeleteInvoice() {
        doNothing().when(invoiceRepository).deleteById(1L);
        invoiceService.deleteInvoice(1L);
        verify(invoiceRepository, times(1)).deleteById(1L);
    }
}
