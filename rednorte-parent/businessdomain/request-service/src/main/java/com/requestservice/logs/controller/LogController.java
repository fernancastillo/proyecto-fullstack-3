package com.requestservice.logs.controller;  

import com.requestservice.logs.entity.LogRequest;
import com.requestservice.logs.service.LogRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogRequestService logService;

    public LogController(LogRequestService logService) {
        this.logService = logService;
    }

    @GetMapping
    public ResponseEntity<List<LogRequest>> obtenerTodosLosLogs() {
        return ResponseEntity.ok(logService.obtenerTodosLosLogs());
    }

    @GetMapping("/ultimos")
    public ResponseEntity<List<LogRequest>> obtenerUltimosLogs() {
        return ResponseEntity.ok(logService.obtenerUltimosLogs());
    }

    @GetMapping("/microservicio/{nombre}")
    public ResponseEntity<List<LogRequest>> obtenerLogsPorMicroservicio(
            @PathVariable("nombre") String nombre) {
        return ResponseEntity.ok(logService.obtenerLogsPorMicroservicio(nombre));
    }

    @GetMapping("/errores")
    public ResponseEntity<List<LogRequest>> obtenerLogsConError() {
        return ResponseEntity.ok(logService.obtenerLogsConError());
    }

    @GetMapping("/estadisticas/{endpoint}")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(
        @PathVariable("endpoint") String endpoint) {
        Double tiempoPromedio = logService.obtenerTiempoPromedioEndpoint(endpoint);

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("endpoint", endpoint);
        estadisticas.put("tiempoPromedioMs", tiempoPromedio != null ? tiempoPromedio : 0);

        return ResponseEntity.ok(estadisticas);
    }

    @GetMapping("/rango")
    public ResponseEntity<List<LogRequest>> obtenerLogsPorRango(
            @RequestParam("inicio") String inicio,
            @RequestParam("fin")    String fin) {

        LocalDateTime inicioDateTime = LocalDate.parse(inicio).atStartOfDay();
        LocalDateTime finDateTime    = LocalDate.parse(fin).atTime(23, 59, 59);

        return ResponseEntity.ok(logService.obtenerLogsPorRango(inicioDateTime, finDateTime));
    }
}