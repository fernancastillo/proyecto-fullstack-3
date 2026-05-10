package com.requestservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "request")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 9)
    private String rut;

    @Column(nullable = false, length = 1)
    private String dv;

    @Column(nullable = false)
    private String especialidad;

    @Column(nullable = false)
    private String descripcion;
}
