package com.bffservice.dto;

import java.time.LocalDateTime;

public class LogRequestDTO {

    private Long          id;
    private String        endpoint;
    private String        metodoHttp;
    private Long          tiempoRespuesta;
    private Integer       status;
    private String        microservicio;
    private LocalDateTime fecha;
    private String        errorMensaje;

    // ── Getters y Setters ─────────────────────────────────────────────────────

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public String getEndpoint()                { return endpoint; }
    public void setEndpoint(String endpoint)   { this.endpoint = endpoint; }

    public String getMetodoHttp()              { return metodoHttp; }
    public void setMetodoHttp(String m)        { this.metodoHttp = m; }

    public Long getTiempoRespuesta()           { return tiempoRespuesta; }
    public void setTiempoRespuesta(Long t)     { this.tiempoRespuesta = t; }

    public Integer getStatus()                 { return status; }
    public void setStatus(Integer status)      { this.status = status; }

    public String getMicroservicio()           { return microservicio; }
    public void setMicroservicio(String m)     { this.microservicio = m; }

    public LocalDateTime getFecha()            { return fecha; }
    public void setFecha(LocalDateTime fecha)  { this.fecha = fecha; }

    public String getErrorMensaje()            { return errorMensaje; }
    public void setErrorMensaje(String e)      { this.errorMensaje = e; }
}