package com.dyma.tennis.rest;

public record HealthCheck(ApplicationStatus status, String message) {
}
