package com.bffservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    private String rut;
    private String dv;
    private String name;
    private String lastname;
    private String email;
    private String password;
    private String phone;
    private String region;
    private String comuna;
    private String address;
    // role y especialidad se asignan en el backend
}
