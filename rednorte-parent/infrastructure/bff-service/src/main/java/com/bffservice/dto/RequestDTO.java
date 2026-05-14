package com.bffservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestDTO {
    private Long id;
    private Long userId;
    private Long medicoId;
    private String especialidad;
    private String descripcion;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaCita;
}