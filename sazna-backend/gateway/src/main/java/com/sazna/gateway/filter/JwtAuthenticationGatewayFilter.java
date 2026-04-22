package com.sazna.gateway.filter;

import com.sazna.security.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtAuthenticationGatewayFilter extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilter.Config> {

    @Autowired
    private JwtTokenProvider tokenProvider;

    public JwtAuthenticationGatewayFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Skip authentication for public endpoints
            if (isPublicEndpoint(request)) {
                return chain.filter(exchange);
            }

            // Extract JWT token from Authorization header
            String token = extractToken(request);

            if (token == null || !validateToken(token)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Add user info to request headers
            String userId = tokenProvider.getUserIdFromToken(token).toString();
            String username = tokenProvider.getUsernameFromToken(token);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-USER-ID", userId)
                    .header("X-USERNAME", username)
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
            return chain.filter(mutatedExchange);
        };
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        String path = request.getPath().value();
        return path.contains("/api/users/signup") ||
               path.contains("/api/users/login") ||
               path.contains("/api/auth/login") ||
               path.contains("/api/users/validate");
    }

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            return tokenProvider.validateToken(token);
        } catch (Exception e) {
            log.error("JWT validation failed", e);
            return false;
        }
    }

    public static class Config {
        // Configuration properties if needed
    }
}