package apartment.example.backend.controller;

import apartment.example.backend.dto.PaymentResponseDto;
import apartment.example.backend.entity.*;
import apartment.example.backend.entity.enums.PaymentStatus;
import apartment.example.backend.entity.enums.PaymentType;
import apartment.example.backend.entity.enums.UnitStatus;
import apartment.example.backend.service.LeaseService;
import apartment.example.backend.service.PaymentService;
import apartment.example.backend.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private LeaseService leaseService;

    @Mock
    private TenantService tenantService;

    @InjectMocks
    private PaymentController controller;

    private Payment samplePayment;
    private Lease sampleLease;
    private Tenant sampleTenant;
    private Unit sampleUnit;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sampleTenant = new Tenant();
        sampleTenant.setId(1L);
        sampleTenant.setFirstName("John");
        sampleTenant.setLastName("Doe");
        sampleTenant.setEmail("john@example.com");

        sampleUnit = new Unit();
        sampleUnit.setId(1L);
        sampleUnit.setRoomNumber("101");
        sampleUnit.setStatus(UnitStatus.OCCUPIED);

        sampleLease = new Lease();
        sampleLease.setId(1L);
        sampleLease.setTenant(sampleTenant);
        sampleLease.setUnit(sampleUnit);

        samplePayment = new Payment();
        samplePayment.setId(1L);
        samplePayment.setAmount(BigDecimal.valueOf(1000));
        samplePayment.setStatus(PaymentStatus.PENDING);
        samplePayment.setPaymentType(PaymentType.RENT);
        samplePayment.setDueDate(LocalDate.now());
        samplePayment.setLease(sampleLease);
    }

    @Test
    void testGetAllPayments() {
        when(paymentService.getAllPayments()).thenReturn(Collections.singletonList(samplePayment));
        ResponseEntity<List<PaymentResponseDto>> response = controller.getAllPayments();
        assertEquals(1, response.getBody().size());
        verify(paymentService).getAllPayments();
    }

    @Test
    void testGetPaymentById_Found() {
        when(paymentService.getPaymentById(1L)).thenReturn(Optional.of(samplePayment));
        ResponseEntity<PaymentResponseDto> response = controller.getPaymentById(1L);
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testGetPaymentById_NotFound() {
        when(paymentService.getPaymentById(1L)).thenReturn(Optional.empty());
        ResponseEntity<PaymentResponseDto> response = controller.getPaymentById(1L);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetPaymentsByStatus() {
        when(paymentService.getPaymentsByStatus(PaymentStatus.PENDING))
                .thenReturn(Collections.singletonList(samplePayment));
        ResponseEntity<List<PaymentResponseDto>> response = controller.getPaymentsByStatus(PaymentStatus.PENDING);
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetPaymentsByType() {
        when(paymentService.getPaymentsByType(PaymentType.RENT))
                .thenReturn(Collections.singletonList(samplePayment));
        ResponseEntity<List<PaymentResponseDto>> response = controller.getPaymentsByType(PaymentType.RENT);
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCreatePayment_Success() {
        when(paymentService.createPayment(samplePayment)).thenReturn(samplePayment);
        ResponseEntity<PaymentResponseDto> response = controller.createPayment(samplePayment);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(samplePayment.getAmount(), response.getBody().getAmount());
    }

    @Test
    void testUpdatePayment_Success() {
        when(paymentService.updatePayment(1L, samplePayment)).thenReturn(samplePayment);
        ResponseEntity<PaymentResponseDto> response = controller.updatePayment(1L, samplePayment);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDeletePayment_Success() {
        doNothing().when(paymentService).deletePayment(1L);
        ResponseEntity<Void> response = controller.deletePayment(1L);
        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void testCheckPaymentExists_True() {
        when(paymentService.existsById(1L)).thenReturn(true);
        ResponseEntity<Boolean> response = controller.checkPaymentExists(1L);
        assertTrue(response.getBody());
    }

    @Test
    void testMarkAsPaid() {
        when(paymentService.markAsPaid(
                anyLong(),      // id
                any(LocalDate.class),
                anyString(),    // paymentMethod
                anyString()     // notes
        )).thenReturn(samplePayment);
        Map<String, Object> markPaidRequest = new HashMap<>();
        markPaidRequest.put("paymentMethod", "BANK_TRANSFER");
        markPaidRequest.put("notes", "Test payment");

        ResponseEntity<PaymentResponseDto> markPaidResponse = controller.markAsPaid(1L, markPaidRequest);

        assertEquals(200, markPaidResponse.getStatusCodeValue());
        assertNotNull(markPaidResponse.getBody());
        assertEquals(samplePayment.getAmount(), markPaidResponse.getBody().getAmount());
    }

    @Test
    void testGetMonthlyPaymentSummary() {
        when(paymentService.getAllPayments()).thenReturn(Collections.singletonList(samplePayment));
        ResponseEntity<List<Map<String, Object>>> response = controller.getMonthlyPaymentSummary(null);
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetRevenue() {
        when(paymentService.getTotalRevenueByDateRange(any(), any())).thenReturn(BigDecimal.valueOf(1000));
        ResponseEntity<Map<String, Object>> response = controller.getRevenue(LocalDate.now(), LocalDate.now());
        assertEquals(BigDecimal.valueOf(1000), response.getBody().get("totalRevenue"));
    }

    @Test
    void testMarkOverduePayments() {
        doNothing().when(paymentService).markOverduePayments();
        ResponseEntity<String> response = controller.markOverduePayments();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Overdue payments marked successfully", response.getBody());
    }

    @Test
    void testGetVillagerPaymentHistory() {
        when(paymentService.getAllPayments()).thenReturn(Collections.singletonList(samplePayment));
        ResponseEntity<List<Map<String, Object>>> response = controller.getVillagerPaymentHistory("john@example.com");
        assertEquals(1, response.getBody().size());
    }
}
