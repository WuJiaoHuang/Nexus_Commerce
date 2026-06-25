package org.example.com.securityplatform;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.com.securityplatform.jwt.JwtTokenValidator;
import org.example.com.securityplatform.jwt.JwtValidationResult;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenValidatorTests {

    @Test
    void validatesToken() {
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String base64 = Base64.getEncoder().encodeToString(key.getEncoded());
        String token = Jwts.builder()
                .subject("alice")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        JwtTokenValidator validator = new JwtTokenValidator(base64);
        JwtValidationResult result = validator.validate(token);

        assertTrue(result.valid());
        assertEquals("alice", result.username());
    }
}
