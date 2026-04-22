package com.sazna.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();

        String requestId = exchange.getRequest().getId();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();

        log.info("Incoming request - ID: {}, Method: {}, Path: {}", requestId, method, path);

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    int status = exchange.getResponse().getStatusCode() != null ?
                        exchange.getResponse().getStatusCode().value() : 0;
                    log.info("Response - ID: {}, Status: {}, Duration: {}ms", requestId, status, duration);
                }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}