package com.userservice.controller;

import com.userservice.dto.AuthRequest;
import com.userservice.entity.User;
import com.userservice.exception.ResourceNotFoundException;
import com.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el ID: " + id));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/rut/{rut}")
    public ResponseEntity<User> getUserByRut(@PathVariable("rut") String rut) {
        User user = userService.getUserByRut(rut)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el RUT: " + rut));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable("email") String email) {
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el email: " + email));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable("role") String role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    @GetMapping("/medicos")
    public ResponseEntity<List<User>> getMedicos() {
        return ResponseEntity.ok(userService.getMedicos());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable("id") Long id,
            @RequestBody User userDetails) {
        return ResponseEntity.ok(userService.updateUser(id, userDetails));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable("id") Long id,
            @RequestBody String newPassword) {
        userService.updatePassword(id, newPassword);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Valida email + contraseña y devuelve el usuario si las credenciales son correctas.
     * Retorna 401 si son incorrectas o el usuario no existe.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<User> authenticate(@RequestBody AuthRequest request) {
        Optional<User> userOpt = userService.authenticate(request.getEmail(), request.getPassword());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userOpt.get();
        user.setPassword(null); // nunca exponer el hash
        return ResponseEntity.ok(user);
    }
}
