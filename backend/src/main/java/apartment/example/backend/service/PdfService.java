package apartment.example.backend.service;

import apartment.example.backend.entity.Invoice;
import apartment.example.backend.entity.Lease;
import apartment.example.backend.entity.Payment;
import apartment.example.backend.entity.Tenant;
import apartment.example.backend.entity.Unit;
import apartment.example.backend.entity.enums.PaymentType;
import apartment.example.backend.repository.InvoiceRepository;
import apartment.example.backend.repository.PaymentRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    /**
     * Generate Lease Agreement PDF
     * 
     * @param lease Lease entity with tenant and unit information
     * @return PDF as byte array
     */
    public byte[] generateLeaseAgreementPdf(Lease lease) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            Tenant tenant = lease.getTenant();
            Unit unit = lease.getUnit();

            // Title
            Paragraph title = new Paragraph("Rent Invoice")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30);
            document.add(title);

            // Invoice Details Table
            Table invoiceDetailsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .useAllAvailableWidth()
                    .setMarginBottom(20);

            // Header
            Cell headerCell = new Cell(1, 2)
                    .add(new Paragraph("Invoice Details"))
                    .setBackgroundColor(new DeviceRgb(240, 240, 240))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold();
            invoiceDetailsTable.addHeaderCell(headerCell);

            // Get payments for this lease to get payment details
            List<Payment> payments = paymentRepository.findByLeaseId(lease.getId());
            
            // Get latest invoice for this lease
            List<Invoice> invoices = invoiceRepository.findByLeaseId(lease.getId());
            String invoiceNumber = invoices.isEmpty() ? 
                String.format("INV-%05d", lease.getId()) : 
                invoices.get(invoices.size() - 1).getInvoiceNumber(); // Get latest invoice

            LocalDate invoiceDate = invoices.isEmpty() ? 
                LocalDate.now() : 
                invoices.get(invoices.size() - 1).getInvoiceDate();
                
            LocalDate dueDate = invoices.isEmpty() ? 
                lease.getEndDate() : 
                invoices.get(invoices.size() - 1).getDueDate();

            // Invoice Details Rows
            addTableRow(invoiceDetailsTable, "Invoice Number:", invoiceNumber);
            addTableRow(invoiceDetailsTable, "Invoice Date:", invoiceDate.format(DATE_FORMATTER));
            addTableRow(invoiceDetailsTable, "Due Date:", dueDate.format(DATE_FORMATTER));

            document.add(invoiceDetailsTable);

            // Bill To Section
            Paragraph billToHeader = new Paragraph("Bill To:")
                    .setBold()
                    .setFontSize(12)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(billToHeader);

            document.add(new Paragraph("Name: " + tenant.getFirstName() + " " + tenant.getLastName()));
            document.add(new Paragraph("Unit: " + unit.getRoomNumber())); 
            document.add(new Paragraph("Mail: " + tenant.getEmail()).setMarginBottom(20));

            // Amount Details Table
            Table amountTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                    .useAllAvailableWidth()
                    .setMarginTop(20);

            // Header
            amountTable.addHeaderCell(createHeaderCell("Description"));
            amountTable.addHeaderCell(createHeaderCell("Amount"));

            // Payments already fetched above, reuse the same list
            // Find amounts by payment type
            BigDecimal rentAmount = payments.stream()
                    .filter(p -> p.getPaymentType() == PaymentType.RENT)
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
            BigDecimal electricityAmount = payments.stream()
                    .filter(p -> p.getPaymentType() == PaymentType.ELECTRICITY)
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
            BigDecimal waterAmount = payments.stream()
                    .filter(p -> p.getPaymentType() == PaymentType.WATER)
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Rows
            addAmountRow(amountTable, "Monthly Rent", rentAmount.doubleValue());
            addAmountRow(amountTable, "Electricity", electricityAmount.doubleValue());
            addAmountRow(amountTable, "Water", waterAmount.doubleValue());

            // Total Row
            Cell totalLabelCell = new Cell()
                    .add(new Paragraph("Total"))
                    .setBold()
                    .setBackgroundColor(new DeviceRgb(230, 230, 250))
                    .setTextAlignment(TextAlignment.CENTER);
            
            BigDecimal totalAmount = rentAmount.add(electricityAmount).add(waterAmount);
            Cell totalAmountCell = new Cell()
                    .add(new Paragraph(String.format("%.2f Baht", totalAmount.doubleValue())))
                    .setBold()
                    .setBackgroundColor(new DeviceRgb(230, 230, 250))
                    .setTextAlignment(TextAlignment.RIGHT);

            amountTable.addCell(totalLabelCell);
            amountTable.addCell(totalAmountCell);

            document.add(amountTable);

            document.close();
            
            log.info("PDF generated successfully for lease ID: {}", lease.getId());
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating PDF for lease ID: {}", lease.getId(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label)));
        table.addCell(new Cell().add(new Paragraph(value)));
    }

    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(new DeviceRgb(240, 240, 240))
                .setTextAlignment(TextAlignment.CENTER);
    }

    private void addAmountRow(Table table, String description, Double amount) {
        table.addCell(new Cell().add(new Paragraph(description)));
        
        String amountText = amount != null ? String.format("%.2f Baht", amount) : "";
        table.addCell(new Cell()
                .add(new Paragraph(amountText))
                .setTextAlignment(TextAlignment.RIGHT));
    }
}
