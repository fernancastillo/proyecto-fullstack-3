package com.userservice.integration;

import com.userservice.entity.User;
import com.userservice.exception.ResourceNotFoundException;
import com.userservice.repository.UserRepository;
import com.userservice.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("[INTEGRACIÓN] UserService — Procesos de negocio críticos")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // ─── Datos de prueba (RUTs/emails que NO existen en DataInitializer) ───
    private static final String RUT_PACIENTE  = "20202020";
    private static final String RUT_MEDICO    = "21212121";
    private static final String RUT_EXTRA     = "30303030";
    private static final String EMAIL_PACIENTE = "test.paciente1@test.cl";
    private static final String EMAIL_MEDICO   = "test.medico1@test.cl";
    private static final String EMAIL_EXTRA    = "test.extra1@test.cl";

    @AfterEach
    void limpiarDatosTest() {
        // Eliminar sólo los usuarios creados por los tests
        userRepository.findByEmail(EMAIL_PACIENTE).ifPresent(u -> userRepository.delete(u));
        userRepository.findByEmail(EMAIL_MEDICO).ifPresent(u -> userRepository.delete(u));
        userRepository.findByEmail(EMAIL_EXTRA).ifPresent(u -> userRepository.delete(u));
        userRepository.findByRut(RUT_EXTRA).ifPresent(u -> userRepository.delete(u));
    }

    // ───────────────────────────────────────────────────────────────────────
    // 1. REGISTRO DE USUARIO
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Crear paciente → se persiste en BD con rol PACIENTE y contraseña encodeada")
    void crearPaciente_debeGuardarseEnBD() {
        User nuevo = buildUser(RUT_PACIENTE, "0", EMAIL_PACIENTE, "+56920202020", "PACIENTE", null);

        User creado = userService.createUser(nuevo);

        assertNotNull(creado.getId(), "El ID debe asignarse después de persistir");
        assertEquals("PACIENTE", creado.getRole());
        assertNull(creado.getEspecialidad(), "Los pacientes no tienen especialidad");
        // La contraseña debe estar hasheada (no en texto plano)
        assertNotEquals("pass1234", creado.getPassword(), "La contraseña debe estar encodeada");
        assertTrue(creado.getPassword().startsWith("$2"), "Debe ser hash BCrypt");
    }

    @Test
    @DisplayName("Crear médico → especialidad se persiste en BD")
    void crearMedico_debeGuardarEspecialidad() {
        User medico = buildUser(RUT_MEDICO, "K", EMAIL_MEDICO, "+56921212121", "MEDICO", "Dermatología");

        User creado = userService.createUser(medico);

        assertEquals("MEDICO", creado.getRole());
        assertEquals("Dermatología", creado.getEspecialidad());
    }

    @Test
    @DisplayName("Crear paciente con especialidad informada → especialidad debe quedar NULL (regla de negocio)")
    void crearPaciente_especialidadDebeSerNull() {
        User paciente = buildUser(RUT_PACIENTE, "0", EMAIL_PACIENTE, "+56920202020", "PACIENTE", "Cardiología");

        User creado = userService.createUser(paciente);

        assertNull(creado.getEspecialidad(),
                "El servicio debe ignorar la especialidad para roles no médicos");
    }

    @Test
    @DisplayName("Registrar con RUT duplicado → lanza IllegalArgumentException")
    void crearUsuario_rutDuplicado_debeLanzarExcepcion() {
        User primero = buildUser(RUT_PACIENTE, "0", EMAIL_PACIENTE, "+56920202020", "PACIENTE", null);
        userService.createUser(primero);

        // Segundo usuario con mismo RUT pero diferente email
        User segundo = buildUser(RUT_PACIENTE, "0", EMAIL_EXTRA, "+56930303030", "PACIENTE", null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(segundo));

        assertTrue(ex.getMessage().contains("RUT"), "El mensaje debe mencionar el RUT duplicado");
    }

    @Test
    @DisplayName("Registrar con email duplicado → lanza IllegalArgumentException")
    void crearUsuario_emailDuplicado_debeLanzarExcepcion() {
        User primero = buildUser(RUT_PACIENTE, "0", EMAIL_PACIENTE, "+56920202020", "PACIENTE", null);
        userService.createUser(primero);

        // Segundo usuario con diferente RUT pero mismo email
        User segundo = buildUser(RUT_EXTRA, "0", EMAIL_PACIENTE, "+56930303030", "PACIENTE", null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(segundo));

        assertTrue(ex.getMessage().contains("email"), "El mensaje debe mencionar el email duplicado");
    }

    // ───────────────────────────────────────────────────────────────────────
    // 2. AUTENTICACIÓN (proceso crítico del sistema)
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Autenticar con credenciales correctas → retorna usuario")
    void autenticar_credencialesCorrectas_retornaUsuario() {
        userService.createUser(buildUser(RUT_PACIENTE, "0", EMAIL_PACIENTE, "+56920202020", "PACIENTE", null));

        Optional<User> resultado = userService.authenticate(EMAIL_PACIENTE, "pass1234");

        assertTrue(resultado.isPresent(), "Debe retornar el usuario cuando las credenciales son correctas");
        assertEquals(EMAIL_PACIENTE, resultado.get().getEmail());
    }

    @Test
    @DisplayName("Autenticar con contraseña incorrecta → retorna Optional vacío")
    void autenticar_contrasennaIncorrecta_retornaVacio() {
        userService.createUser(buildUser(RUT_PACIENTE, "0", EMAIL_PACIENTE, "+56920202020", "PACIENTE", null));

        Optional<User> resultado = userService.authenticate(EMAIL_PACIENTE, "wrongpassword");

        assertTrue(resultado.isEmpty(), "No debe autenticar con contraseña incorrecta");
    }

    @Test
    @DisplayName("Autenticar email inexistente → retorna Optional vacío")
    void autenticar_emailNoExiste_retornaVacio() {
        Optional<User> resultado = userService.authenticate("noexiste@test.cl", "pass1234");

        assertTrue(resultado.isEmpty(), "No debe autenticar si el email no existe");
    }

    @Test
    @DisplayName("Autenticar usuario Deshabilitado → acceso denegado aunque contraseña sea correcta")
    void autenticar_usuarioDeshabilitado_accesoDenegado() {
        userService.createUser(buildUser(RUT_PACIENTE, "0", EMAIL_PACIENTE, "+56920202020", "Deshabilitado", null));

        Optional<User> resultado = userService.authenticate(EMAIL_PACIENTE, "pass1234");

        assertTrue(resultado.isEmpty(), "Los usuarios deshabilitados no deben poder autenticarse");
    }

    // ───────────────────────────────────────────────────────────────────────
    // 3. CONSULTAS POR ROL
    // ───────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMedicos() → retorna sólo usuarios con rol MEDICO (activos)")
    void getMedicos_retornaListaDeMedicos() {
        userService.createUser(buildUser(RUT_MEDICO, "K", EMAIL_MEDICO, "+56921212121", "MEDICO", "Cirugía"));

        List<User> medicos = userService.getMedicos();

        assertFalse(medicos.isEmpty(), "Debe haber al menos un médico");
        assertTrue(medicos.stream().allMatch(u -> "MEDICO".equals(u.getRole())),
                "Todos los resultados deben tener rol MEDICO");
    }

    @Test
    @DisplayName("Actualizar usuario existente → cambios se persisten en BD")
    void actualizarUsuario_cambiosSePersisten() {
        User creado = userService.createUser(
                buildUser(RUT_PACIENTE, "0", EMAIL_PACIENTE, "+56920202020", "PACIENTE", null));

        User cambios = buildUser(RUT_PACIENTE, "0", EMAIL_PACIENTE, "+56920202020", "PACIENTE", null);
        cambios.setName("NombreModificado");
        cambios.setAddress("Nueva Dirección 999");

        User actualizado = userService.updateUser(creado.getId(), cambios);

        assertEquals("NombreModificado", actualizado.getName());
        assertEquals("Nueva Dirección 999", actualizado.getAddress());
    }

    @Test
    @DisplayName("Actualizar usuario inexistente → lanza ResourceNotFoundException")
    void actualizarUsuario_noExiste_lanzaExcepcion() {
        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(999999L, buildUser(RUT_PACIENTE, "0", EMAIL_PACIENTE, "+56920202020", "PACIENTE", null)));
    }

    // ─── Helper ───────────────────────────────────────────────────────────
    private User buildUser(String rut, String dv, String email, String phone, String role, String especialidad) {
        return new User(null, rut, dv, "Test", "Usuario",
                email, "pass1234", phone,
                "Tarapacá", "Iquique", "Calle Test 100",
                role, especialidad);
    }
}
