package com.userservice.exception;

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
        mockRequest.setRequestURI("/api/users");
        webRequest = new ServletWebRequest(mockRequest);
    }

    @Test
    void handleIllegalArgumentException_devuelve409ConMensaje() {
        IllegalArgumentException ex = new IllegalArgumentException("Ya existe un usuario con el RUT: 12345678");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(ex, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflicto", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("RUT"));
    }

    @Test
    void handleResourceNotFoundException_devuelve404ConMensaje() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Usuario no encontrado con el ID: 99");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex, webRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No Encontrado", response.getBody().getError());
    }

    @Test
    void handleGlobalException_devuelve500ConMensaje() {
        RuntimeException ex = new RuntimeException("Fallo inesperado");

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error Interno del Servidor", response.getBody().getError());
    }
}