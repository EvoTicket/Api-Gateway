package com.capstone.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);

        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        final String finalRequestId = requestId;

        ServerHttpRequest mutatedRequest = request.mutate()
                .header(REQUEST_ID_HEADER, finalRequestId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        long startTime = System.currentTimeMillis();

        log.info("[{}] Request: {} {} from {}", finalRequestId, request.getMethod(), request.getURI(),
                request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown");

        return chain.filter(mutatedExchange)
                .doOnSuccess(aVoid -> {
                    ServerHttpResponse response = mutatedExchange.getResponse();
                    long duration = System.currentTimeMillis() - startTime;

                    log.info("[{}] Response status: {} in {}ms", finalRequestId, response.getStatusCode(), duration);
                })
                .doOnSubscribe(sub -> {
                    mutatedExchange.getResponse().beforeCommit(() -> {
                        mutatedExchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, finalRequestId);
                        return Mono.empty();
                    });
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
