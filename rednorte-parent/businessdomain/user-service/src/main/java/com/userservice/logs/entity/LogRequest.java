package com.userservice.logs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "logs_request")
@Getter
@Setter
@NoArgsConstructor
public class LogRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String endpoint;
    private String metodoHttp;
    private Long tiempoRespuesta;
    private Integer status;
    private String microservicio;
    private LocalDateTime fecha;
}
