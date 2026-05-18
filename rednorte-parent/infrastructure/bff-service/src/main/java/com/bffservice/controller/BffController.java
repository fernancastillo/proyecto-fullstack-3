package com.bffservice.controller;

import com.bffservice.client.RequestClient;
import com.bffservice.client.UserClient;
import com.bffservice.client.WaitingListClient;
import com.bffservice.client.UserLogClient;
import com.bffservice.client.RequestLogClient;
import com.bffservice.client.WaitingListLogClient;
import com.bffservice.dto.LogRequestDTO;
import com.bffservice.dto.LoginRequestDTO;
import com.bffservice.dto.RegisterRequestDTO;
import com.bffservice.dto.RequestDTO;
import com.bffservice.dto.UserDTO;
import com.bffservice.dto.WaitingListDTO;
import com.bffservice.dto.WaitingListEnrichedDTO;
import com.bffservice.exception.ServiceUnavailableException;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bff")
public class BffController {

    private final RequestClient        requestClient;
    private final WaitingListClient    waitingListClient;
    private final UserClient           userClient;
    private final UserLogClient        userLogClient;
    private final RequestLogClient     requestLogClient;
    private final WaitingListLogClient waitingListLogClient;

    public BffController(RequestClient        requestClient,
                         WaitingListClient    waitingListClient,
                         UserClient           userClient,
                         UserLogClient        userLogClient,
                         RequestLogClient     requestLogClient,
                         WaitingListLogClient waitingListLogClient) {
        this.requestClient        = requestClient;
        this.waitingListClient    = waitingListClient;
        this.userClient           = userClient;
        this.userLogClient        = userLogClient;
        this.requestLogClient     = requestLogClient;
        this.waitingListLogClient = waitingListLogClient;
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
    public ResponseEntity<?> createUser(@RequestBody RegisterRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(userClient.createUser(request));
        } catch (FeignException.Conflict e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Ya existe un usuario con ese RUT o correo electrónico."));
        } catch (FeignException.BadRequest e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Datos inválidos. Revisa los campos del formulario."));
        } catch (FeignException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al crear el usuario."));
        }
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
    public ResponseEntity<List<RequestDTO>> getRequestsByMedicoId(
            @PathVariable("medicoId") Long medicoId) {
        return ResponseEntity.ok(requestClient.getRequestsByMedicoId(medicoId));
    }

    @GetMapping("/requests/especialidad/{especialidad}")
    public ResponseEntity<List<RequestDTO>> getRequestsByEspecialidad(
            @PathVariable("especialidad") String especialidad) {
        return ResponseEntity.ok(requestClient.getRequestsByEspecialidad(especialidad));
    }

    @GetMapping("/requests/estado/{estado}")
    public ResponseEntity<List<RequestDTO>> getRequestsByEstado(
            @PathVariable("estado") String estado) {
        return ResponseEntity.ok(requestClient.getRequestsByEstado(estado));
    }

    @PostMapping("/requests")
    public ResponseEntity<RequestDTO> createRequest(@RequestBody RequestDTO request) {
        RequestDTO created = requestClient.createRequest(request);

        
        if ("PENDIENTE".equals(created.getEstado())) {
            try {
                WaitingListDTO entry = new WaitingListDTO();
                entry.setUserId(created.getUserId());
                entry.setMedicoId(created.getMedicoId());
                entry.setSpecialty(created.getEspecialidad());
                entry.setPriority("MEDIA");
                entry.setStatus("EN_ESPERA");
                waitingListClient.create(entry);
            } catch (Exception ignored) {
                
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
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

    @GetMapping("/waiting-list/enriched")
    public ResponseEntity<List<WaitingListEnrichedDTO>> getWaitingListEnriched() {
        List<WaitingListDTO> waitingList = waitingListClient.getAll();
        List<UserDTO> users = userClient.getAllUsers();

        Map<Long, UserDTO> userMap = users.stream()
                .collect(Collectors.toMap(UserDTO::getId, u -> u, (a, b) -> a));

        List<WaitingListEnrichedDTO> enriched = waitingList.stream().map(wl -> {
            UserDTO patient = userMap.get(wl.getUserId());
            UserDTO doctor  = userMap.get(wl.getMedicoId());

            String patientName     = patient != null ? patient.getName() + " " + patient.getLastname() : "Desconocido";
            String doctorName      = doctor  != null ? doctor.getName()  + " " + doctor.getLastname()  : "Desconocido";
            String doctorEspecialidad = doctor != null ? doctor.getEspecialidad() : "";

            return new WaitingListEnrichedDTO(
                    wl.getId(), wl.getUserId(), patientName,
                    wl.getMedicoId(), doctorName, doctorEspecialidad,
                    wl.getSpecialty(), wl.getPriority(), wl.getStatus(), wl.getRequestDate()
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(enriched);
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
    public ResponseEntity<WaitingListDTO> createWaitingList(
            @RequestBody WaitingListDTO waitingList) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(waitingListClient.create(waitingList));
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

    @GetMapping("/dashboard/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDashboard(
            @PathVariable("userId") Long userId) {
        UserDTO user = userClient.getUserById(userId);
        List<RequestDTO> requests = requestClient.getRequestsByUserId(userId);
        List<WaitingListDTO> waitingList = waitingListClient.getByUserId(userId);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("user", user);
        dashboard.put("requests", requests);
        dashboard.put("waitingList", waitingList);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/medico/{medicoId}")
    public ResponseEntity<Map<String, Object>> getMedicoDashboard(
            @PathVariable("medicoId") Long medicoId) {
        UserDTO medico = userClient.getUserById(medicoId);
        List<RequestDTO> citas = requestClient.getRequestsByMedicoId(medicoId);
        List<WaitingListDTO> waitingList = waitingListClient.getByMedicoId(medicoId);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("medico", medico);
        dashboard.put("citas", citas);
        dashboard.put("waitingList", waitingList);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/admin")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        List<UserDTO>        users       = userClient.getAllUsers();
        List<RequestDTO>     requests    = requestClient.getAllRequests();
        List<WaitingListDTO> waitingList = waitingListClient.getAll();
        List<UserDTO>        medicos     = userClient.getMedicos();

        long solicitudesActivas  = requests.stream()
                .filter(r -> "PENDIENTE".equals(r.getEstado()))
                .count();

        long totalEspecialidades = medicos.stream()
                .map(UserDTO::getEspecialidad)
                .filter(e -> e != null && !e.isBlank())
                .distinct()
                .count();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalUsuarios",        users.size());
        dashboard.put("totalSolicitudes",     requests.size());
        dashboard.put("totalEnEspera",        waitingList.size());
        dashboard.put("totalMedicos",         medicos.size());
        dashboard.put("solicitudesActivas",   solicitudesActivas);
        dashboard.put("totalEspecialidades",  totalEspecialidades);
        dashboard.put("usuarios",    users);
        dashboard.put("medicos",     medicos);
        dashboard.put("solicitudes", requests);
        dashboard.put("listaEspera", waitingList);

        return ResponseEntity.ok(dashboard);
    }

    // ─── LOGS ─────────────────────────────────────────────────────

    @GetMapping("/logs/rango")
    public ResponseEntity<List<LogRequestDTO>> getLogsPorRango(
            @RequestParam("inicio") String inicio,
            @RequestParam("fin")    String fin) {

        List<LogRequestDTO> todos = new ArrayList<>();

        try { todos.addAll(userLogClient.getLogsPorRango(inicio, fin)); }
        catch (Exception ignored) {}

        try { todos.addAll(requestLogClient.getLogsPorRango(inicio, fin)); }
        catch (Exception ignored) {}

        try { todos.addAll(waitingListLogClient.getLogsPorRango(inicio, fin)); }
        catch (Exception ignored) {}

        todos.sort((a, b) -> {
            if (a.getFecha() == null && b.getFecha() == null) return 0;
            if (a.getFecha() == null) return 1;
            if (b.getFecha() == null) return -1;
            return b.getFecha().compareTo(a.getFecha());
        });

        return ResponseEntity.ok(todos);
    }

    @PutMapping("/users/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body) {
        try {
            String currentPassword = body.get("currentPassword");
            String newPassword     = body.get("newPassword");

            if (currentPassword == null || currentPassword.isBlank() ||
                newPassword     == null || newPassword.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Debes ingresar la contraseña actual y la nueva."));
            }

            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "La nueva contraseña debe tener al menos 6 caracteres."));
            }

            // 1. Obtener el usuario actual
            UserDTO user = userClient.getUserById(id);

            // 2. Verificar contraseña actual usando authenticate
            LoginRequestDTO authReq = new LoginRequestDTO();
            authReq.setEmail(user.getEmail());
            authReq.setPassword(currentPassword);

            try {
                userClient.authenticate(authReq);
            } catch (FeignException.Unauthorized e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "La contraseña actual es incorrecta."));
            } catch (FeignException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "La contraseña actual es incorrecta."));
            }

            // 3. Actualizar con la nueva contraseña (el user-service la hashea)
            userClient.updatePassword(id, newPassword);

            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."));

        } catch (ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Servicio no disponible. Intenta más tarde."));
        } catch (FeignException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al cambiar la contraseña."));
        }
    }
}