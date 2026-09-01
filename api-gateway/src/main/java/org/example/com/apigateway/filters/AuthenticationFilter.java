package org.example.com.apigateway.filters;

import org.example.com.securityplatform.jwt.JwtTokenValidator;
import org.example.com.securityplatform.jwt.JwtValidationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(1)
public class AuthenticationFilter implements GlobalFilter {

    private final JwtTokenValidator jwtTokenValidator;

    public AuthenticationFilter(JwtTokenValidator jwtTokenValidator) {
        this.jwtTokenValidator = jwtTokenValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod() == null ? "" : request.getMethod().name();
        if (isPublicEndpoint(path, method)) {
            return chain.filter(exchange);
        }

        HttpHeaders headers = request.getHeaders();
        String token = jwtTokenValidator.stripBearerPrefix(headers.getFirst(HttpHeaders.AUTHORIZATION));
        JwtValidationResult validation = jwtTokenValidator.validate(token);
        if (!validation.valid()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        ServerHttpRequest authenticatedRequest = request.mutate()
                .headers(httpHeaders -> {
                    httpHeaders.remove("X-User-Id");
                    httpHeaders.remove("X-User-Name");
                })
                .header("X-User-Name", validation.username())
                .build();
        return chain.filter(exchange.mutate().request(authenticatedRequest).build());
    }

    private boolean isPublicEndpoint(String path, String method) {
        return "/user/login".equals(path) || ("/user".equals(path) && "POST".equalsIgnoreCase(method));
    }

    @Configuration
    static class JwtValidatorConfiguration {
        @Bean
        JwtTokenValidator jwtTokenValidator(@Value("${app.security.jwt-secret}") String jwtSecret) {
            return new JwtTokenValidator(jwtSecret);
        }
    }
}
