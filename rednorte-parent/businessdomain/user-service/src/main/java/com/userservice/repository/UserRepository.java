package com.userservice.repository;

import com.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByRut(String rut);
    Optional<User> findByEmail(String email);
    boolean existsByRut(String rut);
    boolean existsByEmail(String email);
    List<User> findByRole(String role);
    List<User> findByEspecialidad(String especialidad);
}