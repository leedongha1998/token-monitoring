package com.dongha.monitoring.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "monitoring.client")
public record MonitoringClientProperties(
    String apiKey,
    @DefaultValue("http://localhost:8080") String serverUrl,
    @DefaultValue("true") boolean enabled,
    @DefaultValue("3000") int connectTimeoutMs,
    @DefaultValue("5000") int readTimeoutMs) {}
