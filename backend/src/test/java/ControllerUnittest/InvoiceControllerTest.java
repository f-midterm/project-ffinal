package apartment.example.backend.controller;

import apartment.example.backend.entity.Invoice;
import apartment.example.backend.entity.enums.InvoiceType;
import apartment.example.backend.service.InvoiceService;
import apartment.example.backend.service.PdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InvoiceControllerTest {

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private PdfService pdfService;

    @InjectMocks
    private InvoiceController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createInvoice_monthlyRent_success() {
        InvoiceController.CreateInvoiceRequest req = new InvoiceController.CreateInvoiceRequest();
        req.setLeaseId(1L);
        req.setInvoiceDate(LocalDate.now());
        req.setDueDate(LocalDate.now().plusDays(5));
        req.setRentAmount(BigDecimal.valueOf(5000));
        req.setElectricityAmount(BigDecimal.valueOf(800));
        req.setWaterAmount(BigDecimal.valueOf(200));
        req.setInvoiceType("MONTHLY_RENT");

        Invoice invoice = new Invoice();
        when(invoiceService.createInvoiceWithPayments(any(), any(), any(), any(), any(), any()))
                .thenReturn(invoice);

        ResponseEntity<Invoice> response = controller.createInvoice(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(invoice, response.getBody());
    }

    @Test
    void createInvoice_invalidType_shouldReturnBadRequest() {
        InvoiceController.CreateInvoiceRequest req = new InvoiceController.CreateInvoiceRequest();
        req.setInvoiceType("INVALID_TYPE");

        ResponseEntity<Invoice> response = controller.createInvoice(req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getInvoiceById_success() {
        Invoice invoice = new Invoice();
        when(invoiceService.getInvoiceById(1L)).thenReturn(invoice);

        ResponseEntity<Invoice> response = controller.getInvoiceById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(invoice, response.getBody());
    }

    @Test
    void getInvoiceById_notFound() {
        when(invoiceService.getInvoiceById(1L)).thenThrow(new RuntimeException());
        ResponseEntity<Invoice> response = controller.getInvoiceById(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void downloadInvoicePdf_success() {
        Invoice inv = new Invoice();
        inv.setInvoiceNumber("INV001");

        when(invoiceService.getInvoiceById(1L)).thenReturn(inv);
        when(pdfService.generateInvoicePdf(inv)).thenReturn(new byte[]{1, 2, 3});

        ResponseEntity<byte[]> resp = controller.downloadInvoicePdf(1L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }

    @Test
    void uploadSlip_wrongFileType_shouldReturnBadRequest() {
        MockMultipartFile file = new MockMultipartFile("slip", "test.txt", "text/plain", "data".getBytes());

        ResponseEntity<?> resp = controller.uploadPaymentSlip(1L, file);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void uploadSlip_success() {
        MockMultipartFile file = new MockMultipartFile("slip", "image.png", "image/png", "data".getBytes());
        Invoice invoice = new Invoice();

        when(invoiceService.uploadPaymentSlip(eq(1L), any())).thenReturn(invoice);

        ResponseEntity<?> resp = controller.uploadPaymentSlip(1L, file);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void verifyPayment_success() {
        Invoice invoice = new Invoice();
        InvoiceController.VerifyPaymentRequest req = new InvoiceController.VerifyPaymentRequest();
        req.setApproved(true);
        req.setNotes("ok");

        when(invoiceService.verifyPayment(1L, true, "ok")).thenReturn(invoice);

        ResponseEntity<?> resp = controller.verifyPayment(1L, req);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(invoice, resp.getBody());
    }

    @Test
    void deleteInvoice_success() {
        ResponseEntity<Void> resp = controller.deleteInvoice(1L);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        verify(invoiceService, times(1)).deleteInvoice(1L);
    }
}
