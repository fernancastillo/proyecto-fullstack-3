package com.userservice.logs.controller;

import com.userservice.logs.entity.LogRequest;
import com.userservice.logs.service.LogRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
            @PathVariable String nombre) {
        return ResponseEntity.ok(logService.obtenerLogsPorMicroservicio(nombre));
    }

    @GetMapping("/estadisticas/{endpoint}")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(
            @PathVariable String endpoint) {
        Double tiempoPromedio = logService.obtenerTiempoPromedioEndpoint(endpoint);

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("endpoint", endpoint);
        estadisticas.put("tiempoPromedioMs", tiempoPromedio != null ? tiempoPromedio : 0);

        return ResponseEntity.ok(estadisticas);
    }
}
