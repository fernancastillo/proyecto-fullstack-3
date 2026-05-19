package com.waitinglistservice.service;

import com.waitinglistservice.entity.WaitingList;
import com.waitinglistservice.exception.ResourceNotFoundException;
import com.waitinglistservice.repository.WaitingListRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class WaitingListService {

    private final WaitingListRepository repository;

    public WaitingListService(WaitingListRepository repository) {
        this.repository = repository;
    }

    public List<WaitingList> getAll() {
        return repository.findAll();
    }

    public Optional<WaitingList> getById(Long id) {
        return repository.findById(id);
    }

    public List<WaitingList> getByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    public List<WaitingList> getByMedicoId(Long medicoId) {
        return repository.findByMedicoId(medicoId);
    }

    public List<WaitingList> getBySpecialty(String specialty) {
        return repository.findBySpecialty(specialty);
    }

    public List<WaitingList> getByStatus(String status) {
        return repository.findByStatus(status);
    }

    public List<WaitingList> getByPriority(String priority) {
        return repository.findByPriority(priority);
    }

    public WaitingList create(WaitingList waitingList) {
        return repository.save(waitingList);
    }

    public WaitingList update(Long id, WaitingList details) {
        return repository.findById(id).map(existing -> {
            existing.setUserId(details.getUserId());
            existing.setMedicoId(details.getMedicoId());
            existing.setSpecialty(details.getSpecialty());
            existing.setPriority(details.getPriority());
            existing.setStatus(details.getStatus());
            return repository.save(existing);
        }).orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado con ID: " + id));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Registro no encontrado con ID: " + id);
        }
        repository.deleteById(id);
    }
}