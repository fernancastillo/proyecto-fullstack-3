package com.bffservice.client.fallback;

import com.bffservice.client.WaitingListClient;
import com.bffservice.dto.WaitingListDTO;
import com.bffservice.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class WaitingListClientFallback implements WaitingListClient {

    private static final String SERVICE = "lista de espera";

    @Override
    public List<WaitingListDTO> getAll() {
        return Collections.emptyList();
    }

    @Override
    public WaitingListDTO getById(Long id) {
        throw new ServiceUnavailableException(SERVICE);
    }

    @Override
    public List<WaitingListDTO> getByUserId(Long userId) {
        return Collections.emptyList();
    }

    @Override
    public List<WaitingListDTO> getByMedicoId(Long medicoId) {
        return Collections.emptyList();
    }

    @Override
    public List<WaitingListDTO> getBySpecialty(String specialty) {
        return Collections.emptyList();
    }

    @Override
    public List<WaitingListDTO> getByStatus(String status) {
        return Collections.emptyList();
    }

    @Override
    public WaitingListDTO create(WaitingListDTO waitingList) {
        throw new ServiceUnavailableException(SERVICE);
    }

    @Override
    public WaitingListDTO update(Long id, WaitingListDTO waitingList) {
        throw new ServiceUnavailableException(SERVICE);
    }

    @Override
    public void delete(Long id) {
        throw new ServiceUnavailableException(SERVICE);
    }
}