package com.waitinglistservice.filter;

import com.waitinglistservice.logs.entity.LogRequest;
import com.waitinglistservice.logs.service.LogRequestService;
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

        String uri = req.getRequestURI();
        if (uri.contains("/actuator") || uri.contains("/swagger") ||
            uri.contains("/v3/api-docs") || uri.contains("/h2-console")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            chain.doFilter(request, response);
        } finally {
            long fin = System.currentTimeMillis();
            long tiempoRespuesta = fin - inicio;

            System.out.println("[" + LocalDateTime.now() + "] " +
                               req.getMethod() + " " + uri +
                               " -> " + res.getStatus() +
                               " (" + tiempoRespuesta + "ms)");

            LogRequest log = new LogRequest();
            log.setEndpoint(uri);
            log.setMetodoHttp(req.getMethod());
            log.setTiempoRespuesta(tiempoRespuesta);
            log.setStatus(res.getStatus());
            log.setFecha(LocalDateTime.now());
            log.setMicroservicio("patient-service");

            try {
                logService.guardar(log);
            } catch (Exception e) {
                System.err.println("ERROR guardando log en BD: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
