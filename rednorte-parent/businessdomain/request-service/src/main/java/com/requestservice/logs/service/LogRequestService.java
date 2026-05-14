package com.requestservice.logs.service;

import com.requestservice.logs.entity.LogRequest;
import com.requestservice.logs.repository.LogRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class LogRequestService {

    private final LogRequestRepository repository;

    public LogRequestService(LogRequestRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void guardar(LogRequest log) {
        repository.save(log);
    }

    public List<LogRequest> obtenerTodosLosLogs() {
        return repository.findAll();
    }

    public List<LogRequest> obtenerUltimosLogs() {
        return repository.findTop20ByOrderByFechaDesc();
    }

    public List<LogRequest> obtenerLogsPorMicroservicio(String microservicio) {
        return repository.findByMicroservicioOrderByFechaDesc(microservicio);
    }

    public Double obtenerTiempoPromedioEndpoint(String endpoint) {
        return repository.getTiempoPromedioByEndpoint(endpoint);
    }
}
