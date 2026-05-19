package com.waitinglistservice.logs.repository;

import com.waitinglistservice.logs.entity.LogRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface LogRequestRepository extends JpaRepository<LogRequest, Long> {

    List<LogRequest> findByMicroservicioOrderByFechaDesc(String microservicio);

    List<LogRequest> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<LogRequest> findTop20ByOrderByFechaDesc();

    List<LogRequest> findByErrorMensajeIsNotNullOrderByFechaDesc();

    @Query("SELECT AVG(l.tiempoRespuesta) FROM LogRequest l WHERE l.endpoint = :endpoint")
    Double getTiempoPromedioByEndpoint(@Param("endpoint") String endpoint);
}
