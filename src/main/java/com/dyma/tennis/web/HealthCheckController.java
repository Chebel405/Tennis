package com.dyma.tennis.web;

import com.dyma.tennis.HealthCheck;
import com.dyma.tennis.service.HealthCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @Autowired
    private HealthCheckService healthCheckService;
    @GetMapping("/healthcheck")
    public HealthCheck healthcheck(){
        return healthCheckService.healthcheck();
    }
}
