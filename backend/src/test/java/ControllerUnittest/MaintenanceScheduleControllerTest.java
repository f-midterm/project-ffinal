package apartment.example.backend.controller;

import apartment.example.backend.dto.MaintenanceScheduleDTO;
import apartment.example.backend.dto.ScheduleAffectedUnitDTO;
import apartment.example.backend.entity.User;
import apartment.example.backend.service.MaintenanceScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MaintenanceScheduleControllerTest {

    @Mock
    private MaintenanceScheduleService scheduleService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MaintenanceScheduleController controller;

    private User mockUser;
    private MaintenanceScheduleDTO sampleSchedule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(1L);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        sampleSchedule = new MaintenanceScheduleDTO();
        sampleSchedule.setId(1L);
        sampleSchedule.setTitle("Test Schedule");
        sampleSchedule.setStartDate(LocalDate.now());
        sampleSchedule.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testGetAllActiveSchedules_Success() {
        when(scheduleService.getAllActiveSchedules()).thenReturn(Collections.singletonList(sampleSchedule));

        ResponseEntity<List<MaintenanceScheduleDTO>> response = controller.getAllActiveSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(scheduleService).getAllActiveSchedules();
    }

    @Test
    void testGetScheduleById_Success() {
        when(scheduleService.getScheduleById(1L)).thenReturn(sampleSchedule);

        ResponseEntity<MaintenanceScheduleDTO> response = controller.getScheduleById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testGetScheduleById_NotFound() {
        when(scheduleService.getScheduleById(1L)).thenThrow(new RuntimeException("Not found"));

        ResponseEntity<MaintenanceScheduleDTO> response = controller.getScheduleById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCreateSchedule_Success() {
        when(scheduleService.createSchedule(sampleSchedule, 1L)).thenReturn(sampleSchedule);

        ResponseEntity<MaintenanceScheduleDTO> response = controller.createSchedule(sampleSchedule, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Test Schedule", response.getBody().getTitle());
        verify(scheduleService).createSchedule(sampleSchedule, 1L);
    }

    @Test
    void testUpdateSchedule_Success() {
        when(scheduleService.updateSchedule(1L, sampleSchedule, 1L)).thenReturn(sampleSchedule);

        ResponseEntity<MaintenanceScheduleDTO> response = controller.updateSchedule(1L, sampleSchedule, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(scheduleService).updateSchedule(1L, sampleSchedule, 1L);
    }

    @Test
    void testUpdateSchedule_NotFound() {
        when(scheduleService.updateSchedule(1L, sampleSchedule, 1L)).thenThrow(new RuntimeException("Not found"));

        ResponseEntity<MaintenanceScheduleDTO> response = controller.updateSchedule(1L, sampleSchedule, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteSchedule_Success() {
        doNothing().when(scheduleService).deleteSchedule(1L, 1L);

        ResponseEntity<Void> response = controller.deleteSchedule(1L, authentication);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(scheduleService).deleteSchedule(1L, 1L);
    }

    // ---- Activate / Deactivate / Pause / Resume ----
    @Test
    void testActivateSchedule_Success() {
        doNothing().when(scheduleService).activateSchedule(1L, 1L);
        ResponseEntity<Void> response = controller.activateSchedule(1L, authentication);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(scheduleService).activateSchedule(1L, 1L);
    }

    @Test
    void testDeactivateSchedule_Success() {
        doNothing().when(scheduleService).deactivateSchedule(1L, 1L);
        ResponseEntity<Void> response = controller.deactivateSchedule(1L, authentication);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(scheduleService).deactivateSchedule(1L, 1L);
    }

    @Test
    void testPauseSchedule_Success() {
        doNothing().when(scheduleService).pauseSchedule(1L, 1L);
        ResponseEntity<Void> response = controller.pauseSchedule(1L, authentication);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(scheduleService).pauseSchedule(1L, 1L);
    }

    @Test
    void testResumeSchedule_Success() {
        doNothing().when(scheduleService).resumeSchedule(1L, 1L);
        ResponseEntity<Void> response = controller.resumeSchedule(1L, authentication);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(scheduleService).resumeSchedule(1L, 1L);
    }

    // ---- Trigger ----
    @Test
    void testTriggerSchedule_Success() {
        doNothing().when(scheduleService).triggerSchedule(1L);
        ResponseEntity<Void> response = controller.triggerSchedule(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(scheduleService).triggerSchedule(1L);
    }

    @Test
    void testTriggerScheduleForUnit_Success() {
        Map<String, Object> request = Map.of("unitId", 101L, "preferredDateTime", "2025-11-20T14:00:00");
        doNothing().when(scheduleService).triggerScheduleForUnit(1L, 101L, "2025-11-20T14:00:00");

        ResponseEntity<Void> response = controller.triggerScheduleForUnit(1L, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(scheduleService).triggerScheduleForUnit(1L, 101L, "2025-11-20T14:00:00");
    }

    // ---- Affected Units ----
    @Test
    void testGetAffectedUnits_Success() {
        ScheduleAffectedUnitDTO unitDTO = new ScheduleAffectedUnitDTO(101L, "101", "John", "john@example.com", "2025-11-20T14:00:00", true, false);
        when(scheduleService.getAffectedUnitsWithTimeSlots(1L)).thenReturn(Collections.singletonList(unitDTO));

        ResponseEntity<List<ScheduleAffectedUnitDTO>> response = controller.getAffectedUnits(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(101L, response.getBody().get(0).getUnitId());
    }
}
