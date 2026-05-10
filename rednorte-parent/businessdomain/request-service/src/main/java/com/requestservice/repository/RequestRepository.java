package com.requestservice.repository;

import com.requestservice.entity.Request;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository

public interface RequestRepository extends JpaRepository<Request, Long>{
    List<Request> findByRut(String rut);

    List<Request> findByEspecialidad(String especialidad);

    Optional<Request> findById(Long id);

    boolean existsByRut(String rut);
}
