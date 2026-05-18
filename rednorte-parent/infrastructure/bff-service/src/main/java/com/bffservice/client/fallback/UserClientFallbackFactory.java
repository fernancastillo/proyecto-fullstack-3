package com.bffservice.client.fallback;

import com.bffservice.client.UserClient;
import com.bffservice.dto.LoginRequestDTO;
import com.bffservice.dto.RegisterRequestDTO;
import com.bffservice.dto.UserDTO;
import com.bffservice.exception.ServiceUnavailableException;
import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {

            @Override
            public List<UserDTO> getAllUsers() {
                return Collections.emptyList();
            }

            @Override
            public UserDTO getUserById(Long id) {
                throw new ServiceUnavailableException("usuarios");
            }

            @Override
            public UserDTO getUserByRut(String rut) {
                throw new ServiceUnavailableException("usuarios");
            }

            @Override
            public UserDTO getUserByEmail(String email) {
                throw new ServiceUnavailableException("usuarios");
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
                if (cause instanceof FeignException fe) {
                    throw fe; // propaga el 409 Conflict, 400, etc.
                }
                throw new ServiceUnavailableException("usuarios");
            }

            @Override
            public UserDTO authenticate(LoginRequestDTO loginRequest) {
                if (cause instanceof FeignException fe) {
                    throw fe; // propaga el 401 al AuthController
                }
                throw new ServiceUnavailableException("usuarios");
            }

            @Override
            public UserDTO updateUser(Long id, UserDTO user) {
                if (cause instanceof FeignException fe) {
                    throw fe;
                }
                throw new ServiceUnavailableException("usuarios");
            }

            @Override
            public void deleteUser(Long id) {
                if (cause instanceof FeignException fe) {
                    throw fe;
                }
                throw new ServiceUnavailableException("usuarios");
            }

            @Override
            public void updatePassword(Long id, String newPassword) {
                if (cause instanceof FeignException fe) {
                    throw fe;
                }
                throw new ServiceUnavailableException("usuarios");
            }
        };
    }
}