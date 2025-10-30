package com.capstone.apigateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Routes {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Route cho Swagger API docs
                .route("iam_service_swagger", r -> r
                        .path("/iam-service/v3/api-docs/**")
                        .filters(f -> f
                                .rewritePath("/iam-service/v3/api-docs(?<segment>.*)", "/v3/api-docs${segment}")
                        )
                        .uri("lb://iam-service")
                )
                // Route cho các API thông thường
                .route("iam-service", r -> r
                        .path("/iam-service/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://iam-service")
                )
                .build();
    }
}
