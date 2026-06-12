package com.userservice.e2e;

import com.userservice.entity.User;
import com.userservice.repository.UserRepository;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("[E2E] UserController — Flujo HTTP completo")
class UserControllerE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    // ─── RUTs y emails exclusivos para E2E (sin conflicto con DataInitializer) ───
    private static final String RUT_E2E_1  = "40404040";
    private static final String RUT_E2E_2  = "41414141";
    private static final String EMAIL_E2E_1 = "e2e.paciente1@test.cl";
    private static final String EMAIL_E2E_2 = "e2e.paciente2@test.cl";

    @AfterEach
    void limpiarDatosE2E() {
        userRepository.findByEmail(EMAIL_E2E_1).ifPresent(userRepository::delete);
        userRepository.findByEmail(EMAIL_E2E_2).ifPresent(userRepository::delete);
    }

    // ───────────────────────────────────────────────────────────────────────
    // 1. POST /api/users — Registro de usuario
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/users → 201 Created con datos del usuario creado")
    void registrarUsuario_devuelve201YDatosDelUsuario() {
        User payload = buildUser(RUT_E2E_1, "0", EMAIL_E2E_1, "+56940404040");

        ResponseEntity<User> response = restTemplate.postForEntity("/api/users", payload, User.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Debe retornar HTTP 201");
        assertNotNull(response.getBody(), "El cuerpo de respuesta no debe ser null");
        assertNotNull(response.getBody().getId(), "El usuario creado debe tener ID asignado");
        assertEquals("PACIENTE", response.getBody().getRole());
        assertNull(response.getBody().getEspecialidad(), "Paciente no debe tener especialidad");
    }

    // ───────────────────────────────────────────────────────────────────────
    // 2. POST /api/users/authenticate — Login
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/users/authenticate con credenciales válidas → 200 OK y usuario sin password")
    void autenticar_credencialesValidas_devuelve200() {
        // Registrar usuario primero
        User payload = buildUser(RUT_E2E_1, "0", EMAIL_E2E_1, "+56940404040");
        restTemplate.postForEntity("/api/users", payload, User.class);

        // Autenticar
        Map<String, String> authRequest = Map.of("email", EMAIL_E2E_1, "password", "pass1234");
        ResponseEntity<User> response = restTemplate.postForEntity(
                "/api/users/authenticate", authRequest, User.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Debe retornar HTTP 200");
        assertNotNull(response.getBody());
        assertEquals(EMAIL_E2E_1, response.getBody().getEmail());
        assertNull(response.getBody().getPassword(),
                "La contraseña NO debe retornarse en la respuesta por seguridad");
    }

    @Test
    @DisplayName("POST /api/users/authenticate con contraseña incorrecta → 401 Unauthorized")
    void autenticar_contrasennaIncorrecta_devuelve401() {
        User payload = buildUser(RUT_E2E_1, "0", EMAIL_E2E_1, "+56940404040");
        restTemplate.postForEntity("/api/users", payload, User.class);

        Map<String, String> authRequest = Map.of("email", EMAIL_E2E_1, "password", "wrongpass");
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/users/authenticate", authRequest, Void.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                "Debe retornar HTTP 401 con contraseña incorrecta");
    }

    @Test
    @DisplayName("POST /api/users/authenticate con email inexistente → 401 Unauthorized")
    void autenticar_emailNoExiste_devuelve401() {
        Map<String, String> authRequest = Map.of(
                "email", "fantasma@noexiste.cl", "password", "pass1234");

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/users/authenticate", authRequest, Void.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ───────────────────────────────────────────────────────────────────────
    // 3. GET /api/users/{id} — Consultar usuario por ID
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/users/{id} existente → 200 OK con datos del usuario")
    void obtenerUsuarioPorId_existente_devuelve200() {
        // Crear usuario y obtener su ID
        User payload = buildUser(RUT_E2E_1, "0", EMAIL_E2E_1, "+56940404040");
        ResponseEntity<User> created = restTemplate.postForEntity("/api/users", payload, User.class);
        Long id = created.getBody().getId();

        // Consultar por ID
        ResponseEntity<User> response = restTemplate.getForEntity("/api/users/" + id, User.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(EMAIL_E2E_1, response.getBody().getEmail());
    }

    @Test
    @DisplayName("GET /api/users/{id} inexistente → 404 Not Found")
    void obtenerUsuarioPorId_inexistente_devuelve404() {
        ResponseEntity<Void> response = restTemplate.getForEntity("/api/users/999999", Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                "Debe retornar HTTP 404 cuando el usuario no existe");
    }

    // ───────────────────────────────────────────────────────────────────────
    // 4. GET /api/users/role/{role} — Consultar por rol
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/users/role/PACIENTE → 200 OK con lista de pacientes")
    void obtenerUsuariosPorRol_devuelve200ConLista() {
        // El DataInitializer ya crea pacientes, así que siempre habrá resultados
        ResponseEntity<User[]> response = restTemplate.getForEntity(
                "/api/users/role/PACIENTE", User[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("PUT /api/users/{id}/password → 204 No Content")
    void actualizarPassword_devuelve204() {
        User payload = buildUser(RUT_E2E_1, "0", EMAIL_E2E_1, "+56940404040");
        ResponseEntity<User> created = restTemplate.postForEntity("/api/users", payload, User.class);
        Long id = created.getBody().getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/users/" + id + "/password",
                HttpMethod.PUT,
                new HttpEntity<>("nuevaPass5678"),
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    // ─── Helper ───────────────────────────────────────────────────────────
    private User buildUser(String rut, String dv, String email, String phone) {
        return new User(null, rut, dv, "Test", "E2E",
                email, "pass1234", phone,
                "Tarapacá", "Iquique", "Calle E2E 200",
                "PACIENTE", null);
    }
}
