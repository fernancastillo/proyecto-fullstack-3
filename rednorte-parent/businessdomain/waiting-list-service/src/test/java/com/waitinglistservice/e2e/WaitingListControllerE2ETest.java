package com.waitinglistservice.e2e;

import com.waitinglistservice.entity.WaitingList;
import com.waitinglistservice.repository.WaitingListRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("[E2E] WaitingListController — Flujo HTTP completo de lista de espera")
class WaitingListControllerE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WaitingListRepository waitingListRepository;

    @AfterEach
    void limpiar() {
        waitingListRepository.deleteAll();
    }

    // ───────────────────────────────────────────────────────────────────────
    // 1. POST /waiting-list — Ingresar a la lista de espera
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /waiting-list → 201 Created con estado EN_ESPERA y prioridad MEDIA por defecto")
    void ingresarListaEspera_devuelve201ConValoresPorDefecto() {
        WaitingList payload = buildEntry(1L, 2L, "Cardiología", null, null);

        ResponseEntity<WaitingList> response = restTemplate.postForEntity(
                "/waiting-list", payload, WaitingList.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getId());
        assertEquals("EN_ESPERA", response.getBody().getStatus(),
                "@PrePersist debe asignar EN_ESPERA por defecto");
        assertEquals("MEDIA", response.getBody().getPriority(),
                "@PrePersist debe asignar MEDIA por defecto");
        assertNotNull(response.getBody().getRequestDate());
    }

    @Test
    @DisplayName("POST /waiting-list con prioridad ALTA → se persiste la prioridad indicada")
    void ingresarConPrioridadAlta_persistePrioridad() {
        WaitingList payload = buildEntry(1L, 2L, "Traumatología", "ALTA", null);

        ResponseEntity<WaitingList> response = restTemplate.postForEntity(
                "/waiting-list", payload, WaitingList.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("ALTA", response.getBody().getPriority());
    }

    // ───────────────────────────────────────────────────────────────────────
    // 2. GET /waiting-list — Listar entradas
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /waiting-list → 200 OK con lista de todas las entradas")
    void listarTodasLasEntradas_devuelve200() {
        restTemplate.postForEntity("/waiting-list", buildEntry(1L, 2L, "Neurología", "ALTA", null), WaitingList.class);
        restTemplate.postForEntity("/waiting-list", buildEntry(2L, 3L, "Pediatría", "MEDIA", null), WaitingList.class);

        ResponseEntity<WaitingList[]> response = restTemplate.getForEntity(
                "/waiting-list", WaitingList[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().length >= 2);
    }

    // ───────────────────────────────────────────────────────────────────────
    // 3. GET /waiting-list/{id}
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /waiting-list/{id} existente → 200 OK con datos de la entrada")
    void obtenerPorId_existente_devuelve200() {
        ResponseEntity<WaitingList> created = restTemplate.postForEntity(
                "/waiting-list", buildEntry(3L, 4L, "Neurología", "BAJA", null), WaitingList.class);
        Long id = created.getBody().getId();

        ResponseEntity<WaitingList> response = restTemplate.getForEntity(
                "/waiting-list/" + id, WaitingList.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Neurología", response.getBody().getSpecialty());
        assertEquals(3L, response.getBody().getUserId());
    }

    @Test
    @DisplayName("GET /waiting-list/{id} inexistente → 404 Not Found")
    void obtenerPorId_inexistente_devuelve404() {
        ResponseEntity<Void> response = restTemplate.getForEntity(
                "/waiting-list/999999", Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ───────────────────────────────────────────────────────────────────────
    // 4. GET /waiting-list/user/{userId} y /status/{status}
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /waiting-list/user/{userId} → filtra entradas por paciente")
    void obtenerPorUserId_devuelveListaDelPaciente() {
        restTemplate.postForEntity("/waiting-list", buildEntry(8L, 2L, "Neurología", "ALTA", null), WaitingList.class);
        restTemplate.postForEntity("/waiting-list", buildEntry(8L, 3L, "Cardiología", "MEDIA", null), WaitingList.class);
        restTemplate.postForEntity("/waiting-list", buildEntry(99L, 2L, "Pediatría", "BAJA", null), WaitingList.class);

        ResponseEntity<WaitingList[]> response = restTemplate.getForEntity(
                "/waiting-list/user/8", WaitingList[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().length);
        for (WaitingList entry : response.getBody()) {
            assertEquals(8L, entry.getUserId());
        }
    }

    @Test
    @DisplayName("GET /waiting-list/status/EN_ESPERA → filtra por estado")
    void obtenerPorEstado_enEspera_filtraCorrectamente() {
        restTemplate.postForEntity("/waiting-list", buildEntry(1L, 2L, "Cardiología", "ALTA", null), WaitingList.class);
        restTemplate.postForEntity("/waiting-list", buildEntry(2L, 3L, "Pediatría", "MEDIA", "ATENDIDO"), WaitingList.class);

        ResponseEntity<WaitingList[]> response = restTemplate.getForEntity(
                "/waiting-list/status/EN_ESPERA", WaitingList[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        for (WaitingList entry : response.getBody()) {
            assertEquals("EN_ESPERA", entry.getStatus());
        }
    }

    @Test
    @DisplayName("GET /waiting-list/priority/ALTA → filtra entradas de máxima prioridad")
    void obtenerPorPrioridad_alta_filtraCorrectamente() {
        restTemplate.postForEntity("/waiting-list", buildEntry(1L, 2L, "Neurología", "ALTA", null), WaitingList.class);
        restTemplate.postForEntity("/waiting-list", buildEntry(2L, 3L, "Cardiología", "BAJA", null), WaitingList.class);

        ResponseEntity<WaitingList[]> response = restTemplate.getForEntity(
                "/waiting-list/priority/ALTA", WaitingList[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().length);
        assertEquals("ALTA", response.getBody()[0].getPriority());
    }

    // ───────────────────────────────────────────────────────────────────────
    // 5. PUT /waiting-list/{id} — Actualización de estado / prioridad
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /waiting-list/{id} → marcar paciente como ATENDIDO")
    void marcarComoAtendido_estadoCambiaCorrectamente() {
        ResponseEntity<WaitingList> created = restTemplate.postForEntity(
                "/waiting-list", buildEntry(1L, 2L, "Cardiología", "ALTA", null), WaitingList.class);
        Long id = created.getBody().getId();

        WaitingList actualizacion = new WaitingList();
        actualizacion.setUserId(1L);
        actualizacion.setMedicoId(2L);
        actualizacion.setSpecialty("Cardiología");
        actualizacion.setPriority("ALTA");
        actualizacion.setStatus("ATENDIDO");

        ResponseEntity<WaitingList> response = restTemplate.exchange(
                "/waiting-list/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(actualizacion),
                WaitingList.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ATENDIDO", response.getBody().getStatus());
    }

    @Test
    @DisplayName("PUT /waiting-list/{id} → escalar prioridad a ALTA (urgencia)")
    void escalarPrioridad_cambiaCorrectamente() {
        ResponseEntity<WaitingList> created = restTemplate.postForEntity(
                "/waiting-list", buildEntry(1L, 2L, "Neurología", "BAJA", null), WaitingList.class);
        Long id = created.getBody().getId();

        WaitingList actualizacion = new WaitingList();
        actualizacion.setUserId(1L);
        actualizacion.setMedicoId(2L);
        actualizacion.setSpecialty("Neurología");
        actualizacion.setPriority("ALTA");
        actualizacion.setStatus("EN_ESPERA");

        ResponseEntity<WaitingList> response = restTemplate.exchange(
                "/waiting-list/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(actualizacion),
                WaitingList.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ALTA", response.getBody().getPriority());
    }

    @Test
    @DisplayName("PUT /waiting-list/{id} inexistente → 404 Not Found")
    void actualizarEntrada_inexistente_devuelve404() {
        WaitingList cambios = buildEntry(1L, 2L, "Cardiología", "ALTA", "ATENDIDO");

        ResponseEntity<Void> response = restTemplate.exchange(
                "/waiting-list/999999",
                HttpMethod.PUT,
                new HttpEntity<>(cambios),
                Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ───────────────────────────────────────────────────────────────────────
    // 6. DELETE /waiting-list/{id}
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /waiting-list/{id} existente → 204 No Content y entrada eliminada")
    void eliminarEntrada_existente_devuelve204() {
        ResponseEntity<WaitingList> created = restTemplate.postForEntity(
                "/waiting-list", buildEntry(1L, 2L, "Traumatología", "MEDIA", null), WaitingList.class);
        Long id = created.getBody().getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/waiting-list/" + id, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verificar que ya no existe
        ResponseEntity<Void> check = restTemplate.getForEntity("/waiting-list/" + id, Void.class);
        assertEquals(HttpStatus.NOT_FOUND, check.getStatusCode());
    }

    @Test
    @DisplayName("DELETE /waiting-list/{id} inexistente → 404 Not Found")
    void eliminarEntrada_inexistente_devuelve404() {
        ResponseEntity<Void> response = restTemplate.exchange(
                "/waiting-list/999999", HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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
