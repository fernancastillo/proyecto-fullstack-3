package com.requestservice.integration;

import com.requestservice.entity.Request;
import com.requestservice.exception.ResourceNotFoundException;
import com.requestservice.repository.RequestRepository;
import com.requestservice.service.RequestService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("[INTEGRACIÓN] RequestService — Gestión de solicitudes médicas")
class RequestServiceIntegrationTest {

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestRepository requestRepository;

    @AfterEach
    void limpiar() {
        requestRepository.deleteAll();
    }

    // ───────────────────────────────────────────────────────────────────────
    // 1. CREACIÓN DE SOLICITUD
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Crear solicitud → estado por defecto PENDIENTE y fecha auto-asignada")
    void crearSolicitud_estadoDefectoPendiente() {
        Request nueva = buildRequest(1L, 2L, "Cardiología", "Dolor en el pecho");
        // NO establecemos estado ni fechaSolicitud (deben asignarse por @PrePersist)

        Request creada = requestService.createRequest(nueva);

        assertNotNull(creada.getId(), "El ID debe ser asignado por la BD");
        assertEquals("PENDIENTE", creada.getEstado(),
                "@PrePersist debe asignar estado PENDIENTE por defecto");
        assertNotNull(creada.getFechaSolicitud(),
                "@PrePersist debe asignar la fecha de solicitud automáticamente");
        assertEquals("Cardiología", creada.getEspecialidad());
    }

    @Test
    @DisplayName("Crear solicitud con estado explícito CONFIRMADA → se respeta el estado dado")
    void crearSolicitud_estadoExplicito_seRespeta() {
        Request request = buildRequest(1L, 2L, "Pediatría", "Control niño");
        request.setEstado("CONFIRMADA");

        Request creada = requestService.createRequest(request);

        assertEquals("CONFIRMADA", creada.getEstado());
    }

    // ───────────────────────────────────────────────────────────────────────
    // 2. CONSULTAS CRÍTICAS
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getRequestsByUserId → retorna sólo las solicitudes del paciente indicado")
    void consultarPorUserId_retornaCorrectamente() {
        requestService.createRequest(buildRequest(10L, 2L, "Neurología", "Migrañas"));
        requestService.createRequest(buildRequest(10L, 3L, "Pediatría", "Control"));
        requestService.createRequest(buildRequest(99L, 2L, "Cardiología", "Otro paciente"));

        List<Request> resultado = requestService.getRequestsByUserId(10L);

        assertEquals(2, resultado.size(), "Debe retornar 2 solicitudes para userId=10");
        assertTrue(resultado.stream().allMatch(r -> r.getUserId().equals(10L)));
    }

    @Test
    @DisplayName("getRequestsByEstado('PENDIENTE') → retorna sólo solicitudes pendientes")
    void consultarPorEstado_pendiente_filtraCorrectamente() {
        requestService.createRequest(buildRequest(1L, 2L, "Cardiología", "Desc 1"));
        Request confirmada = buildRequest(2L, 3L, "Pediatría", "Desc 2");
        confirmada.setEstado("CONFIRMADA");
        requestService.createRequest(confirmada);

        List<Request> pendientes = requestService.getRequestsByEstado("PENDIENTE");

        assertFalse(pendientes.isEmpty(), "Debe haber solicitudes en estado PENDIENTE");
        assertTrue(pendientes.stream().allMatch(r -> "PENDIENTE".equals(r.getEstado())));
    }

    @Test
    @DisplayName("getRequestsByEspecialidad → filtra por especialidad médica")
    void consultarPorEspecialidad_filtraCorrectamente() {
        requestService.createRequest(buildRequest(1L, 2L, "Traumatología", "Fractura"));
        requestService.createRequest(buildRequest(2L, 3L, "Traumatología", "Esguince"));
        requestService.createRequest(buildRequest(3L, 4L, "Neurología", "Otro"));

        List<Request> resultado = requestService.getRequestsByEspecialidad("Traumatología");

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(r -> "Traumatología".equals(r.getEspecialidad())));
    }

    // ───────────────────────────────────────────────────────────────────────
    // 3. ACTUALIZACIÓN DE SOLICITUD (flujo de negocio crítico)
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Confirmar solicitud: PENDIENTE → CONFIRMADA con médico asignado")
    void actualizarSolicitud_confirmar_persisteNuevoEstado() {
        Request creada = requestService.createRequest(
                buildRequest(1L, 2L, "Neurología", "Dolor de cabeza"));
        assertEquals("PENDIENTE", creada.getEstado());

        // Admin confirma la solicitud y asigna médico con fecha
        Request cambios = new Request();
        cambios.setEstado("CONFIRMADA");
        cambios.setMedicoId(5L);
        cambios.setEspecialidad("Neurología");
        cambios.setDescripcion("Dolor de cabeza");
        cambios.setFechaCita(creada.getFechaSolicitud().plusDays(7));

        Request actualizada = requestService.updateRequest(creada.getId(), cambios);

        assertEquals("CONFIRMADA", actualizada.getEstado(),
                "El estado debe cambiar a CONFIRMADA");
        assertEquals(5L, actualizada.getMedicoId(),
                "El médico debe ser asignado correctamente");
        assertNotNull(actualizada.getFechaCita(), "La fecha de cita debe quedar registrada");
    }

    @Test
    @DisplayName("Cancelar solicitud: PENDIENTE → CANCELADA")
    void actualizarSolicitud_cancelar_persisteEstadoCancelada() {
        Request creada = requestService.createRequest(
                buildRequest(1L, 2L, "Cardiología", "Revisión"));

        Request cambios = new Request();
        cambios.setEstado("CANCELADA");
        cambios.setMedicoId(creada.getMedicoId());
        cambios.setEspecialidad(creada.getEspecialidad());
        cambios.setDescripcion(creada.getDescripcion());

        Request cancelada = requestService.updateRequest(creada.getId(), cambios);

        assertEquals("CANCELADA", cancelada.getEstado());
    }

    @Test
    @DisplayName("Actualizar solicitud inexistente → ResourceNotFoundException")
    void actualizarSolicitud_noExiste_lanzaExcepcion() {
        Request cambios = buildRequest(1L, 2L, "Cardiología", "Desc");

        assertThrows(ResourceNotFoundException.class,
                () -> requestService.updateRequest(999999L, cambios));
    }

    // ───────────────────────────────────────────────────────────────────────
    // 4. ELIMINACIÓN
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Eliminar solicitud existente → eliminada de BD")
    void eliminarSolicitud_existente_seEliminaCorrectamente() {
        Request creada = requestService.createRequest(
                buildRequest(1L, 2L, "Traumatología", "Esguince"));

        assertDoesNotThrow(() -> requestService.deleteRequest(creada.getId()));

        Optional<Request> eliminada = requestService.getRequestById(creada.getId());
        assertTrue(eliminada.isEmpty(), "La solicitud debe haber sido eliminada");
    }

    @Test
    @DisplayName("Eliminar solicitud inexistente → ResourceNotFoundException")
    void eliminarSolicitud_noExiste_lanzaExcepcion() {
        assertThrows(ResourceNotFoundException.class,
                () -> requestService.deleteRequest(999999L));
    }

    // ─── Helper ───────────────────────────────────────────────────────────
    private Request buildRequest(Long userId, Long medicoId, String especialidad, String descripcion) {
        Request r = new Request();
        r.setUserId(userId);
        r.setMedicoId(medicoId);
        r.setEspecialidad(especialidad);
        r.setDescripcion(descripcion);
        // estado y fechaSolicitud son asignados por @PrePersist
        return r;
    }
}
