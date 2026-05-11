package com.userservice.service;

import com.userservice.entity.User;
import com.userservice.exception.ResourceNotFoundException;
import com.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

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

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(Long id, @NonNull User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(userDetails.getUsername());
            user.setEmail(userDetails.getEmail());
            user.setRole(userDetails.getRole());
            user.setPassword(userDetails.getPassword());
            return userRepository.save(user);
        }).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el ID: " + id)); // <-- Cambiado aquí
    }


    public void deleteUser(Long id) {
    if (!userRepository.existsById(id)) {
        throw new ResourceNotFoundException("Usuario no encontrado con el ID: " + id);
    }
    userRepository.deleteById(id);
}
}