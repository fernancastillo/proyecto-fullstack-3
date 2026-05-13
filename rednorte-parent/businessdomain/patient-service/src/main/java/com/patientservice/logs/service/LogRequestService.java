package com.patientservice.logs.service;

import com.patientservice.logs.entity.LogRequest;
import com.patientservice.logs.repository.LogRequestRepository;
import org.springframework.stereotype.Service;

@Service
public class LogRequestService {

    private final LogRequestRepository repository;

    public LogRequestService(LogRequestRepository repository) {
        this.repository = repository;
    }

    public void guardar(LogRequest log) {
        repository.save(log);
    }
}