package com.waitinglistservice.exception;

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
        mockRequest.setRequestURI("/waiting-list/1");
        webRequest = new ServletWebRequest(mockRequest);
    }

    @Test
    void handleResourceNotFoundException_devuelve404ConMensaje() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Entrada no encontrada con el ID: 99");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex, webRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No Encontrado", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("99"));
    }

    @Test
    void handleGlobalException_devuelve500ConMensaje() {
        RuntimeException ex = new RuntimeException("Error inesperado");

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error Interno del Servidor", response.getBody().getError());
    }

    @Test
    void handleResourceNotFoundException_incluyeRutaEnLaRespuesta() {
        ResourceNotFoundException ex = new ResourceNotFoundException("No encontrado");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex, webRequest);

        assertEquals("/waiting-list/1", response.getBody().getPath());
    }
}