package com.waitinglistservice.logs.service;

import com.waitinglistservice.logs.entity.LogRequest;
import com.waitinglistservice.logs.repository.LogRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogRequestServiceTest {

    @Mock
    private LogRequestRepository repository;

    private LogRequestService service;
    private LogRequest mockLog;

    @BeforeEach
    void setUp() {
        service = new LogRequestService(repository);
        mockLog = new LogRequest();
        mockLog.setEndpoint("/requests");
        mockLog.setMetodoHttp("GET");
        mockLog.setStatus(200);
        mockLog.setMicroservicio("request-service");
        mockLog.setFecha(LocalDateTime.now());
    }

    @Test
    void guardar_persisteElLogEnElRepositorio() {
        service.guardar(mockLog);
        verify(repository, times(1)).save(mockLog);
    }

    @Test
    void obtenerTodosLosLogs_retornaTodosLosRegistros() {
        when(repository.findAll()).thenReturn(Arrays.asList(mockLog));
        List<LogRequest> resultado = service.obtenerTodosLosLogs();
        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerUltimosLogs_delegaEnFindTop20() {
        when(repository.findTop20ByOrderByFechaDesc()).thenReturn(Arrays.asList(mockLog));
        List<LogRequest> resultado = service.obtenerUltimosLogs();
        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerLogsPorMicroservicio_filtraPorNombre() {
        when(repository.findByMicroservicioOrderByFechaDesc("request-service"))
                .thenReturn(Arrays.asList(mockLog));
        List<LogRequest> resultado = service.obtenerLogsPorMicroservicio("request-service");
        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerTiempoPromedioEndpoint_delegaEnQueryPersonalizada() {
        when(repository.getTiempoPromedioByEndpoint("/requests")).thenReturn(123.45);
        Double resultado = service.obtenerTiempoPromedioEndpoint("/requests");
        assertEquals(123.45, resultado);
    }

    @Test
    void obtenerLogsConError_retornaSoloLosQueTienenErrorMensaje() {
        mockLog.setErrorMensaje("RuntimeException: fallo simulado");
        when(repository.findByErrorMensajeIsNotNullOrderByFechaDesc())
                .thenReturn(Arrays.asList(mockLog));
        List<LogRequest> resultado = service.obtenerLogsConError();
        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerLogsPorRango_delegaConFechasCorrectas() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fin = LocalDateTime.now();
        when(repository.findByFechaBetween(inicio, fin)).thenReturn(Arrays.asList(mockLog));
        List<LogRequest> resultado = service.obtenerLogsPorRango(inicio, fin);
        assertEquals(1, resultado.size());
    }
}
