package com.userservice.service;

import com.userservice.entity.User;
import com.userservice.exception.ResourceNotFoundException;
import com.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User(
            1L,
            "12345678",  // rut
            "9",         // dv
            "Juan",      // name
            "Pérez",     // lastname
            "jperez@correo.com", // email
            "pass123",   // password
            "+56912345678", // phone
            "Tarapacá",  // region
            "Iquique",   // comuna
            "Av. Arturo Prat 123", // address
            "PACIENTE",  // role
            null         // especialidad
        );
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("Juan", result.get().getName());
        assertEquals("PACIENTE", result.get().getRole());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(mockUser));

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("jperez@correo.com", result.get(0).getEmail());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testCreateUser_Success() {
        when(userRepository.existsByRut(mockUser.getRut())).thenReturn(false);
        when(userRepository.existsByEmail(mockUser.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User created = userService.createUser(mockUser);

        assertNotNull(created);
        assertEquals("PACIENTE", created.getRole());
        assertNull(created.getEspecialidad());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void testCreateUser_DuplicateRut_ThrowsException() {
        when(userRepository.existsByRut(mockUser.getRut())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(mockUser);
        });

        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateUser_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByRut(mockUser.getRut())).thenReturn(false);
        when(userRepository.existsByEmail(mockUser.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(mockUser);
        });

        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUser_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(99L, mockUser);
        });
    }

    @Test
    void testDeleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUser_NotFound_ThrowsException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(99L);
        });

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void testGetUsersByRole() {
        when(userRepository.findByRole("PACIENTE")).thenReturn(Arrays.asList(mockUser));

        List<User> result = userService.getUsersByRole("PACIENTE");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PACIENTE", result.get(0).getRole());
    }
}