package apartment.example.backend.controller;

import apartment.example.backend.entity.User;

import apartment.example.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController controller;

    private User activeUser;
    private User deletedUser;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setUsername("john");
        activeUser.setEmail("john@example.com");
        activeUser.setRole(User.Role.ADMIN);
        activeUser.setDeletedAt(null);

        deletedUser = new User();
        deletedUser.setId(2L);
        deletedUser.setUsername("deletedUser");
        deletedUser.setEmail("deleted@example.com");
        deletedUser.setRole(User.Role.USER);
        deletedUser.setDeletedAt(LocalDateTime.now());
    }

    // ============================================================
    // TEST: GET ALL USERS
    // ============================================================

    @Test
    void testGetAllUsers_ReturnActiveUsersOnly() {
        when(userRepository.findAll())
                .thenReturn(List.of(activeUser, deletedUser));

        ResponseEntity<?> response = controller.getAllUsers();

        assertEquals(200, response.getStatusCodeValue());

        List<UserController.UserDTO> list =
                (List<UserController.UserDTO>) response.getBody();

        assertEquals(1, list.size());
        assertEquals("john", list.get(0).getUsername());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetAllUsers_ExceptionThrown() {
        when(userRepository.findAll())
                .thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> response = controller.getAllUsers();

        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("DB error"));
    }

    // ============================================================
    // TEST: GET USER BY ID
    // ============================================================

    @Test
    void testGetUserById_Found() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(activeUser));

        ResponseEntity<?> response = controller.getUserById(1L);

        assertEquals(200, response.getStatusCodeValue());

        UserController.UserDTO dto =
                (UserController.UserDTO) response.getBody();

        assertEquals(1L, dto.getId());
        assertEquals("john", dto.getUsername());
        assertEquals("ADMIN", dto.getRole());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getUserById(99L);

        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("User not found"));
    }
}
