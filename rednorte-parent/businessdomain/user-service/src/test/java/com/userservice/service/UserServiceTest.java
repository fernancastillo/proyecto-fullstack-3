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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;  // AÑADIDO

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User(
            1L,
            "12345678",
            "9",
            "Juan",
            "Pérez",
            "jperez@correo.com",
            "pass123",
            "+56912345678",
            "Tarapacá",
            "Iquique",
            "Av. Arturo Prat 123",
            "PACIENTE",
            null
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
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");  // AÑADIDO
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User created = userService.createUser(mockUser);

        assertNotNull(created);
        assertEquals("PACIENTE", created.getRole());
        assertNull(created.getEspecialidad());
        verify(userRepository, times(1)).save(mockUser);
        verify(passwordEncoder, times(1)).encode(anyString());  // AÑADIDO
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

    // NUEVO TEST
    @Test
    void testAuthenticate_DisabledUser_ReturnsEmpty() {
        User disabledUser = new User(
            2L, "87654321", "K", "Ana", "López",
            "alopez@correo.com", "hashed", "+56911111111",
            "Santiago", "Santiago", "Calle Falsa 123",
            "Deshabilitado", null
        );
        when(userRepository.findByEmail("alopez@correo.com")).thenReturn(Optional.of(disabledUser));

        Optional<User> result = userService.authenticate("alopez@correo.com", "pass123");

        assertTrue(result.isEmpty(), "Un usuario Deshabilitado no debe poder autenticarse");
    }

    @Test
    void testAuthenticate_InactiveMedico_ReturnsEmpty() {
        User medicoInactivo = new User(
            3L, "11111111", "1", "Carlos", "Soto",
            "csoto@correo.com", "hashed", "+56922222222",
            "Valparaíso", "Viña del Mar", "Av. Marina 456",
            "MEDICO_INACTIVO", "Cardiología"
        );
        when(userRepository.findByEmail("csoto@correo.com")).thenReturn(Optional.of(medicoInactivo));

        Optional<User> result = userService.authenticate("csoto@correo.com", "pass123");

        assertTrue(result.isEmpty(), "Un médico MEDICO_INACTIVO no debe poder autenticarse");
    }
}