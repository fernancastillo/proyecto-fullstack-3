package com.waitinglistservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waitinglistservice.entity.WaitingList;
import com.waitinglistservice.service.WaitingListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WaitingListController.class)
public class WaitingListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WaitingListService waitingListService;

    @Autowired
    private ObjectMapper objectMapper;

    private WaitingList mockWaitingList;

    @BeforeEach
    void setUp() {
        // Simulamos un registro en la lista de espera
        mockWaitingList = new WaitingList(
                1L, 
                1001L, // ID de un paciente ficticio
                "Cardiología", 
                "ALTA", 
                "EN_ESPERA", 
                LocalDateTime.now()
        );
    }

    @Test
    void testGetAll() throws Exception {
        when(waitingListService.getAll()).thenReturn(Arrays.asList(mockWaitingList));

        mockMvc.perform(get("/api/waiting-list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].specialty").value("Cardiología"))
                .andExpect(jsonPath("$[0].priority").value("ALTA"));
    }

    @Test
    void testGetById_Success() throws Exception {
        when(waitingListService.getById(1L)).thenReturn(Optional.of(mockWaitingList));

        mockMvc.perform(get("/api/waiting-list/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EN_ESPERA"))
                .andExpect(jsonPath("$.patientId").value(1001));
    }

    @Test
    void testGetByPatientId() throws Exception {
        when(waitingListService.getByPatientId(1001L)).thenReturn(Arrays.asList(mockWaitingList));

        mockMvc.perform(get("/api/waiting-list/patient/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].specialty").value("Cardiología"));
    }

    @Test
    void testCreate() throws Exception {
        when(waitingListService.create(any(WaitingList.class))).thenReturn(mockWaitingList);

        mockMvc.perform(post("/api/waiting-list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockWaitingList)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.priority").value("ALTA"))
                .andExpect(jsonPath("$.specialty").value("Cardiología"));
    }
}
