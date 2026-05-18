package com.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.config.server.git.uri=file:///tmp/test-config-repo"
})
class ConfigServerApplicationTests {

    @Test
    void contextLoads() {
    }
}