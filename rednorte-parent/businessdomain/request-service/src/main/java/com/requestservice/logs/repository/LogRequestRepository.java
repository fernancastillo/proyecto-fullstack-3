package com.requestservice.logs.repository;

import com.requestservice.logs.entity.LogRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface LogRequestRepository extends JpaRepository<LogRequest, Long> {

    List<LogRequest> findByMicroservicioOrderByFechaDesc(String microservicio);

    List<LogRequest> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<LogRequest> findTop20ByOrderByFechaDesc();

    @Query("SELECT AVG(l.tiempoRespuesta) FROM LogRequest l WHERE l.endpoint = :endpoint")
    Double getTiempoPromedioByEndpoint(@Param("endpoint") String endpoint);
}
