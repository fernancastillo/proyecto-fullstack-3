package com.waitinglistservice.controller;

import com.waitinglistservice.entity.WaitingList;
import com.waitinglistservice.exception.ResourceNotFoundException;
import com.waitinglistservice.service.WaitingListService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/waiting-list")
public class WaitingListController {

    private final WaitingListService service;

    public WaitingListController(WaitingListService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<WaitingList>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WaitingList> getById(@PathVariable("id") Long id) {
        WaitingList waitingList = service.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado con el ID: " + id));
        return ResponseEntity.ok(waitingList);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WaitingList>> getByUserId(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<List<WaitingList>> getByMedicoId(@PathVariable("medicoId") Long medicoId) {
        return ResponseEntity.ok(service.getByMedicoId(medicoId));
    }

    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<WaitingList>> getBySpecialty(@PathVariable("specialty") String specialty) {
        return ResponseEntity.ok(service.getBySpecialty(specialty));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<WaitingList>> getByStatus(@PathVariable("status") String status) {
        return ResponseEntity.ok(service.getByStatus(status));
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<WaitingList>> getByPriority(@PathVariable("priority") String priority) {
        return ResponseEntity.ok(service.getByPriority(priority));
    }

    @PostMapping
    public ResponseEntity<WaitingList> create(@RequestBody WaitingList waitingList) {
        WaitingList created = service.create(waitingList);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WaitingList> update(
            @PathVariable("id") Long id,
            @RequestBody WaitingList details) {
        return ResponseEntity.ok(service.update(id, details));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}