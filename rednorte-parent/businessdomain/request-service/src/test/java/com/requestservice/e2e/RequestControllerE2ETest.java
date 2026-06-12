package com.requestservice.e2e;

import com.requestservice.entity.Request;
import com.requestservice.repository.RequestRepository;
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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("[E2E] RequestController — Flujo HTTP completo de solicitudes médicas")
class RequestControllerE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RequestRepository requestRepository;

    @AfterEach
    void limpiar() {
        requestRepository.deleteAll();
    }

    // ───────────────────────────────────────────────────────────────────────
    // 1. POST /requests — Crear solicitud
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /requests → 201 Created con solicitud en estado PENDIENTE")
    void crearSolicitud_devuelve201ConEstadoPendiente() {
        Request payload = buildRequest(1L, 2L, "Cardiología", "Palpitaciones frecuentes");

        ResponseEntity<Request> response = restTemplate.postForEntity("/requests", payload, Request.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId(), "El ID debe ser asignado automáticamente");
        assertEquals("PENDIENTE", response.getBody().getEstado(),
                "@PrePersist debe asignar estado PENDIENTE");
        assertNotNull(response.getBody().getFechaSolicitud(),
                "@PrePersist debe asignar la fecha de solicitud");
        assertEquals("Cardiología", response.getBody().getEspecialidad());
    }

    // ───────────────────────────────────────────────────────────────────────
    // 2. GET /requests — Listar solicitudes
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /requests → 200 OK con lista de solicitudes")
    void listarSolicitudes_devuelve200ConLista() {
        restTemplate.postForEntity("/requests", buildRequest(1L, 2L, "Neurología", "Migraña"), Request.class);
        restTemplate.postForEntity("/requests", buildRequest(2L, 3L, "Pediatría", "Control"), Request.class);

        ResponseEntity<Request[]> response = restTemplate.getForEntity("/requests", Request[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length >= 2,
                "Debe retornar al menos las 2 solicitudes creadas");
    }

    // ───────────────────────────────────────────────────────────────────────
    // 3. GET /requests/{id} — Consultar por ID
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /requests/{id} existente → 200 OK con datos de la solicitud")
    void obtenerPorId_existente_devuelve200() {
        ResponseEntity<Request> created = restTemplate.postForEntity(
                "/requests", buildRequest(1L, 2L, "Traumatología", "Fractura de radio"), Request.class);
        Long id = created.getBody().getId();

        ResponseEntity<Request> response = restTemplate.getForEntity("/requests/" + id, Request.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Traumatología", response.getBody().getEspecialidad());
        assertEquals(1L, response.getBody().getUserId());
    }

    @Test
    @DisplayName("GET /requests/{id} inexistente → 404 Not Found")
    void obtenerPorId_inexistente_devuelve404() {
        ResponseEntity<Void> response = restTemplate.getForEntity("/requests/999999", Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ───────────────────────────────────────────────────────────────────────
    // 4. GET /requests/user/{userId} y /estado/{estado}
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /requests/user/{userId} → retorna solicitudes del paciente")
    void obtenerPorUserId_devuelveListaFiltrada() {
        restTemplate.postForEntity("/requests", buildRequest(7L, 2L, "Neurología", "A"), Request.class);
        restTemplate.postForEntity("/requests", buildRequest(7L, 3L, "Pediatría", "B"), Request.class);
        restTemplate.postForEntity("/requests", buildRequest(99L, 2L, "Cardiología", "C"), Request.class);

        ResponseEntity<Request[]> response = restTemplate.getForEntity(
                "/requests/user/7", Request[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().length,
                "Debe retornar solo las 2 solicitudes del userId=7");
        for (Request r : response.getBody()) {
            assertEquals(7L, r.getUserId());
        }
    }

    @Test
    @DisplayName("GET /requests/estado/PENDIENTE → retorna solicitudes en estado PENDIENTE")
    void obtenerPorEstado_pendiente_devuelveFiltrado() {
        restTemplate.postForEntity("/requests", buildRequest(1L, 2L, "Cardiología", "A"), Request.class);
        restTemplate.postForEntity("/requests", buildRequest(2L, 3L, "Pediatría", "B"), Request.class);

        ResponseEntity<Request[]> response = restTemplate.getForEntity(
                "/requests/estado/PENDIENTE", Request[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        for (Request r : response.getBody()) {
            assertEquals("PENDIENTE", r.getEstado());
        }
    }

    // ───────────────────────────────────────────────────────────────────────
    // 5. PUT /requests/{id} — Cambio de estado (proceso crítico)
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /requests/{id} → cambiar estado PENDIENTE a CONFIRMADA (médico asignado)")
    void confirmarSolicitud_estadoCambiaAConfirmada() {
        ResponseEntity<Request> created = restTemplate.postForEntity(
                "/requests", buildRequest(1L, 2L, "Neurología", "Cefalea"), Request.class);
        Long id = created.getBody().getId();

        // Construir request de actualización con médico y fecha cita
        Request actualizacion = new Request();
        actualizacion.setEstado("CONFIRMADA");
        actualizacion.setMedicoId(5L);
        actualizacion.setEspecialidad("Neurología");
        actualizacion.setDescripcion("Cefalea");
        actualizacion.setFechaCita(LocalDateTime.now().plusDays(3));

        ResponseEntity<Request> response = restTemplate.exchange(
                "/requests/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(actualizacion),
                Request.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("CONFIRMADA", response.getBody().getEstado(),
                "El estado debe haberse actualizado a CONFIRMADA");
        assertEquals(5L, response.getBody().getMedicoId());
        assertNotNull(response.getBody().getFechaCita());
    }

    @Test
    @DisplayName("PUT /requests/{id} → cancelar solicitud (CANCELADA)")
    void cancelarSolicitud_estadoCambiaACancelada() {
        ResponseEntity<Request> created = restTemplate.postForEntity(
                "/requests", buildRequest(1L, 2L, "Pediatría", "Revisión"), Request.class);
        Long id = created.getBody().getId();

        Request cancelacion = new Request();
        cancelacion.setEstado("CANCELADA");
        cancelacion.setMedicoId(2L);
        cancelacion.setEspecialidad("Pediatría");
        cancelacion.setDescripcion("Revisión");

        ResponseEntity<Request> response = restTemplate.exchange(
                "/requests/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(cancelacion),
                Request.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("CANCELADA", response.getBody().getEstado());
    }

    @Test
    @DisplayName("PUT /requests/{id} inexistente → 404 Not Found")
    void actualizarSolicitud_inexistente_devuelve404() {
        Request cambios = buildRequest(1L, 2L, "Cardiología", "Desc");
        cambios.setEstado("CONFIRMADA");

        ResponseEntity<Void> response = restTemplate.exchange(
                "/requests/999999",
                HttpMethod.PUT,
                new HttpEntity<>(cambios),
                Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ───────────────────────────────────────────────────────────────────────
    // 6. DELETE /requests/{id}
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /requests/{id} existente → 204 No Content")
    void eliminarSolicitud_existente_devuelve204() {
        ResponseEntity<Request> created = restTemplate.postForEntity(
                "/requests", buildRequest(1L, 2L, "Traumatología", "Esguince"), Request.class);
        Long id = created.getBody().getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/requests/" + id, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verificar que ya no existe
        ResponseEntity<Void> check = restTemplate.getForEntity("/requests/" + id, Void.class);
        assertEquals(HttpStatus.NOT_FOUND, check.getStatusCode(), "La solicitud debe haber sido eliminada");
    }

    @Test
    @DisplayName("DELETE /requests/{id} inexistente → 404 Not Found")
    void eliminarSolicitud_inexistente_devuelve404() {
        ResponseEntity<Void> response = restTemplate.exchange(
                "/requests/999999", HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── Helper ───────────────────────────────────────────────────────────
    private Request buildRequest(Long userId, Long medicoId, String especialidad, String descripcion) {
        Request r = new Request();
        r.setUserId(userId);
        r.setMedicoId(medicoId);
        r.setEspecialidad(especialidad);
        r.setDescripcion(descripcion);
        return r;
    }
}
