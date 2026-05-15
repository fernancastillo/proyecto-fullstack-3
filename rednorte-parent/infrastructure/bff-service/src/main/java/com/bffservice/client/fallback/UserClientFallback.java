package com.bffservice.client.fallback;

import com.bffservice.client.UserClient;
import com.bffservice.dto.LoginRequestDTO;
import com.bffservice.dto.RegisterRequestDTO;
import com.bffservice.dto.UserDTO;
import com.bffservice.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class UserClientFallback implements UserClient {

    private static final String SERVICE = "usuarios";

    @Override
    public List<UserDTO> getAllUsers() {
        return Collections.emptyList();
    }

    @Override
    public UserDTO getUserById(Long id) {
        throw new ServiceUnavailableException(SERVICE);
    }

    @Override
    public UserDTO getUserByRut(String rut) {
        throw new ServiceUnavailableException(SERVICE);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        throw new ServiceUnavailableException(SERVICE);
    }

    @Override
    public List<UserDTO> getUsersByRole(String role) {
        return Collections.emptyList();
    }

    @Override
    public List<UserDTO> getMedicos() {
        return Collections.emptyList();
    }

    @Override
    public UserDTO createUser(RegisterRequestDTO request) {
        throw new ServiceUnavailableException(SERVICE);
    }

    @Override
    public UserDTO authenticate(LoginRequestDTO loginRequest) {
        throw new ServiceUnavailableException(SERVICE);
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO user) {
        throw new ServiceUnavailableException(SERVICE);
    }

    @Override
    public void deleteUser(Long id) {
        throw new ServiceUnavailableException(SERVICE);
    }
}