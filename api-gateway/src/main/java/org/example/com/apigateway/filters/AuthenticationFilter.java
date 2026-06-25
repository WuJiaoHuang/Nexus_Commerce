package org.example.com.apigateway.filters;

import ch.qos.logback.core.util.StringUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
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
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod() == null ? "" : request.getMethod().name();
        if ("/user/login".equals(path) || ("/user".equals(path) && "POST".equalsIgnoreCase(method))) {
            return chain.filter(exchange);
        }
        HttpHeaders headers = request.getHeaders();
        String role = headers.getFirst("role");
        if (StringUtil.isNullOrEmpty(role) || !"admin".equalsIgnoreCase(role)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);

    }

}
