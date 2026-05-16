package com.bffservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaitingListEnrichedDTO {
    private Long id;
    private Long userId;
    private String patientName;
    private Long medicoId;
    private String doctorName;
    private String doctorEspecialidad;
    private String specialty;
    private String priority;
    private String status;
    private LocalDateTime requestDate;
}