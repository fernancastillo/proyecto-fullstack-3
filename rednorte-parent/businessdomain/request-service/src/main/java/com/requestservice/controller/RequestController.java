package com.requestservice.controller;

import com.requestservice.entity.Request;
import com.requestservice.exception.ResourceNotFoundException;
import com.requestservice.service.RequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    public ResponseEntity<List<Request>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Request> getRequestById(@PathVariable("id") Long id) {
        Request request = requestService.getRequestById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con el ID: " + id));
        return ResponseEntity.ok(request);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Request>> getRequestsByUserId(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(requestService.getRequestsByUserId(userId));
    }

    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<List<Request>> getRequestsByMedicoId(@PathVariable("medicoId") Long medicoId) {
        return ResponseEntity.ok(requestService.getRequestsByMedicoId(medicoId));
    }

    @GetMapping("/especialidad/{especialidad}")
    public ResponseEntity<List<Request>> getRequestsByEspecialidad(@PathVariable("especialidad") String especialidad) {
        return ResponseEntity.ok(requestService.getRequestsByEspecialidad(especialidad));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Request>> getRequestsByEstado(@PathVariable("estado") String estado) {
        return ResponseEntity.ok(requestService.getRequestsByEstado(estado));
    }

    @PostMapping
    public ResponseEntity<Request> createRequest(@RequestBody Request request) {
        Request created = requestService.createRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Request> updateRequest(
            @PathVariable("id") Long id,
            @RequestBody Request requestDetails) {
        return ResponseEntity.ok(requestService.updateRequest(id, requestDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable("id") Long id) {
        requestService.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }
}