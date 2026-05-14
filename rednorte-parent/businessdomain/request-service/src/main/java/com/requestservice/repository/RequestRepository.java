package com.requestservice.repository;

import com.requestservice.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByUserId(Long userId);
    List<Request> findByMedicoId(Long medicoId);
    List<Request> findByEspecialidad(String especialidad);
    List<Request> findByEstado(String estado);
    List<Request> findByUserIdAndEstado(Long userId, String estado);
    List<Request> findByMedicoIdAndEstado(Long medicoId, String estado);
}