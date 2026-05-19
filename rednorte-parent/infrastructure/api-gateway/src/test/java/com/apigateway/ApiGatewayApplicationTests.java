package com.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false",
    "spring.security.oauth2.resourceserver.jwt.secret-value=dGVzdFNlY3JldEtleVBhcmFQcnVlYmFzRnVsbFN0YWNrMTIzNDU2Nzg="
})
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}