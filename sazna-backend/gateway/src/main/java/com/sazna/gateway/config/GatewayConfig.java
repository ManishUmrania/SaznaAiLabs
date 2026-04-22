package com.sazna.gateway.config;

import com.sazna.gateway.filter.JwtAuthenticationGatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("identity-service", r -> r.path("/api/users/**")
                        .filters(f -> f.filter((GatewayFilter) new JwtAuthenticationGatewayFilter()))
                        .uri("lb://identity-service"))
                .route("cipher-service", r -> r.path("/api/auth/**")
                        .filters(f -> f.filter((GatewayFilter) new JwtAuthenticationGatewayFilter() ))
                        .uri("lb://cipher-service"))
                .build();
    }
}