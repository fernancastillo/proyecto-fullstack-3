package com.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userservice.entity.User;
import com.userservice.logs.service.LogRequestService;   
import com.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private LogRequestService logRequestService;   // AÑADIDO: satisface la dependencia del LoggingFilter

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User(
            1L,
            "12345678",
            "9",
            "María",
            "Medina",
            "mmedina@rednorte.cl",
            "pass123",
            "+56987654321",
            "Antofagasta",
            "Antofagasta",
            "Calle Principal 456",
            "MEDICO",
            "Cardiología"
        );
    }

    @Test
    void testGetAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(Arrays.asList(mockUser));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("María"))
                .andExpect(jsonPath("$[0].role").value("MEDICO"))
                .andExpect(jsonPath("$[0].especialidad").value("Cardiología"));
    }

    @Test
    void testGetUserById_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("mmedina@rednorte.cl"))
                .andExpect(jsonPath("$.rut").value("12345678"));
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        when(userService.getUserById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateUser() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(mockUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("María"))
                .andExpect(jsonPath("$.role").value("MEDICO"));
    }

    @Test
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetUserByRut_Success() throws Exception {
        when(userService.getUserByRut("12345678")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/users/rut/12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rut").value("12345678"));
    }

    @Test
    void testGetUserByRut_NotFound() throws Exception {
        when(userService.getUserByRut("99999999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/rut/99999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUserByEmail_Success() throws Exception {
        when(userService.getUserByEmail("mmedina@rednorte.cl")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/users/email/mmedina@rednorte.cl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("mmedina@rednorte.cl"));
    }

    @Test
    void testGetUsersByRole_RetornaListaFiltrada() throws Exception {
        when(userService.getUsersByRole("MEDICO")).thenReturn(Arrays.asList(mockUser));

        mockMvc.perform(get("/api/users/role/MEDICO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("MEDICO"));
    }

    @Test
    void testGetMedicos_RetornaListaDeMedicos() throws Exception {
        when(userService.getMedicos()).thenReturn(Arrays.asList(mockUser));

        mockMvc.perform(get("/api/users/medicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].especialidad").value("Cardiología"));
    }

    @Test
    void testUpdateUser_RetornaUsuarioActualizado() throws Exception {
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(mockUser);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("María"));
    }

    @Test
    void testUpdatePassword_Retorna204() throws Exception {
        mockMvc.perform(put("/api/users/1/password")
                .contentType(MediaType.TEXT_PLAIN)
                .content("nuevaClave123"))
                .andExpect(status().isNoContent());

        verify(userService).updatePassword(1L, "nuevaClave123");
    }

    @Test
    void testAuthenticate_CredencialesValidas_Retorna200() throws Exception {
        when(userService.authenticate("mmedina@rednorte.cl", "pass123"))
                .thenReturn(Optional.of(mockUser));

        mockMvc.perform(post("/api/users/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"mmedina@rednorte.cl\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("mmedina@rednorte.cl"));
    }

    @Test
    void testAuthenticate_CredencialesInvalidas_Retorna401() throws Exception {
        when(userService.authenticate("mmedina@rednorte.cl", "claveIncorrecta"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"mmedina@rednorte.cl\",\"password\":\"claveIncorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

}