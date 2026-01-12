package com.capstone.apigateway.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;
import io.jsonwebtoken.Claims;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .anyExchange().permitAll()
                )

                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable);

        return http.build();
    }

    @Bean
    public KeyResolver jwtUserKeyResolver() {
        return exchange -> {
            String auth = exchange.getRequest()
                    .getHeaders()
                    .getFirst("Authorization");

            if (auth == null || !auth.startsWith("Bearer ")) {
                return Mono.just("anonymous");
            }
            String token = auth.substring(7);

            byte[] keyBytes = Decoders.BASE64.decode("");

            Claims claims = Jwts
                    .parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Long userId = claims.get("userId", Long.class);

            System.out.println("RATE LIMIT USER = " + userId);

            return Mono.just(String.valueOf(userId));
        };
    }
}
