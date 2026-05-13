package com.patientservice.filter;

import com.patientservice.logs.entity.LogRequest;
import com.patientservice.logs.service.LogRequestService;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class LoggingFilter implements Filter {

    private final LogRequestService logService;

    public LoggingFilter(LogRequestService logService) {
        this.logService = logService;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        long inicio = System.currentTimeMillis();

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        chain.doFilter(request, response);

        long fin = System.currentTimeMillis();

        LogRequest log = new LogRequest();

        log.setEndpoint(req.getRequestURI());
        log.setMetodoHttp(req.getMethod());
        log.setTiempoRespuesta(fin - inicio);
        log.setStatus(res.getStatus());
        log.setFecha(LocalDateTime.now());
        log.setMicroservicio("patients-service");

        logService.guardar(log);
    }
}