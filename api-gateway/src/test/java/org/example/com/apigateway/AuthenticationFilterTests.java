package org.example.com.apigateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.com.apigateway.filters.AuthenticationFilter;
import org.example.com.securityplatform.jwt.JwtTokenValidator;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationFilterTests {

    private final SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
    private final AuthenticationFilter filter = new AuthenticationFilter(
            new JwtTokenValidator(Base64.getEncoder().encodeToString(key.getEncoded())));

    @Test
    void publicLoginDoesNotRequireToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/user/login").build());

        filter.filter(exchange, passthrough()).block();

        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void protectedRouteWithoutTokenReturnsUnauthorized() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/product/p-1").build());

        filter.filter(exchange, passthrough()).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void validTokenAddsTrustedUserHeaderAndRemovesSpoofedHeader() {
        String token = token("alice", new Date(System.currentTimeMillis() + 60_000));
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/product/p-1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header("X-User-Name", "mallory")
                        .build());
        AtomicReference<String> forwardedUser = new AtomicReference<>();
        GatewayFilterChain chain = mutatedExchange -> {
            forwardedUser.set(mutatedExchange.getRequest().getHeaders().getFirst("X-User-Name"));
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertEquals("alice", forwardedUser.get());
    }

    @Test
    void expiredTokenReturnsUnauthorized() {
        String token = token("alice", new Date(System.currentTimeMillis() - 60_000));
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/orders/o-1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build());

        filter.filter(exchange, passthrough()).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    private GatewayFilterChain passthrough() {
        return exchange -> Mono.empty();
    }

    private String token(String subject, Date expiration) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(key)
                .compact();
    }
}
