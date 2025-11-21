package apartment.example.backend.controller;

import apartment.example.backend.dto.UnitAuditLogDto;
import apartment.example.backend.dto.UnitPriceHistoryDto;
import apartment.example.backend.entity.UnitAuditLog;
import apartment.example.backend.entity.UnitPriceHistory;
import apartment.example.backend.entity.enums.UnitAuditActionType;
import apartment.example.backend.service.UnitAuditLogService;
import apartment.example.backend.service.UnitPriceHistoryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class UnitPriceHistoryControllerTest {

    @Mock
    private UnitPriceHistoryService priceHistoryService;

    @Mock
    private UnitAuditLogService auditLogService;

    @InjectMocks
    private UnitPriceHistoryController controller;

    private UnitPriceHistory history;
    private UnitAuditLog audit;

    @BeforeEach
    void setUp() {
        // Mock UnitPriceHistory
        history = new UnitPriceHistory();
        history.setId(1L);

        var unit = new apartment.example.backend.entity.Unit();
        unit.setId(99L);
        unit.setRoomNumber("A101");
        history.setUnit(unit);

        history.setRentAmount(new BigDecimal("5000"));
        history.setEffectiveFrom(LocalDateTime.of(2024, 1, 1, 0, 0));
        history.setEffectiveTo(LocalDateTime.of(2024, 2, 1, 0, 0));
        history.setChangeReason("Seasonal update");
        history.setNotes("Test");
        history.setCreatedAt(LocalDateTime.now());
        history.setEffectiveTo(null);

        // Mock UnitAuditLog
        audit = new UnitAuditLog();
        audit.setId(100L);
        audit.setUnit(unit);
        audit.setActionType(UnitAuditActionType.UPDATED);
        audit.setOldValues("old");
        audit.setNewValues("new");
        audit.setDescription("desc");
        audit.setIpAddress("127.0.0.1");
        audit.setUserAgent("JUnit");
        audit.setCreatedAt(LocalDateTime.now());
    }

    // ============================================
    // PRICE HISTORY TESTS
    // ============================================

    @Test
    void testGetPriceHistory_ReturnsList() {
        when(priceHistoryService.getPriceHistoryByUnitId(99L))
                .thenReturn(List.of(history));

        ResponseEntity<List<UnitPriceHistoryDto>> response = controller.getPriceHistory(99L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(99L, response.getBody().get(0).getUnitId());

        verify(priceHistoryService, times(1))
                .getPriceHistoryByUnitId(99L);
    }

    @Test
    void testGetCurrentPrice_Found() {
        when(priceHistoryService.getCurrentPrice(99L))
                .thenReturn(Optional.of(history));

        var response = controller.getCurrentPrice(99L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(new BigDecimal("5000"), response.getBody().getRentAmount());

    }

    @Test
    void testGetCurrentPrice_NotFound() {
        when(priceHistoryService.getCurrentPrice(99L))
                .thenReturn(Optional.empty());

        var response = controller.getCurrentPrice(99L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetPriceAtDate_Found() {
        LocalDate date = LocalDate.of(2024, 1, 5);

        when(priceHistoryService.getPriceAtDate(99L, date))
                .thenReturn(Optional.of(history));

        var response = controller.getPriceAtDate(99L, date);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testGetPriceHistoryInRange() {
        LocalDate s = LocalDate.of(2024, 1, 1);
        LocalDate e = LocalDate.of(2024, 2, 1);

        when(priceHistoryService.getPriceHistoryInRange(99L, s, e))
                .thenReturn(List.of(history));

        var response = controller.getPriceHistoryInRange(99L, s, e);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetAllCurrentPrices() {
        when(priceHistoryService.getAllCurrentPrices())
                .thenReturn(List.of(history));

        var response = controller.getAllCurrentPrices();

        assertEquals(200, response.getStatusCodeValue());
    }

    // ============================================
    // AUDIT LOG TESTS
    // ============================================

    @Test
    void testGetAuditLogs_Paged() {
        Page<UnitAuditLog> mockPage = new PageImpl<>(List.of(audit));

        when(auditLogService.getAuditLogsByUnitId(eq(99L), any()))
                .thenReturn(mockPage);

        var response = controller.getAuditLogs(99L, 0, 20);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void testGetRecentAuditLogs() {
        when(auditLogService.getRecentAuditLogs(99L))
                .thenReturn(List.of(audit));

        var response = controller.getRecentAuditLogs(99L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetAuditLogsWithFilters() {
        Page<UnitAuditLog> mockPage = new PageImpl<>(List.of(audit));

        when(auditLogService.getAuditLogsWithFilters(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(mockPage);

        var response = controller.getAuditLogsWithFilters(
                99L,
                UnitAuditActionType.UPDATED,
                1L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                0,
                20
        );

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testGetPriceChangeLogs() {
        when(auditLogService.getPriceChangeLogs(99L))
                .thenReturn(List.of(audit));

        var response = controller.getPriceChangeLogs(99L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }
}
