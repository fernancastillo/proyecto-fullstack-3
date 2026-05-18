package com.requestservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.requestservice.entity.Request;
import com.requestservice.logs.service.LogRequestService;
import com.requestservice.service.RequestService;
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

@WebMvcTest(RequestController.class)
public class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RequestService requestService;

    @MockitoBean
    private LogRequestService logRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    private Request mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = new Request(
            1L, 1L, 2L, "Neurología",
            "Dolor de cabeza frecuente", "PENDIENTE",
            LocalDateTime.now(), null
        );
    }

    @Test
    void testGetAllRequests() throws Exception {
        when(requestService.getAllRequests()).thenReturn(Arrays.asList(mockRequest));

        mockMvc.perform(get("/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].especialidad").value("Neurología"))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    void testGetRequestById_Success() throws Exception {
        when(requestService.getRequestById(1L)).thenReturn(Optional.of(mockRequest));

        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especialidad").value("Neurología"));
    }

    @Test
    void testGetRequestById_NotFound() throws Exception {
        when(requestService.getRequestById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/requests/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateRequest() throws Exception {
        when(requestService.createRequest(any(Request.class))).thenReturn(mockRequest);

        mockMvc.perform(post("/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.especialidad").value("Neurología"));
    }

    @Test
    void testDeleteRequest() throws Exception {
        mockMvc.perform(delete("/requests/1"))
                .andExpect(status().isNoContent());
    }
}