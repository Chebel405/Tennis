package com.dyma.tennis.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @GetMapping("/healthcheck")
    public HealthCheck healthCheck(){
        return new HealthCheck(ApplicationStatus.OK, "Welcome to Dyma Tennis!");
    }
}
