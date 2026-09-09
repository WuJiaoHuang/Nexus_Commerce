package org.example.com.aiassistantservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class AiAssistantServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
