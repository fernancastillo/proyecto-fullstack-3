package com.requestservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // referencia al paciente en user-service

    @Column(nullable = false)
    private Long medicoId; // referencia al médico en user-service

    @Column(nullable = false, length = 100)
    private String especialidad;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String estado; // PENDIENTE, CONFIRMADA, CANCELADA

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaCita;

    @PrePersist
    protected void onCreate() {
        fechaSolicitud = LocalDateTime.now();
        if (estado == null) estado = "PENDIENTE";
    }
}