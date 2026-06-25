package org.example.com.userservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private final String secretKey;

    public JwtService(@Value("${app.security.jwt-secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 15))
                .and()
                .signWith(getKey())
                .compact();
    }

    public SecretKey getKey(){
        byte[] encodedKey = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(encodedKey);
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();

    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
    public boolean validateToken(String token,String username) {
        final String extractUsername = extractUsername(token);
        return (extractUsername(token).equals(username) && !isTokenValid(token));
    }

}
