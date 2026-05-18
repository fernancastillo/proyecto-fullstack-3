package com.waitinglistservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waitinglistservice.entity.WaitingList;
import com.waitinglistservice.logs.service.LogRequestService;
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

    @MockitoBean
    private LogRequestService logRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    private WaitingList mockEntry;

    @BeforeEach
    void setUp() {
        mockEntry = new WaitingList(
            1L, 1L, 2L, "Pediatría",
            "MEDIA", "EN_ESPERA", LocalDateTime.now()
        );
    }

    @Test
    void testGetAll() throws Exception {
        when(waitingListService.getAll()).thenReturn(Arrays.asList(mockEntry));

        mockMvc.perform(get("/waiting-list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].specialty").value("Pediatría"))
                .andExpect(jsonPath("$[0].priority").value("MEDIA"));
    }

    @Test
    void testGetById_Success() throws Exception {
        when(waitingListService.getById(1L)).thenReturn(Optional.of(mockEntry));

        mockMvc.perform(get("/waiting-list/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty").value("Pediatría"));
    }

    @Test
    void testGetById_NotFound() throws Exception {
        when(waitingListService.getById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/waiting-list/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {
        when(waitingListService.create(any(WaitingList.class))).thenReturn(mockEntry);

        mockMvc.perform(post("/waiting-list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockEntry)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.specialty").value("Pediatría"));
    }

    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/waiting-list/1"))
                .andExpect(status().isNoContent());
    }
}