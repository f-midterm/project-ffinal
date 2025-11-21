package apartment.example.backend.controller;

import apartment.example.backend.dto.UnitAuditLogDto;
import apartment.example.backend.entity.Unit;
import apartment.example.backend.entity.UnitAuditLog;
import apartment.example.backend.entity.User;
import apartment.example.backend.entity.enums.UnitAuditActionType;
import apartment.example.backend.service.UnitAuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnitAuditLogControllerTest {

    private UnitAuditLogService auditLogService;
    private UnitAuditLogController controller;

    private UnitAuditLog sampleLog;

    @BeforeEach
    void setUp() {
        auditLogService = mock(UnitAuditLogService.class);
        controller = new UnitAuditLogController(auditLogService);

        // สร้าง sample UnitAuditLog
        Unit unit = new Unit();
        unit.setId(10L);
        unit.setRoomNumber("A101");

        User user = new User();
        user.setUsername("admin");

        sampleLog = new UnitAuditLog();
        sampleLog.setId(1L);
        sampleLog.setUnit(unit);
        sampleLog.setActionType(UnitAuditActionType.PRICE_CHANGED);
        sampleLog.setCreatedAt(LocalDateTime.now());
        sampleLog.setCreatedBy(user);
        sampleLog.setDescription("Changed price");
    }

    @Test
    void testGetAllAuditLogs() {
        Page<UnitAuditLog> page = new PageImpl<>(List.of(sampleLog));
        when(auditLogService.getAllAuditLogs(any(Pageable.class))).thenReturn(page);

        ResponseEntity<List<UnitAuditLogDto>> response = controller.getAllAuditLogs(0, 10);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(sampleLog.getId(), response.getBody().get(0).getId());

        verify(auditLogService, times(1)).getAllAuditLogs(any(Pageable.class));
    }

    @Test
    void testGetAuditLogsByUnit() {
        Page<UnitAuditLog> page = new PageImpl<>(List.of(sampleLog));
        when(auditLogService.getAuditLogsByUnitId(eq(10L), any(Pageable.class))).thenReturn(page);

        ResponseEntity<List<UnitAuditLogDto>> response = controller.getAuditLogsByUnit(10L, 0, 10);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(10L, response.getBody().get(0).getUnitId());

        verify(auditLogService, times(1)).getAuditLogsByUnitId(eq(10L), any(Pageable.class));
    }

    @Test
    void testGetAuditLogsByActionType() {
        Page<UnitAuditLog> page = new PageImpl<>(List.of(sampleLog));
        when(auditLogService.getAuditLogsByActionType(eq(UnitAuditActionType.PRICE_CHANGED), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<List<UnitAuditLogDto>> response =
                controller.getAuditLogsByActionType(UnitAuditActionType.PRICE_CHANGED, 0, 10);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(UnitAuditActionType.PRICE_CHANGED, response.getBody().get(0).getActionType());

        verify(auditLogService, times(1))
                .getAuditLogsByActionType(eq(UnitAuditActionType.PRICE_CHANGED), any(Pageable.class));
    }

    @Test
    void testSearchAuditLogs() {
        Page<UnitAuditLog> page = new PageImpl<>(List.of(sampleLog));
        when(auditLogService.getAuditLogsWithFilters(
                eq(10L),
                eq(UnitAuditActionType.PRICE_CHANGED),
                isNull(),
                any(),
                any(),
                any(Pageable.class)
        )).thenReturn(page);

        ResponseEntity<List<UnitAuditLogDto>> response = controller.searchAuditLogs(
                10L, UnitAuditActionType.PRICE_CHANGED, null, null, null, 0, 10
        );

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }
}
