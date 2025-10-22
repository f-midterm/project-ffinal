package apartment.example.backend.controller;

import apartment.example.backend.entity.*;
import apartment.example.backend.entity.enums.*;
import apartment.example.backend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private PaymentController paymentController;

    private Payment mockPayment;
    private Lease mockLease;
    private Tenant mockTenant;
    private Unit mockUnit;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockTenant = new Tenant();
        mockTenant.setEmail("test@demo.com");
        mockTenant.setFirstName("John");
        mockTenant.setLastName("Doe");

        mockUnit = new Unit();
        mockUnit.setRoomNumber("A101");
        mockUnit.setStatus(UnitStatus.OCCUPIED);

        mockLease = new Lease();
        mockLease.setId(1L);
        mockLease.setTenant(mockTenant);
        mockLease.setUnit(mockUnit);

        // 🔥 ให้ unit และ tenant รู้จัก lease ด้วย (ป้องกัน null pointer)
        mockUnit.setLeases(List.of(mockLease));
        mockTenant.setLeases(List.of(mockLease));

        mockPayment = new Payment();
        mockPayment.setId(10L);
        mockPayment.setLease(mockLease);
        mockPayment.setPaymentType(PaymentType.RENT);
        mockPayment.setAmount(BigDecimal.valueOf(5000));
        mockPayment.setStatus(PaymentStatus.PAID);
        mockPayment.setDueDate(LocalDate.of(2025, 10, 1));
    }


    @Test
    void testGetAllPayments_ReturnsList() {
        when(paymentService.getAllPayments()).thenReturn(List.of(mockPayment));

        ResponseEntity<?> response = paymentController.getAllPayments();

        assertEquals(200, response.getStatusCodeValue());
        List<?> body = (List<?>) response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        verify(paymentService, times(1)).getAllPayments();
    }


    @Test
    void testGetMonthlyPaymentSummary_ReturnsSummary() {
        when(paymentService.getAllPayments()).thenReturn(List.of(mockPayment));

        ResponseEntity<?> response = paymentController.getMonthlyPaymentSummary("2025-10");

        assertEquals(200, response.getStatusCodeValue());
        List<?> result = (List<?>) response.getBody();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(paymentService).getAllPayments();
    }


    @Test
    void testDeleteMonthlyPayment_Success() {
        mockPayment.setStatus(PaymentStatus.PENDING);

        when(paymentService.getAllPayments()).thenReturn(List.of(mockPayment));

        Map<String, Object> req = Map.of(
                "tenantEmail", "test@demo.com",
                "month", "2025-10",
                "unitNumber", "A101"
        );

        ResponseEntity<?> response = paymentController.deleteMonthlyPayment(req);

        assertEquals(200, response.getStatusCodeValue());
        verify(paymentService).deletePayment(10L);
    }


    @Test
    void testUpdateMonthlyPayment_InvalidStatus() {
        when(paymentService.getAllPayments()).thenReturn(List.of(mockPayment));

        Map<String, Object> req = new HashMap<>();
        req.put("totalAmount", 10000.0);
        req.put("status", "INVALID");
        req.put("notes", "test");

        ResponseEntity<?> response = paymentController.updateMonthlyPayment("test@demo.com", "2025-10", req);

        assertEquals(400, response.getStatusCodeValue());
    }


    @Test
    void testMarkMonthlyPaymentAsPaid_Success() {
        when(paymentService.getAllPayments()).thenReturn(List.of(mockPayment));
        when(paymentService.markAsPaid(anyLong(), any(), anyString(), anyString())).thenReturn(mockPayment);

        Map<String, Object> req = Map.of(
                "tenantEmail", "test@demo.com",
                "month", "2025-10",
                "unitNumber", "A101",
                "transactionNumber", "TX123",
                "transactionDate", "2025-10-01",
                "transactionTime", "10:00"
        );

        ResponseEntity<?> response = paymentController.markMonthlyPaymentAsPaid(req);

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(true, body.get("success"));
        verify(paymentService, atLeastOnce()).markAsPaid(anyLong(), any(), anyString(), anyString());
    }


    @Test
    void testGetVillagerPaymentHistory_ReturnsHistory() {
        when(paymentService.getAllPayments()).thenReturn(List.of(mockPayment));

        ResponseEntity<?> response = paymentController.getVillagerPaymentHistory("test@demo.com");

        assertEquals(200, response.getStatusCodeValue());
        List<?> result = (List<?>) response.getBody();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}