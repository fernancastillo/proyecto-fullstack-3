package com.bffservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientDTO {
    private Long id;
    private String rut;
    private String dv;
    private String name;
    private String lastname;
    private String email;
    private String phone;
    private String region;
    private String comuna;
    private String address;
}