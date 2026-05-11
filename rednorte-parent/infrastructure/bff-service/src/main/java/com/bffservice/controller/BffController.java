package com.bffservice.controller;

import com.bffservice.client.PatientClient;
import com.bffservice.client.RequestClient;
import com.bffservice.client.UserClient;
import com.bffservice.client.WaitingListClient;
import com.bffservice.dto.PatientDTO;
import com.bffservice.dto.RequestDTO;
import com.bffservice.dto.UserDTO;
import com.bffservice.dto.WaitingListDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/bff")
public class BffController {

    private final PatientClient patientClient;
    private final RequestClient requestClient;
    private final WaitingListClient waitingListClient;
    private final UserClient userClient;

    public BffController(PatientClient patientClient,
                         RequestClient requestClient,
                         WaitingListClient waitingListClient,
                         UserClient userClient) {
        this.patientClient = patientClient;
        this.requestClient = requestClient;
        this.waitingListClient = waitingListClient;
        this.userClient = userClient;
    }

    // ─── PATIENTS ────────────────────────────────────────────────

    @GetMapping("/patients")
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        return ResponseEntity.ok(patientClient.getAllPatients());
    }

    @GetMapping("/patients/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(patientClient.getPatientById(id));
    }

    @GetMapping("/patients/rut/{rut}")
    public ResponseEntity<PatientDTO> getPatientByRut(@PathVariable("rut") String rut) {
        return ResponseEntity.ok(patientClient.getPatientByRut(rut));
    }

    @PostMapping("/patients")
    public ResponseEntity<PatientDTO> createPatient(@RequestBody PatientDTO patient) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientClient.createPatient(patient));
    }

    @PutMapping("/patients/{id}")
    public ResponseEntity<PatientDTO> updatePatient(@PathVariable("id") Long id,
                                                     @RequestBody PatientDTO patient) {
        return ResponseEntity.ok(patientClient.updatePatient(id, patient));
    }

    @DeleteMapping("/patients/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable("id") Long id) {
        patientClient.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    // ─── REQUESTS ────────────────────────────────────────────────

    @GetMapping("/requests")
    public ResponseEntity<List<RequestDTO>> getAllRequests() {
        return ResponseEntity.ok(requestClient.getAllRequests());
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<RequestDTO> getRequestById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(requestClient.getRequestById(id));
    }

    @GetMapping("/requests/rut/{rut}")
    public ResponseEntity<List<RequestDTO>> getRequestsByRut(@PathVariable("rut") String rut) {
        return ResponseEntity.ok(requestClient.getRequestsByRut(rut));
    }

    @PostMapping("/requests")
    public ResponseEntity<RequestDTO> createRequest(@RequestBody RequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(requestClient.createRequest(request));
    }

    @PutMapping("/requests/{id}")
    public ResponseEntity<RequestDTO> updateRequest(@PathVariable("id") Long id,
                                                     @RequestBody RequestDTO request) {
        return ResponseEntity.ok(requestClient.updateRequest(id, request));
    }

    @DeleteMapping("/requests/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable("id") Long id) {
        requestClient.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }

    // ─── WAITING LIST ─────────────────────────────────────────────

    @GetMapping("/waiting-list")
    public ResponseEntity<List<WaitingListDTO>> getAllWaitingList() {
        return ResponseEntity.ok(waitingListClient.getAll());
    }

    @GetMapping("/waiting-list/{id}")
    public ResponseEntity<WaitingListDTO> getWaitingListById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(waitingListClient.getById(id));
    }

    @GetMapping("/waiting-list/patient/{patientId}")
    public ResponseEntity<List<WaitingListDTO>> getWaitingListByPatientId(
            @PathVariable("patientId") Long patientId) {
        return ResponseEntity.ok(waitingListClient.getByPatientId(patientId));
    }

    @PostMapping("/waiting-list")
    public ResponseEntity<WaitingListDTO> createWaitingList(@RequestBody WaitingListDTO waitingList) {
        return ResponseEntity.status(HttpStatus.CREATED).body(waitingListClient.create(waitingList));
    }

    @PutMapping("/waiting-list/{id}")
    public ResponseEntity<WaitingListDTO> updateWaitingList(@PathVariable("id") Long id,
                                                             @RequestBody WaitingListDTO waitingList) {
        return ResponseEntity.ok(waitingListClient.update(id, waitingList));
    }

    @DeleteMapping("/waiting-list/{id}")
    public ResponseEntity<Void> deleteWaitingList(@PathVariable("id") Long id) {
        waitingListClient.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── USERS ───────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userClient.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userClient.getUserById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userClient.createUser(user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable("id") Long id,
                                               @RequestBody UserDTO user) {
        return ResponseEntity.ok(userClient.updateUser(id, user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        userClient.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ─── ENDPOINT COMBINADO ───────────────────────────────────────
    // Este endpoint es el que justifica el uso del patrón BFF,
    // ya que agrega datos de múltiples microservicios en una sola respuesta

    @GetMapping("/dashboard/patient/{rut}")
    public ResponseEntity<?> getPatientDashboard(@PathVariable("rut") String rut) {
        PatientDTO patient = patientClient.getPatientByRut(rut);
        List<RequestDTO> requests = requestClient.getRequestsByRut(rut);
        List<WaitingListDTO> waitingList = waitingListClient.getByPatientId(patient.getId());

        return ResponseEntity.ok(new java.util.HashMap<>() {{
            put("patient", patient);
            put("requests", requests);
            put("waitingList", waitingList);
        }});
    }
}