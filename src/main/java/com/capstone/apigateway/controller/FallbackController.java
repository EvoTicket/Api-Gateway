package com.capstone.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/fallback")
    public Mono<ResponseEntity<Map<String, String>>> fallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Service temporarily unavailable",
                        "message", "Please try again later"
                )));
    }
}
