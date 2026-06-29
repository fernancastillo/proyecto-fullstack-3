package com.waitinglistservice.integration;

import com.waitinglistservice.entity.WaitingList;
import com.waitinglistservice.exception.ResourceNotFoundException;
import com.waitinglistservice.repository.WaitingListRepository;
import com.waitinglistservice.service.WaitingListService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("[INTEGRACIÓN] WaitingListService — Gestión de lista de espera")
class WaitingListServiceIntegrationTest {

    @Autowired
    private WaitingListService waitingListService;

    @Autowired
    private WaitingListRepository waitingListRepository;

    @AfterEach
    void limpiar() {
        waitingListRepository.deleteAll();
    }

    // ───────────────────────────────────────────────────────────────────────
    // 1. CREACIÓN — Ingreso a lista de espera
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Crear entrada en lista de espera → estado EN_ESPERA y prioridad MEDIA por defecto")
    void crearEntrada_valoresPorDefecto_correctos() {
        WaitingList entrada = buildEntry(1L, 2L, "Cardiología", null, null);
        // sin status ni priority: @PrePersist debe asignarlos

        WaitingList creada = waitingListService.create(entrada);

        assertNotNull(creada.getId());
        assertEquals("EN_ESPERA", creada.getStatus(),
                "@PrePersist debe asignar status EN_ESPERA por defecto");
        assertEquals("MEDIA", creada.getPriority(),
                "@PrePersist debe asignar priority MEDIA por defecto");
        assertNotNull(creada.getRequestDate(),
                "@PrePersist debe asignar requestDate automáticamente");
    }

    @Test
    @DisplayName("Crear entrada con prioridad ALTA → prioridad se persiste correctamente")
    void crearEntrada_prioridadAlta_sePersiste() {
        WaitingList entrada = buildEntry(1L, 2L, "Traumatología", "ALTA", "EN_ESPERA");

        WaitingList creada = waitingListService.create(entrada);

        assertEquals("ALTA", creada.getPriority());
        assertEquals("EN_ESPERA", creada.getStatus());
    }

    // ───────────────────────────────────────────────────────────────────────
    // 2. CONSULTAS CRÍTICAS
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByUserId → retorna sólo entradas del paciente indicado")
    void consultarPorUserId_filtraCorrectamente() {
        waitingListService.create(buildEntry(5L, 2L, "Neurología", "ALTA", null));
        waitingListService.create(buildEntry(5L, 3L, "Pediatría", "MEDIA", null));
        waitingListService.create(buildEntry(99L, 2L, "Cardiología", "BAJA", null));

        List<WaitingList> resultado = waitingListService.getByUserId(5L);

        assertEquals(2, resultado.size(), "Debe retornar las 2 entradas del userId=5");
        assertTrue(resultado.stream().allMatch(e -> e.getUserId().equals(5L)));
    }

    @Test
    @DisplayName("getByPriority('ALTA') → retorna sólo entradas de prioridad ALTA")
    void consultarPorPrioridad_alta_filtraCorrectamente() {
        waitingListService.create(buildEntry(1L, 2L, "Neurología", "ALTA", null));
        waitingListService.create(buildEntry(2L, 3L, "Pediatría", "ALTA", null));
        waitingListService.create(buildEntry(3L, 4L, "Cardiología", "BAJA", null));

        List<WaitingList> altas = waitingListService.getByPriority("ALTA");

        assertEquals(2, altas.size());
        assertTrue(altas.stream().allMatch(e -> "ALTA".equals(e.getPriority())));
    }

    @Test
    @DisplayName("getByStatus('EN_ESPERA') → retorna sólo entradas pendientes")
    void consultarPorEstado_enEspera_filtraCorrectamente() {
        waitingListService.create(buildEntry(1L, 2L, "Neurología", "ALTA", null));
        WaitingList atendida = buildEntry(2L, 3L, "Pediatría", "MEDIA", "ATENDIDO");
        waitingListService.create(atendida);

        List<WaitingList> enEspera = waitingListService.getByStatus("EN_ESPERA");

        assertFalse(enEspera.isEmpty());
        assertTrue(enEspera.stream().allMatch(e -> "EN_ESPERA".equals(e.getStatus())));
        assertTrue(enEspera.stream().noneMatch(e -> "ATENDIDO".equals(e.getStatus())));
    }

