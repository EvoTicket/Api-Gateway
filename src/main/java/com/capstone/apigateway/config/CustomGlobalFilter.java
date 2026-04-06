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

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        mutatedExchange.getResponse().beforeCommit(() -> {
            ServerHttpResponse response = mutatedExchange.getResponse();
            if (!response.isCommitted()) {
                response.getHeaders().set(REQUEST_ID_HEADER, finalRequestId);
            }
            return Mono.empty();
        });

        long startTime = System.currentTimeMillis();

        String clientIp = request.getHeaders().getFirst("X-Forwarded-For");

        if (clientIp != null && clientIp.contains(",")) {
            clientIp = clientIp.split(",")[0];
        }

        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getHeaders().getFirst("X-Real-IP");
        }

        if (clientIp == null || clientIp.isEmpty()) {
            if (request.getRemoteAddress() != null &&
                    request.getRemoteAddress().getAddress() != null) {
                clientIp = request.getRemoteAddress().getAddress().getHostAddress();
            } else {
                clientIp = "unknown";
            }
        }

        log.info("[{}] Request: {} {} from {}",
                finalRequestId,
                request.getMethod(),
                request.getURI(),
                clientIp
        );

        return chain.filter(mutatedExchange)
                .doOnSuccess(aVoid -> {
                    ServerHttpResponse response = mutatedExchange.getResponse();
                    long duration = System.currentTimeMillis() - startTime;

                    log.info("[{}] Response status: {} in {}ms",
                            finalRequestId,
                            response.getStatusCode(),
                            duration
                    );
                })
                .doOnError(error -> {
                    long duration = System.currentTimeMillis() - startTime;

                    log.error("[{}] Error after {}ms: {}",
                            finalRequestId,
                            duration,
                            error.getMessage(),
                            error
                    );
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}