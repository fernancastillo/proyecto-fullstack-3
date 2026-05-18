package com.bffservice.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    // Clave Base64 de al menos 256 bits para HS256
    private static final String SECRET =
        "dGVzdFNlY3JldEtleVBhcmFQcnVlYmFzRnVsbFN0YWNrMTIzNDU2Nzg=";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);
    }

    @Test
    void testGenerateToken_NotNull() {
        String token = jwtUtil.generateToken(1L, "test@correo.cl", "PACIENTE", "Juan", "Pérez");
        assertNotNull(token);
    }

    @Test
    void testGenerateToken_HasThreeParts() {
        String token = jwtUtil.generateToken(1L, "test@correo.cl", "MEDICO", "Ana", "López");
        // Un JWT siempre tiene tres partes separadas por "."
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    void testGenerateToken_DifferentUsersProduceDifferentTokens() {
        String token1 = jwtUtil.generateToken(1L, "a@correo.cl", "PACIENTE", "Juan", "Pérez");
        String token2 = jwtUtil.generateToken(2L, "b@correo.cl", "MEDICO",   "Ana",  "López");
        assertNotEquals(token1, token2);
    }
}