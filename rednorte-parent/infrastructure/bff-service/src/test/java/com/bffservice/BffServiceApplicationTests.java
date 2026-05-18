package com.bffservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false",
    "jwt.secret=dGVzdFNlY3JldEtleVBhcmFQcnVlYmFzRnVsbFN0YWNrMTIzNDU2Nzg=",
    "jwt.expiration=3600000"
})
class BffServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}