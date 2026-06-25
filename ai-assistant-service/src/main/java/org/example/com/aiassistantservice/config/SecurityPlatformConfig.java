package org.example.com.aiassistantservice.config;

import org.example.com.securityplatform.jwt.JwtTokenValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityPlatformConfig {

    @Bean
    public JwtTokenValidator jwtTokenValidator(@Value("${app.security.jwt-secret}") String jwtSecret) {
        return new JwtTokenValidator(jwtSecret);
    }
}
