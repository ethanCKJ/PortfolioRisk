package com.portfoliorisk.marketdata;

public record HealthResponse(
    HealthStatus status,
    String service
) {
}


