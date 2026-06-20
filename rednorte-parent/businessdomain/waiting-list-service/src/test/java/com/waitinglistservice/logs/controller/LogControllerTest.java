package com.waitinglistservice.logs.controller;

import com.waitinglistservice.logs.entity.LogRequest;
import com.waitinglistservice.logs.service.LogRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LogController.class)
class LogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LogRequestService logRequestService;

    private LogRequest mockLog;

    @BeforeEach
    void setUp() {
        mockLog = new LogRequest();
        mockLog.setId(1L);
        mockLog.setEndpoint("/requests");
        mockLog.setMetodoHttp("GET");
        mockLog.setStatus(200);
        mockLog.setTiempoRespuesta(45L);
        mockLog.setMicroservicio("request-service");
        mockLog.setFecha(LocalDateTime.now());
    }

    @Test
    void obtenerTodosLosLogs_retorna200ConLista() throws Exception {
        when(logRequestService.obtenerTodosLosLogs()).thenReturn(Arrays.asList(mockLog));

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].endpoint").value("/requests"));
    }

    @Test
    void obtenerUltimosLogs_retorna200ConLista() throws Exception {
        when(logRequestService.obtenerUltimosLogs()).thenReturn(Arrays.asList(mockLog));

        mockMvc.perform(get("/api/logs/ultimos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].metodoHttp").value("GET"));
    }

    @Test
    void obtenerLogsPorMicroservicio_filtraPorNombre() throws Exception {
        when(logRequestService.obtenerLogsPorMicroservicio("request-service"))
                .thenReturn(Arrays.asList(mockLog));

        mockMvc.perform(get("/api/logs/microservicio/request-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].microservicio").value("request-service"));
    }

    @Test
    void obtenerLogsConError_retorna200ConLista() throws Exception {
        mockLog.setErrorMensaje("Error simulado");
        when(logRequestService.obtenerLogsConError()).thenReturn(Arrays.asList(mockLog));

        mockMvc.perform(get("/api/logs/errores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].errorMensaje").value("Error simulado"));
    }

    @Test
    void obtenerEstadisticas_retornaTiempoPromedio() throws Exception {
        when(logRequestService.obtenerTiempoPromedioEndpoint("requests")).thenReturn(150.0);

        mockMvc.perform(get("/api/logs/estadisticas/{endpoint}", "requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endpoint").value("requests"))
                .andExpect(jsonPath("$.tiempoPromedioMs").value(150.0));
    }

    @Test
    void obtenerEstadisticas_sinDatos_devuelveCero() throws Exception {
        when(logRequestService.obtenerTiempoPromedioEndpoint("inexistente")).thenReturn(null);

        mockMvc.perform(get("/api/logs/estadisticas/{endpoint}", "inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tiempoPromedioMs").value(0));
    }

    @Test
    void obtenerLogsPorRango_parseaFechasYDelega() throws Exception {
        when(logRequestService.obtenerLogsPorRango(any(), any())).thenReturn(Arrays.asList(mockLog));

        mockMvc.perform(get("/api/logs/rango")
                .param("inicio", "2026-06-01")
                .param("fin", "2026-06-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].endpoint").value("/requests"));
    }
}
