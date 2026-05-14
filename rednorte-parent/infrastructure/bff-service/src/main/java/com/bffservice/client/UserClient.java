package com.bffservice.client;

import com.bffservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    @GetMapping("/api/users")
    List<UserDTO> getAllUsers();

    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users/rut/{rut}")
    UserDTO getUserByRut(@PathVariable("rut") String rut);

    @GetMapping("/api/users/email/{email}")
    UserDTO getUserByEmail(@PathVariable("email") String email);

    @GetMapping("/api/users/role/{role}")
    List<UserDTO> getUsersByRole(@PathVariable("role") String role);

    @GetMapping("/api/users/medicos")
    List<UserDTO> getMedicos();

    @PostMapping("/api/users")
    UserDTO createUser(@RequestBody UserDTO user);

    @PutMapping("/api/users/{id}")
    UserDTO updateUser(@PathVariable("id") Long id, @RequestBody UserDTO user);

    @DeleteMapping("/api/users/{id}")
    void deleteUser(@PathVariable("id") Long id);
}