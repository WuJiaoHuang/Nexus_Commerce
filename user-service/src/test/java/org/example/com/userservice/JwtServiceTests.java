package org.example.com.userservice;

import org.example.com.userservice.config.JwtService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTests {

    private static final String DEV_SECRET = "bmV4dXNfZGV2X2p3dF9zZWNyZXRfMzJfYnl0ZXMhISE=";

    @Test
    void generatedTokenCanBeParsedAndValidated() {
        JwtService jwtService = new JwtService(DEV_SECRET);

        String token = jwtService.generateToken("alice");

        assertEquals("alice", jwtService.extractUsername(token));
        assertTrue(jwtService.validateToken(token, "alice"));
    }

    @Test
    void invalidTokenIsRejected() {
        JwtService jwtService = new JwtService(DEV_SECRET);

        assertFalse(jwtService.validateToken("not-a-jwt", "alice"));
    }
}
