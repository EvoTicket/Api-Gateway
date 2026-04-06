package com.capstone.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RedisRateLimiterConfig {

    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            var request = exchange.getRequest();

            String ip = request.getHeaders().getFirst("X-Forwarded-For");

            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0];
            }

            if (ip == null || ip.isEmpty()) {
                ip = request.getHeaders().getFirst("X-Real-IP");
            }

            if (ip == null || ip.isEmpty()) {
                if (request.getRemoteAddress() != null &&
                        request.getRemoteAddress().getAddress() != null) {
                    ip = request.getRemoteAddress().getAddress().getHostAddress();
                } else {
                    ip = "unknown";
                }
            }

            return Mono.just(ip);
        };
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return Mono.just(authHeader.substring(7));
            }
            return Mono.just("anonymous");
        };
    }
}
