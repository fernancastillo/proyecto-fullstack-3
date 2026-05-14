package com.bffservice.controller;

import com.bffservice.client.UserClient;
import com.bffservice.dto.AuthResponseDTO;
import com.bffservice.dto.LoginRequestDTO;
import com.bffservice.dto.RegisterRequestDTO;
import com.bffservice.dto.UserDTO;
import com.bffservice.util.JwtUtil;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/bff/auth")
public class AuthController {

    private final UserClient userClient;
    private final JwtUtil jwtUtil;

    public AuthController(UserClient userClient, JwtUtil jwtUtil) {
        this.userClient = userClient;
        this.jwtUtil = jwtUtil;
    }

    /**
     * POST /bff/auth/register
     * Registra un nuevo usuario (rol PACIENTE por defecto).
     * Devuelve el token JWT + datos del usuario creado.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO req) {
        try {
            UserDTO created = userClient.registerUser(req);
            String token = jwtUtil.generateToken(
                    created.getId(), created.getEmail(),
                    created.getRole(), created.getName(), created.getLastname());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new AuthResponseDTO(token, created.getId(), created.getEmail(),
                            created.getRole(), created.getName(), created.getLastname(),
                            created.getEspecialidad()));
        } catch (FeignException.Conflict e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "El RUT o correo ya están registrados."));
        } catch (FeignException.BadRequest e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Datos de registro inválidos."));
        } catch (FeignException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al registrar el usuario."));
        }
    }

    /**
     * POST /bff/auth/login
     * Valida credenciales y devuelve el token JWT + datos del usuario.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO req) {
        try {
            UserDTO user = userClient.authenticate(req);
            String token = jwtUtil.generateToken(
                    user.getId(), user.getEmail(),
                    user.getRole(), user.getName(), user.getLastname());
            return ResponseEntity.ok(
                    new AuthResponseDTO(token, user.getId(), user.getEmail(),
                            user.getRole(), user.getName(), user.getLastname(),
                            user.getEspecialidad()));
        } catch (FeignException.Unauthorized e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Correo o contraseña incorrectos."));
        } catch (FeignException.NotFound e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Correo o contraseña incorrectos."));
        } catch (FeignException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al iniciar sesión."));
        }
    }
}
