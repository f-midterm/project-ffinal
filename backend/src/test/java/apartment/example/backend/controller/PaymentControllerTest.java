package apartment.example.backend.controller;

import apartment.example.backend.dto.PaymentResponseDto;
import apartment.example.backend.entity.Payment;
import apartment.example.backend.entity.Lease;
import apartment.example.backend.entity.Tenant;
import apartment.example.backend.entity.Unit;
import apartment.example.backend.entity.enums.PaymentStatus;
import apartment.example.backend.entity.enums.PaymentType;
import apartment.example.backend.entity.enums.UnitStatus;
import apartment.example.backend.service.PaymentService;
import apartment.example.backend.service.LeaseService;
import apartment.example.backend.service.TenantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @Mock
    private LeaseService leaseService;

    @Mock
    private TenantService tenantService;

    @InjectMocks
    private PaymentController paymentController;

    private ObjectMapper objectMapper;
    private Payment testPayment;
    private Lease testLease;
    private Tenant testTenant;
    private Unit testUnit;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Setup test data
        testTenant = new Tenant();
        testTenant.setId(1L);
        testTenant.setFirstName("John");
        testTenant.setLastName("Doe");
        testTenant.setEmail("john.doe@example.com");
        testTenant.setPhone("1234567890");

        testUnit = new Unit();
        testUnit.setId(1L);
        testUnit.setRoomNumber("101");
        testUnit.setFloor(1);
        testUnit.setType("1BR");
        testUnit.setStatus(UnitStatus.OCCUPIED);

        testLease = new Lease();
        testLease.setId(1L);
        testLease.setTenant(testTenant);
        testLease.setUnit(testUnit);
        testLease.setRentAmount(BigDecimal.valueOf(15000));

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setLease(testLease);
        testPayment.setAmount(BigDecimal.valueOf(15000));
        testPayment.setPaymentType(PaymentType.RENT);
        testPayment.setStatus(PaymentStatus.PENDING);
        testPayment.setDueDate(LocalDate.now().plusDays(5));
        testPayment.setNotes("Monthly rent payment");
    }

    @Test
    void getAllPayments_Success() throws Exception {
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getAllPayments()).thenReturn(payments);

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].amount").value(15000));

        verify(paymentService, times(1)).getAllPayments();
    }

