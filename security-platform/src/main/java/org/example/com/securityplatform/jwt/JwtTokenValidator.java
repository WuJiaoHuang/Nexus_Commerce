package org.example.com.securityplatform.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Base64;

public class JwtTokenValidator {

    private final SecretKey secretKey;

    public JwtTokenValidator(String base64Secret) {
        if (base64Secret == null || base64Secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret must not be blank");
        }
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
    }

    public JwtValidationResult validate(String token) {
        if (token == null || token.isBlank()) {
            return JwtValidationResult.failure("Missing token");
        }
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                return JwtValidationResult.failure("Token subject is missing");
            }
            return JwtValidationResult.success(subject);
        } catch (JwtException | IllegalArgumentException e) {
            return JwtValidationResult.failure("Invalid token");
        }
    }

    public String stripBearerPrefix(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}
