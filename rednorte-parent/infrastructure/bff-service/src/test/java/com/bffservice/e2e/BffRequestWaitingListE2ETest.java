package com.bffservice.e2e;

import com.bffservice.dto.RequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("[E2E] BFF — Flujo cross-service: crear solicitud médica + alta automática en lista de espera")
class BffRequestWaitingListE2ETest {

    // Se inicia en bloque estático: debe estar levantado ANTES de que
    // @DynamicPropertySource registre su puerto.
    private static final WireMockServer wireMock =
            new WireMockServer(WireMockConfiguration.options().dynamicPort());

    static {
        wireMock.start();
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void overrideDiscovery(DynamicPropertyRegistry registry) {
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("spring.cloud.config.enabled", () -> "false");
        registry.add("jwt.secret", () -> "dGVzdFNlY3JldEtleVBhcmFQcnVlYmFzRnVsbFN0YWNrMTIzNDU2Nzg=");
        registry.add("jwt.expiration", () -> "3600000");

        // En vez de resolver REQUEST-SERVICE / WAITING-LIST-SERVICE vía Eureka,
        // los apuntamos directo al WireMock que simula esos microservicios.
        registry.add("spring.cloud.discovery.client.simple.instances.REQUEST-SERVICE[0].uri",
                () -> "http://localhost:" + wireMock.port());
        registry.add("spring.cloud.discovery.client.simple.instances.WAITING-LIST-SERVICE[0].uri",
                () -> "http://localhost:" + wireMock.port());
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @BeforeEach
    void resetStubs() {
        wireMock.resetAll();
    }

    @Test
    @DisplayName("POST /bff/requests con estado PENDIENTE → crea la solicitud y además registra automáticamente al paciente en la lista de espera")
    void crearSolicitud_creaAutomaticamenteEntradaEnListaDeEspera() throws Exception {

        // Arrange: lo que "request-service" respondería
        RequestDTO solicitudCreada = new RequestDTO(
                10L, 1L, 5L, "Cardiología", "Palpitaciones", "PENDIENTE", null, null);

        wireMock.stubFor(post(urlEqualTo("/requests"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(solicitudCreada))));

        // Arrange: lo que "waiting-list-service" respondería
        wireMock.stubFor(post(urlEqualTo("/waiting-list"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        // Act: un único punto de entrada — el cliente solo conoce al BFF
        RequestDTO payload = new RequestDTO(
                null, 1L, 5L, "Cardiología", "Palpitaciones", null, null, null);

        ResponseEntity<RequestDTO> response = restTemplate.postForEntity(
                "/bff/requests", payload, RequestDTO.class);

        // Assert: respuesta correcta al cliente
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PENDIENTE", response.getBody().getEstado());

        // Assert: el BFF realmente llamó a request-service
        wireMock.verify(postRequestedFor(urlEqualTo("/requests")));

        // Assert: el BFF orquestó la llamada a waiting-list-service con los datos correctos
        wireMock.verify(postRequestedFor(urlEqualTo("/waiting-list"))
                .withRequestBody(matchingJsonPath("$.userId", equalTo("1")))
                .withRequestBody(matchingJsonPath("$.priority", equalTo("MEDIA")))
                .withRequestBody(matchingJsonPath("$.status", equalTo("EN_ESPERA"))));
    }

    @Test
    @DisplayName("POST /bff/requests con estado distinto de PENDIENTE → NO registra en lista de espera")
    void crearSolicitud_estadoNoPendiente_noTocaListaDeEspera() throws Exception {
        RequestDTO solicitudCreada = new RequestDTO(
                11L, 2L, 6L, "Pediatría", "Control", "CONFIRMADA", null, null);

        wireMock.stubFor(post(urlEqualTo("/requests"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(solicitudCreada))));

        RequestDTO payload = new RequestDTO(
                null, 2L, 6L, "Pediatría", "Control", "CONFIRMADA", null, null);

        restTemplate.postForEntity("/bff/requests", payload, RequestDTO.class);

        wireMock.verify(0, postRequestedFor(urlEqualTo("/waiting-list")));
    }
}