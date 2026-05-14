package com.bffservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private String token;
    private Long id;
    private String email;
    private String role;
    private String name;
    private String lastname;
    private String especialidad;
}
