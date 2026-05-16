package com;

public record HealthResponse(
    HealthStatus status,
    String service
) {
}