    @Test
    @DisplayName("getBySpecialty → filtra por especialidad correctamente")
    void consultarPorEspecialidad_filtraCorrectamente() {
        waitingListService.create(buildEntry(1L, 2L, "Traumatología", "ALTA", null));
        waitingListService.create(buildEntry(2L, 3L, "Traumatología", "MEDIA", null));
        waitingListService.create(buildEntry(3L, 4L, "Neurología", "BAJA", null));

        List<WaitingList> resultado = waitingListService.getBySpecialty("Traumatología");

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(e -> "Traumatología".equals(e.getSpecialty())));
    }

    // ───────────────────────────────────────────────────────────────────────
    // 3. ACTUALIZACIÓN DE ESTADO (proceso de negocio crítico)
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Actualizar estado EN_ESPERA → ATENDIDO (paciente atendido)")
    void actualizarEstado_enEsperaAAtendido_persiste() {
        WaitingList creada = waitingListService.create(
                buildEntry(1L, 2L, "Cardiología", "ALTA", null));
        assertEquals("EN_ESPERA", creada.getStatus());

        WaitingList cambios = new WaitingList();
        cambios.setUserId(1L);
        cambios.setMedicoId(2L);
        cambios.setSpecialty("Cardiología");
        cambios.setPriority("ALTA");
        cambios.setStatus("ATENDIDO");

        WaitingList actualizada = waitingListService.update(creada.getId(), cambios);

        assertEquals("ATENDIDO", actualizada.getStatus(),
                "El estado debe cambiar a ATENDIDO");
    }

    @Test
    @DisplayName("Actualizar prioridad MEDIA → ALTA (urgencia aumenta)")
    void actualizarPrioridad_mediaAAlta_persiste() {
        WaitingList creada = waitingListService.create(
                buildEntry(1L, 2L, "Neurología", "MEDIA", null));

        WaitingList cambios = new WaitingList();
        cambios.setUserId(1L);
        cambios.setMedicoId(2L);
        cambios.setSpecialty("Neurología");
        cambios.setPriority("ALTA");
        cambios.setStatus("EN_ESPERA");

        WaitingList actualizada = waitingListService.update(creada.getId(), cambios);

        assertEquals("ALTA", actualizada.getPriority(),
                "La prioridad debe actualizarse a ALTA");
    }

    @Test
    @DisplayName("Actualizar entrada inexistente → ResourceNotFoundException")
    void actualizarEntrada_noExiste_lanzaExcepcion() {
        WaitingList cambios = buildEntry(1L, 2L, "Cardiología", "ALTA", "ATENDIDO");

        assertThrows(ResourceNotFoundException.class,
                () -> waitingListService.update(999999L, cambios));
    }

    // ───────────────────────────────────────────────────────────────────────
    // 4. ELIMINACIÓN
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Eliminar entrada existente → ya no existe en BD")
    void eliminarEntrada_existente_seEliminaCorrectamente() {
        WaitingList creada = waitingListService.create(
                buildEntry(1L, 2L, "Traumatología", "BAJA", null));

        assertDoesNotThrow(() -> waitingListService.delete(creada.getId()));

        Optional<WaitingList> eliminada = waitingListService.getById(creada.getId());
        assertTrue(eliminada.isEmpty(), "La entrada debe haber sido eliminada de la BD");
    }

    @Test
    @DisplayName("Eliminar entrada inexistente → ResourceNotFoundException")
    void eliminarEntrada_noExiste_lanzaExcepcion() {
        assertThrows(ResourceNotFoundException.class,
                () -> waitingListService.delete(999999L));
    }

    // ─── Helper ───────────────────────────────────────────────────────────
    private WaitingList buildEntry(Long userId, Long medicoId, String specialty, String priority, String status) {
        WaitingList w = new WaitingList();
        w.setUserId(userId);
        w.setMedicoId(medicoId);
        w.setSpecialty(specialty);
        if (priority != null) w.setPriority(priority);
        if (status != null) w.setStatus(status);
        return w;
    }
}
