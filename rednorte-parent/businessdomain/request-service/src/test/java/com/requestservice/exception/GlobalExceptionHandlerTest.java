package com.requestservice.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/requests/1");
        webRequest = new ServletWebRequest(mockRequest);
    }

    @Test
    void handleIllegalArgumentException_devuelve409ConMensaje() {
        IllegalArgumentException ex = new IllegalArgumentException("Conflicto de datos");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(ex, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflicto", response.getBody().getError());
        assertEquals("Conflicto de datos", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleResourceNotFoundException_devuelve404ConMensaje() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Solicitud no encontrada con el ID: 99");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex, webRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No Encontrado", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("99"));
    }

    @Test
    void handleGlobalException_devuelve500ConMensaje() {
        RuntimeException ex = new RuntimeException("Error inesperado de base de datos");

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error Interno del Servidor", response.getBody().getError());
        assertEquals("Error inesperado de base de datos", response.getBody().getMessage());
    }

    @Test
    void handleResourceNotFoundException_incluyeRutaEnLaRespuesta() {
        ResourceNotFoundException ex = new ResourceNotFoundException("No encontrado");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex, webRequest);

        assertEquals("/requests/1", response.getBody().getPath());
    }
}