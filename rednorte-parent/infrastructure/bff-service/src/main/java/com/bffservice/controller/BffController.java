package com.bffservice.controller;

import com.bffservice.client.RequestClient;
import com.bffservice.client.UserClient;
import com.bffservice.client.WaitingListClient;
import com.bffservice.dto.RequestDTO;
import com.bffservice.dto.UserDTO;
import com.bffservice.dto.WaitingListDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bff")
public class BffController {

    private final RequestClient requestClient;
    private final WaitingListClient waitingListClient;
    private final UserClient userClient;

    public BffController(RequestClient requestClient,
                         WaitingListClient waitingListClient,
                         UserClient userClient) {
        this.requestClient = requestClient;
        this.waitingListClient = waitingListClient;
        this.userClient = userClient;
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

    @GetMapping("/users/rut/{rut}")
    public ResponseEntity<UserDTO> getUserByRut(@PathVariable("rut") String rut) {
        return ResponseEntity.ok(userClient.getUserByRut(rut));
    }

    @GetMapping("/users/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable("email") String email) {
        return ResponseEntity.ok(userClient.getUserByEmail(email));
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable("role") String role) {
        return ResponseEntity.ok(userClient.getUsersByRole(role));
    }

    @GetMapping("/users/medicos")
    public ResponseEntity<List<UserDTO>> getMedicos() {
        return ResponseEntity.ok(userClient.getMedicos());
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

    // ─── REQUESTS ────────────────────────────────────────────────

    @GetMapping("/requests")
    public ResponseEntity<List<RequestDTO>> getAllRequests() {
        return ResponseEntity.ok(requestClient.getAllRequests());
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<RequestDTO> getRequestById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(requestClient.getRequestById(id));
    }

    @GetMapping("/requests/user/{userId}")
    public ResponseEntity<List<RequestDTO>> getRequestsByUserId(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(requestClient.getRequestsByUserId(userId));
    }

    @GetMapping("/requests/medico/{medicoId}")
    public ResponseEntity<List<RequestDTO>> getRequestsByMedicoId(@PathVariable("medicoId") Long medicoId) {
        return ResponseEntity.ok(requestClient.getRequestsByMedicoId(medicoId));
    }

    @GetMapping("/requests/especialidad/{especialidad}")
    public ResponseEntity<List<RequestDTO>> getRequestsByEspecialidad(
            @PathVariable("especialidad") String especialidad) {
        return ResponseEntity.ok(requestClient.getRequestsByEspecialidad(especialidad));
    }

    @GetMapping("/requests/estado/{estado}")
    public ResponseEntity<List<RequestDTO>> getRequestsByEstado(@PathVariable("estado") String estado) {
        return ResponseEntity.ok(requestClient.getRequestsByEstado(estado));
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

    @GetMapping("/waiting-list/user/{userId}")
    public ResponseEntity<List<WaitingListDTO>> getWaitingListByUserId(
            @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(waitingListClient.getByUserId(userId));
    }

    @GetMapping("/waiting-list/medico/{medicoId}")
    public ResponseEntity<List<WaitingListDTO>> getWaitingListByMedicoId(
            @PathVariable("medicoId") Long medicoId) {
        return ResponseEntity.ok(waitingListClient.getByMedicoId(medicoId));
    }

    @GetMapping("/waiting-list/specialty/{specialty}")
    public ResponseEntity<List<WaitingListDTO>> getWaitingListBySpecialty(
            @PathVariable("specialty") String specialty) {
        return ResponseEntity.ok(waitingListClient.getBySpecialty(specialty));
    }

    @GetMapping("/waiting-list/status/{status}")
    public ResponseEntity<List<WaitingListDTO>> getWaitingListByStatus(
            @PathVariable("status") String status) {
        return ResponseEntity.ok(waitingListClient.getByStatus(status));
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

    // ─── ENDPOINTS COMBINADOS ─────────────────────────────────────

    // Dashboard del paciente: datos del usuario + sus solicitudes + su lista de espera
    @GetMapping("/dashboard/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDashboard(@PathVariable("userId") Long userId) {
        UserDTO user = userClient.getUserById(userId);
        List<RequestDTO> requests = requestClient.getRequestsByUserId(userId);
        List<WaitingListDTO> waitingList = waitingListClient.getByUserId(userId);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("user", user);
        dashboard.put("requests", requests);
        dashboard.put("waitingList", waitingList);

        return ResponseEntity.ok(dashboard);
    }

    // Dashboard del médico: datos del médico + citas asignadas + lista de espera a su cargo
    @GetMapping("/dashboard/medico/{medicoId}")
    public ResponseEntity<Map<String, Object>> getMedicoDashboard(@PathVariable("medicoId") Long medicoId) {
        UserDTO medico = userClient.getUserById(medicoId);
        List<RequestDTO> citas = requestClient.getRequestsByMedicoId(medicoId);
        List<WaitingListDTO> waitingList = waitingListClient.getByMedicoId(medicoId);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("medico", medico);
        dashboard.put("citas", citas);
        dashboard.put("waitingList", waitingList);

        return ResponseEntity.ok(dashboard);
    }

    // Dashboard del admin: todos los usuarios, solicitudes y lista de espera
    @GetMapping("/dashboard/admin")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        List<UserDTO> users = userClient.getAllUsers();
        List<RequestDTO> requests = requestClient.getAllRequests();
        List<WaitingListDTO> waitingList = waitingListClient.getAll();
        List<UserDTO> medicos = userClient.getMedicos();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalUsuarios", users.size());
        dashboard.put("totalSolicitudes", requests.size());
        dashboard.put("totalEnEspera", waitingList.size());
        dashboard.put("totalMedicos", medicos.size());
        dashboard.put("usuarios", users);
        dashboard.put("medicos", medicos);
        dashboard.put("solicitudes", requests);
        dashboard.put("listaEspera", waitingList);

        return ResponseEntity.ok(dashboard);
    }
}