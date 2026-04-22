package com.sazna.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Order(-2) // Ensure this runs before the default error handler
@Configuration
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Error occurred: ", ex);

        // Prepare error response
        Map<String, Object> errorAttributes = new HashMap<>();
        errorAttributes.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorAttributes.put("error", "Internal Server Error");
        errorAttributes.put("message", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred");
        errorAttributes.put("timestamp", System.currentTimeMillis());

        // Set response status and content type
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            // Convert error attributes to JSON
            String jsonResponse = objectMapper.writeValueAsString(errorAttributes);

            // Write error response
            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse()
                            .bufferFactory()
                            .wrap(jsonResponse.getBytes())));
        } catch (Exception e) {
            log.error("Failed to serialize error response", e);
            return exchange.getResponse().setComplete();
        }
    }
}