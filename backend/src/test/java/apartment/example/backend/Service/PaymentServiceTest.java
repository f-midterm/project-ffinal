package apartment.example.backend.Service;

import apartment.example.backend.entity.Lease;
import apartment.example.backend.entity.Payment;
import apartment.example.backend.entity.enums.PaymentStatus;
import apartment.example.backend.entity.enums.PaymentType;
import apartment.example.backend.repository.LeaseRepository;
import apartment.example.backend.repository.PaymentRepository;
import apartment.example.backend.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private LeaseRepository leaseRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Payment payment;
    private Lease lease;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        lease = new Lease();
        lease.setId(1L);

        payment = new Payment();
        payment.setId(10L);
        payment.setLease(lease);
        payment.setAmount(BigDecimal.valueOf(1000));
        payment.setPaymentType(PaymentType.RENT);
        payment.setStatus(PaymentStatus.PENDING);
    }

    @Test
    void testCreatePayment_GenerateReceiptAndSave() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        Payment saved = paymentService.createPayment(payment);

        assertNotNull(saved.getReceiptNumber());
        assertTrue(saved.getReceiptNumber().startsWith("RENT"));
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    void testCreateBillByAdmin_Success() {
        when(leaseRepository.findById(1L)).thenReturn(Optional.of(lease));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        Payment bill = paymentService.createBillByAdmin(
                1L, PaymentType.WATER, BigDecimal.valueOf(500),
                LocalDate.now().plusDays(5), "Water Bill"
        );

        assertEquals(PaymentType.WATER, bill.getPaymentType());
        assertEquals(PaymentStatus.PENDING, bill.getStatus());
        assertNotNull(bill.getReceiptNumber());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testMarkAsPaid_ChangesStatusAndSaves() {
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        Payment updated = paymentService.markAsPaid(10L, LocalDate.now(), "cash", "paid fully");

        assertEquals(PaymentStatus.PAID, updated.getStatus());
        assertNotNull(updated.getPaidDate());
        assertTrue(updated.getNotes().contains("Payment Notes"));
        verify(paymentRepository).save(payment);
    }

    @Test
    void testMarkAsPartial_ValidPartialPayment() {
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        Payment updated = paymentService.markAsPartial(10L, BigDecimal.valueOf(500), "half paid");

        assertEquals(PaymentStatus.PARTIAL, updated.getStatus());
        assertTrue(updated.getNotes().contains("Partial Payment"));
        verify(paymentRepository).save(payment);
    }

    @Test
    void testMarkOverduePayments_UpdatesPendingToOverdue() {
        Payment p1 = new Payment();
        p1.setLease(lease);
        p1.setStatus(PaymentStatus.PENDING);
        p1.setPaymentType(PaymentType.RENT);

        when(paymentRepository.findOverduePayments(any())).thenReturn(List.of(p1));

        paymentService.markOverduePayments();

        assertEquals(PaymentStatus.OVERDUE, p1.getStatus());
        verify(paymentRepository).saveAll(List.of(p1));
    }

    @Test
    void testCreateBillByAdmin_LeaseNotFound_ThrowsException() {
        when(leaseRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
                paymentService.createBillByAdmin(999L, PaymentType.OTHER,
                        BigDecimal.TEN, LocalDate.now(), "Missing lease"));
    }
}
