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
}