package com.userservice.service;

import com.userservice.entity.User;
import com.userservice.exception.ResourceNotFoundException;
import com.userservice.repository.UserRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByRut(String rut) {
        return userRepository.findByRut(rut);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    public List<User> getMedicos() {
        return userRepository.findByRole("MEDICO");
    }

    public User createUser(@NonNull User user) {
        if (userRepository.existsByRut(user.getRut())) {
            throw new IllegalArgumentException("Ya existe un usuario con el RUT: " + user.getRut());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + user.getEmail());
        }
        // Al registrarse, el rol por defecto es PACIENTE
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("PACIENTE");
        }
        // La especialidad solo aplica para MEDICO
        if (!"MEDICO".equals(user.getRole())) {
            user.setEspecialidad(null);
        }
        return userRepository.save(user);
    }

    public User updateUser(Long id, @NonNull User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setName(userDetails.getName());
            user.setLastname(userDetails.getLastname());
            user.setEmail(userDetails.getEmail());
            user.setPhone(userDetails.getPhone());
            user.setRegion(userDetails.getRegion());
            user.setComuna(userDetails.getComuna());
            user.setAddress(userDetails.getAddress());
            // Solo admin puede cambiar rol y especialidad
            if (userDetails.getRole() != null && !userDetails.getRole().isBlank()) {
                user.setRole(userDetails.getRole());
            }
            if ("MEDICO".equals(user.getRole())) {
                user.setEspecialidad(userDetails.getEspecialidad());
            } else {
                user.setEspecialidad(null);
            }
            return userRepository.save(user);
        }).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el ID: " + id));
    }

    public User updatePassword(Long id, String newPassword) {
        return userRepository.findById(id).map(user -> {
            user.setPassword(newPassword);
            return userRepository.save(user);
        }).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el ID: " + id));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con el ID: " + id);
        }
        userRepository.deleteById(id);
    }
}