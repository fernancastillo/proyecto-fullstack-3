package com.requestservice.service;

import com.requestservice.entity.Request;
import com.requestservice.repository.RequestRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class RequestService {

    private final RequestRepository requestRepository;

    public RequestService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public List<Request> getAllRequests() {
        return requestRepository.findAll();
    }

    public Optional<Request> getRequestById(Long id) {
        return requestRepository.findById(id);
    }


    public List<Request> getRequestsByRut(String rut) {
        return requestRepository.findByRut(rut);
    }

    public List<Request> getRequestsByEspecialidad(String especialidad) {
        return requestRepository.findByEspecialidad(especialidad);
    }

    public Request createRequest(@NonNull Request request) {
    return requestRepository.save(request);
    }

    public Request updateRequest(Long id, @NonNull Request requestDetails) {
        return requestRepository.findById(id).map(r -> {
            r.setRut(requestDetails.getRut());
            r.setDv(requestDetails.getDv());
            r.setEspecialidad(requestDetails.getEspecialidad());
            r.setDescripcion(requestDetails.getDescripcion());
            return requestRepository.save(r);
        }).orElseThrow(() -> new RuntimeException("Solicitud no encontrada con el ID: " + id));
    }

    public void deleteRequest(Long id) {
        if (!requestRepository.existsById(id)) {
            throw new RuntimeException("Solicitud no encontrada con el ID: " + id);
        }
        requestRepository.deleteById(id);
    }
}