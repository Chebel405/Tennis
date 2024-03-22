package com.dyma.tennis.service;

import com.dyma.tennis.ApplicationStatus;
import com.dyma.tennis.HealthCheck;
import org.springframework.stereotype.Service;

@Service
public class HealthCheckService {
    public HealthCheck healthCheck(){
        return new HealthCheck(ApplicationStatus.OK, "Welcome to Dyma Tennis!");
    }
}
