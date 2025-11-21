package apartment.example.backend.controller;

import apartment.example.backend.dto.MaintenanceNotificationDTO;
import apartment.example.backend.entity.User;
import apartment.example.backend.service.MaintenanceNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MaintenanceNotificationControllerTest {

    @Mock
    private MaintenanceNotificationService service;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MaintenanceNotificationController controller;

    private User mockUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setId(10L);
        when(authentication.getPrincipal()).thenReturn(mockUser);
    }

    @Test
    void getNotifications_success() {
        List<MaintenanceNotificationDTO> list = List.of(new MaintenanceNotificationDTO());
        when(service.getNotificationsByUserId(10L)).thenReturn(list);

        ResponseEntity<List<MaintenanceNotificationDTO>> resp = controller.getNotifications(authentication);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(list, resp.getBody());
    }

    @Test
    void getNotifications_error() {
        when(service.getNotificationsByUserId(10L)).thenThrow(new RuntimeException("err"));

        ResponseEntity<List<MaintenanceNotificationDTO>> resp = controller.getNotifications(authentication);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
    }

    @Test
    void getUnreadNotifications_success() {
        List<MaintenanceNotificationDTO> list = List.of(new MaintenanceNotificationDTO());
        when(service.getUnreadNotificationsByUserId(10L)).thenReturn(list);

        ResponseEntity<List<MaintenanceNotificationDTO>> resp = controller.getUnreadNotifications(authentication);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(list, resp.getBody());
    }

    @Test
    void getUnreadCount_success() {
        when(service.countUnreadNotifications(10L)).thenReturn(5L);

        ResponseEntity<Map<String, Long>> resp = controller.getUnreadCount(authentication);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(5L, resp.getBody().get("count"));
    }

    @Test
    void markAsRead_success() {
        ResponseEntity<Void> resp = controller.markAsRead(1L);
        verify(service, times(1)).markAsRead(1L);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void markAsRead_error() {
        doThrow(new RuntimeException()).when(service).markAsRead(1L);
        ResponseEntity<Void> resp = controller.markAsRead(1L);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
    }

    @Test
    void markAllAsRead_success() {
        ResponseEntity<Void> resp = controller.markAllAsRead(authentication);
        verify(service, times(1)).markAllAsRead(10L);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void deleteNotification_success() {
        ResponseEntity<Void> resp = controller.deleteNotification(2L, authentication);
        verify(service, times(1)).deleteNotification(2L, 10L);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void deleteNotification_error() {
        doThrow(new RuntimeException()).when(service).deleteNotification(2L, 10L);
        ResponseEntity<Void> resp = controller.deleteNotification(2L, authentication);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
    }
}
