package com.dongha.monitoring.project.service;

import java.time.Instant;

public record ApiKeyResult(Long id, String prefix, String rawKey, Instant createdAt) {}
