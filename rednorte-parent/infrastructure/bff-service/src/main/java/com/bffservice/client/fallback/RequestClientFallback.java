package com.bffservice.client.fallback;

import com.bffservice.client.RequestClient;
import com.bffservice.dto.RequestDTO;
import com.bffservice.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class RequestClientFallback implements RequestClient {

    private static final String SERVICE = "solicitudes";

    @Override
    public List<RequestDTO> getAllRequests() {
        return Collections.emptyList();
    }

    @Override
    public RequestDTO getRequestById(Long id) {
        throw new ServiceUnavailableException(SERVICE);
    }

    @Override
    public List<RequestDTO> getRequestsByUserId(Long userId) {
        return Collections.emptyList();
    }

    @Override
    public List<RequestDTO> getRequestsByMedicoId(Long medicoId) {
        return Collections.emptyList();
    }

    @Override
    public List<RequestDTO> getRequestsByEspecialidad(String especialidad) {
        return Collections.emptyList();
    }

    @Override
    public List<RequestDTO> getRequestsByEstado(String estado) {
        return Collections.emptyList();
    }

    @Override
    public RequestDTO createRequest(RequestDTO request) {
        throw new ServiceUnavailableException(SERVICE);
    }

    @Override
    public RequestDTO updateRequest(Long id, RequestDTO request) {
        throw new ServiceUnavailableException(SERVICE);
    }

    @Override
    public void deleteRequest(Long id) {
        throw new ServiceUnavailableException(SERVICE);
    }
}