//    @Test
//    void getAllPaymentsPaged_Success() throws Exception {
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Payment> paymentsPage = new PageImpl<>(Arrays.asList(testPayment), pageable, 1);
//        when(paymentService.getAllPayments(any(Pageable.class))).thenReturn(paymentsPage);
//
//        mockMvc.perform(get("/api/payments/paged")
//                        .param("page", "0")
//                        .param("size", "10"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content[0].id").value(1L));
//
//        verify(paymentService, times(1)).getAllPayments(any(Pageable.class));
//    }

    @Test
    void getPaymentById_Found() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(Optional.of(testPayment));

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(15000));

        verify(paymentService, times(1)).getPaymentById(1L);
    }

    @Test
    void getPaymentById_NotFound() throws Exception {
        when(paymentService.getPaymentById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/payments/999"))
                .andExpect(status().isNotFound());

        verify(paymentService, times(1)).getPaymentById(999L);
    }

    @Test
    void getPaymentsByLease_Success() throws Exception {
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getPaymentsByLease(1L)).thenReturn(payments);

        mockMvc.perform(get("/api/payments/lease/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].leaseId").value(1L));

        verify(paymentService, times(1)).getPaymentsByLease(1L);
    }

    @Test
    void getPaymentsByStatus_Success() throws Exception {
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getPaymentsByStatus(PaymentStatus.PENDING)).thenReturn(payments);

        mockMvc.perform(get("/api/payments/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(paymentService, times(1)).getPaymentsByStatus(PaymentStatus.PENDING);
    }

    @Test
    void getPaymentsByType_Success() throws Exception {
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getPaymentsByType(PaymentType.RENT)).thenReturn(payments);

        mockMvc.perform(get("/api/payments/type/RENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("RENT"));

        verify(paymentService, times(1)).getPaymentsByType(PaymentType.RENT);
    }

    @Test
    void getPaymentsByUnit_Success() throws Exception {
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getPaymentsByUnit(1L)).thenReturn(payments);

        mockMvc.perform(get("/api/payments/unit/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].unit.id").value(1L));

        verify(paymentService, times(1)).getPaymentsByUnit(1L);
    }

    @Test
    void getPaymentsByTenant_Success() throws Exception {
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getPaymentsByTenant(1L)).thenReturn(payments);

        mockMvc.perform(get("/api/payments/tenant/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tenant.id").value(1L));

        verify(paymentService, times(1)).getPaymentsByTenant(1L);
    }

    @Test
    void getOverduePayments_Success() throws Exception {
        testPayment.setStatus(PaymentStatus.OVERDUE);
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getOverduePayments()).thenReturn(payments);

        mockMvc.perform(get("/api/payments/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("OVERDUE"));

        verify(paymentService, times(1)).getOverduePayments();
    }

    @Test
    void getPaymentsDueBetween_Success() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(30);
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getPaymentsDueBetween(startDate, endDate)).thenReturn(payments);

        mockMvc.perform(get("/api/payments/due-between")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(paymentService, times(1)).getPaymentsDueBetween(startDate, endDate);
    }

    @Test
    void getRevenue_Success() throws Exception {
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();
        BigDecimal totalRevenue = BigDecimal.valueOf(50000);
        when(paymentService.getTotalRevenueByDateRange(startDate, endDate)).thenReturn(totalRevenue);

        mockMvc.perform(get("/api/payments/revenue")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(50000));

        verify(paymentService, times(1)).getTotalRevenueByDateRange(startDate, endDate);
    }

    @Test
    void createPayment_Success() throws Exception {
        when(paymentService.createPayment(any(Payment.class))).thenReturn(testPayment);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPayment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        verify(paymentService, times(1)).createPayment(any(Payment.class));
    }

    @Test
    void createPayment_BadRequest() throws Exception {
        when(paymentService.createPayment(any(Payment.class)))
                .thenThrow(new RuntimeException("Invalid payment data"));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPayment)))
                .andExpect(status().isBadRequest());

        verify(paymentService, times(1)).createPayment(any(Payment.class));
    }

    @Test
    void createBillByAdmin_Success() throws Exception {
        when(paymentService.createBillByAdmin(anyLong(), any(PaymentType.class),
                any(BigDecimal.class), any(LocalDate.class), anyString()))
                .thenReturn(testPayment);

        mockMvc.perform(post("/api/payments/create-bill")
                        .param("leaseId", "1")
                        .param("paymentType", "RENT")
                        .param("amount", "15000")
                        .param("dueDate", LocalDate.now().toString())
                        .param("description", "Monthly rent"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        verify(paymentService, times(1)).createBillByAdmin(anyLong(), any(PaymentType.class),
                any(BigDecimal.class), any(LocalDate.class), anyString());
    }

    @Test
    void updatePayment_Success() throws Exception {
        when(paymentService.updatePayment(eq(1L), any(Payment.class))).thenReturn(testPayment);

        mockMvc.perform(put("/api/payments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPayment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(paymentService, times(1)).updatePayment(eq(1L), any(Payment.class));
    }

    @Test
    void updatePayment_NotFound() throws Exception {
        when(paymentService.updatePayment(eq(999L), any(Payment.class)))
                .thenThrow(new RuntimeException("Payment not found"));

        mockMvc.perform(put("/api/payments/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPayment)))
                .andExpect(status().isNotFound());

        verify(paymentService, times(1)).updatePayment(eq(999L), any(Payment.class));
    }

    @Test
    void updatePayment_BadRequest() throws Exception {
        when(paymentService.updatePayment(eq(1L), any(Payment.class)))
                .thenThrow(new IllegalArgumentException("Invalid payment data"));

        mockMvc.perform(put("/api/payments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPayment)))
                .andExpect(status().isBadRequest());

        verify(paymentService, times(1)).updatePayment(eq(1L), any(Payment.class));
    }

    @Test
    void markAsPaid_Success() throws Exception {
        testPayment.setStatus(PaymentStatus.PAID);
        testPayment.setPaidDate(LocalDate.now());
        when(paymentService.markAsPaid(eq(1L), any(LocalDate.class), anyString(), anyString()))
                .thenReturn(testPayment);

        Map<String, Object> request = new HashMap<>();
        request.put("paidDate", LocalDate.now().toString());
        request.put("paymentMethod", "BANK_TRANSFER");
        request.put("notes", "Paid successfully");

        mockMvc.perform(patch("/api/payments/1/mark-paid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        verify(paymentService, times(1)).markAsPaid(eq(1L), any(LocalDate.class), anyString(), anyString());
    }

//    @Test
//    void markAsPaid_NotFound() throws Exception {
//        when(paymentService.markAsPaid(eq(999L), any(LocalDate.class), anyString(), anyString()))
//                .thenThrow(new RuntimeException("Payment not found"));
//
//        Map<String, Object> request = new HashMap<>();
//        request.put("paidDate", LocalDate.now().toString());
//        request.put("paymentMethod", "BANK_TRANSFER");
//
//        mockMvc.perform(patch("/api/payments/999/mark-paid")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isNotFound());
//
//        verify(paymentService, times(1)).markAsPaid(eq(999L), any(LocalDate.class), anyString(), anyString());
//    }

    @Test
    void markAsPartial_Success() throws Exception {
        testPayment.setStatus(PaymentStatus.PARTIAL);
        when(paymentService.markAsPartial(eq(1L), any(BigDecimal.class), anyString()))
                .thenReturn(testPayment);

        Map<String, Object> request = new HashMap<>();
        request.put("paidAmount", "7500");
        request.put("notes", "Partial payment");

        mockMvc.perform(patch("/api/payments/1/mark-partial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARTIAL"));

        verify(paymentService, times(1)).markAsPartial(eq(1L), any(BigDecimal.class), anyString());
    }

    @Test
    void markOverduePayments_Success() throws Exception {
        doNothing().when(paymentService).markOverduePayments();

        mockMvc.perform(post("/api/payments/mark-overdue"))
                .andExpect(status().isOk())
                .andExpect(content().string("Overdue payments marked successfully"));

        verify(paymentService, times(1)).markOverduePayments();
    }

    @Test
    void markOverduePayments_Error() throws Exception {
        doThrow(new RuntimeException("Error marking overdue"))
                .when(paymentService).markOverduePayments();

        mockMvc.perform(post("/api/payments/mark-overdue"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error marking overdue payments"));

        verify(paymentService, times(1)).markOverduePayments();
    }

    @Test
    void deletePayment_Success() throws Exception {
        doNothing().when(paymentService).deletePayment(1L);

        mockMvc.perform(delete("/api/payments/1"))
                .andExpect(status().isNoContent());

        verify(paymentService, times(1)).deletePayment(1L);
    }

    @Test
    void deletePayment_NotFound() throws Exception {
        doThrow(new RuntimeException("Payment not found"))
                .when(paymentService).deletePayment(999L);

        mockMvc.perform(delete("/api/payments/999"))
                .andExpect(status().isNotFound());

        verify(paymentService, times(1)).deletePayment(999L);
    }

    @Test
    void deletePayment_BadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Cannot delete paid payment"))
                .when(paymentService).deletePayment(1L);

        mockMvc.perform(delete("/api/payments/1"))
                .andExpect(status().isBadRequest());

        verify(paymentService, times(1)).deletePayment(1L);
    }

    @Test
    void checkPaymentExists_True() throws Exception {
        when(paymentService.existsById(1L)).thenReturn(true);

        mockMvc.perform(get("/api/payments/1/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(paymentService, times(1)).existsById(1L);
    }

    @Test
    void checkPaymentExists_False() throws Exception {
        when(paymentService.existsById(999L)).thenReturn(false);

        mockMvc.perform(get("/api/payments/999/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(paymentService, times(1)).existsById(999L);
    }

    @Test
    void getActiveLeasesForBilling_Success() throws Exception {
        List<Lease> activeLeases = Arrays.asList(testLease);
        when(leaseService.getActiveLeases()).thenReturn(activeLeases);

        mockMvc.perform(get("/api/payments/active-leases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].leaseId").value(1L))
                .andExpect(jsonPath("$[0].unitNumber").value("101"));

        verify(leaseService, times(1)).getActiveLeases();
    }

    @Test
    void getActiveLeasesForBilling_Error() throws Exception {
        when(leaseService.getActiveLeases()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/payments/active-leases"))
                .andExpect(status().isInternalServerError());

        verify(leaseService, times(1)).getActiveLeases();
    }

    @Test
    void getMonthlyPaymentSummary_Success() throws Exception {
        testPayment.setDueDate(LocalDate.of(2024, 1, 5));
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getAllPayments()).thenReturn(payments);

        mockMvc.perform(get("/api/payments/monthly-summary"))
                .andExpect(status().isOk());

        verify(paymentService, times(1)).getAllPayments();
    }

    @Test
    void getMonthlyPaymentSummary_WithMonthFilter() throws Exception {
        testPayment.setDueDate(LocalDate.of(2024, 1, 5));
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getAllPayments()).thenReturn(payments);

        mockMvc.perform(get("/api/payments/monthly-summary")
                        .param("month", "2024-01"))
                .andExpect(status().isOk());

        verify(paymentService, times(1)).getAllPayments();
    }

    @Test
    void bulkCreatePayments_Success() throws Exception {
        when(leaseService.getLeaseById(1L)).thenReturn(Optional.of(testLease));
        when(paymentService.createPayment(any(Payment.class))).thenReturn(testPayment);

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("leaseId", 1);
        paymentData.put("paymentType", "RENT");
        paymentData.put("amount", 15000);
        paymentData.put("dueDate", LocalDate.now().toString());
        paymentData.put("notes", "Monthly rent");

        Map<String, Object> request = new HashMap<>();
        request.put("payments", Arrays.asList(paymentData));
        request.put("month", "2024-01");

        mockMvc.perform(post("/api/payments/bulk-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(paymentService, times(1)).createPayment(any(Payment.class));
    }

    @Test
    void bulkCreatePayments_Error() throws Exception {
        when(leaseService.getLeaseById(1L)).thenReturn(Optional.empty());

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("leaseId", 1);
        paymentData.put("paymentType", "RENT");
        paymentData.put("amount", 15000);
        paymentData.put("dueDate", LocalDate.now().toString());

        Map<String, Object> request = new HashMap<>();
        request.put("payments", Arrays.asList(paymentData));
        request.put("month", "2024-01");

        mockMvc.perform(post("/api/payments/bulk-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void markMonthlyPaymentAsPaid_Success() throws Exception {
        testPayment.setDueDate(LocalDate.of(2024, 1, 5));
        testPayment.setStatus(PaymentStatus.PAID);
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getAllPayments()).thenReturn(payments);
        when(paymentService.markAsPaid(anyLong(), any(LocalDate.class), anyString(), anyString()))
                .thenReturn(testPayment);

        Map<String, Object> request = new HashMap<>();
        request.put("tenantEmail", "john.doe@example.com");
        request.put("month", "2024-01");
        request.put("unitNumber", "101");
        request.put("transactionNumber", "TXN123456");
        request.put("transactionDate", "2024-01-15");
        request.put("transactionTime", "14:30");

        mockMvc.perform(post("/api/payments/mark-monthly-paid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(paymentService, atLeastOnce()).getAllPayments();
    }

    @Test
    void deleteMonthlyPayment_Success() throws Exception {
        testPayment.setDueDate(LocalDate.of(2024, 1, 5));
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getAllPayments()).thenReturn(payments);
        doNothing().when(paymentService).deletePayment(anyLong());

        Map<String, Object> request = new HashMap<>();
        request.put("tenantEmail", "john.doe@example.com");
        request.put("month", "2024-01");
        request.put("unitNumber", "101");

        mockMvc.perform(delete("/api/payments/monthly-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(paymentService, times(1)).deletePayment(anyLong());
    }

    @Test
    void deleteMonthlyPayment_PaidPayment() throws Exception {
        testPayment.setDueDate(LocalDate.of(2024, 1, 5));
        testPayment.setStatus(PaymentStatus.PAID);
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getAllPayments()).thenReturn(payments);

        Map<String, Object> request = new HashMap<>();
        request.put("tenantEmail", "john.doe@example.com");
        request.put("month", "2024-01");
        request.put("unitNumber", "101");

        mockMvc.perform(delete("/api/payments/monthly-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot delete paid payment"));

        verify(paymentService, never()).deletePayment(anyLong());
    }

    @Test
    void getVillagerPaymentHistory_Success() throws Exception {
        testPayment.setDueDate(LocalDate.of(2024, 1, 5));
        testPayment.setStatus(PaymentStatus.PAID);
        testPayment.setPaidDate(LocalDate.of(2024, 1, 10));
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getAllPayments()).thenReturn(payments);

        mockMvc.perform(get("/api/payments/villager/history/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tenantEmail").value("john.doe@example.com"));

        verify(paymentService, times(1)).getAllPayments();
    }

    @Test
    void updateMonthlyPayment_Success() throws Exception {
        testPayment.setDueDate(LocalDate.of(2024, 1, 5));
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getAllPayments()).thenReturn(payments);
        when(paymentService.updatePayment(anyLong(), any(Payment.class))).thenReturn(testPayment);

        Map<String, Object> request = new HashMap<>();
        request.put("totalAmount", 16000);
        request.put("status", "PAID");
        request.put("notes", "Updated payment");

        mockMvc.perform(put("/api/payments/monthly/john.doe@example.com/2024-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(paymentService, times(1)).updatePayment(anyLong(), any(Payment.class));
    }
}