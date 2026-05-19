package com.bffservice.dto;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String rut;
    private String dv;
    private String name;
    private String lastname;
    private String email;
    private String password; // Solo se usa al CREAR un usuario desde el admin. Nunca se muestra en la UI.
    private String phone;
    private String region;
    private String comuna;
    private String address;
    private String role;
    private String especialidad;
}