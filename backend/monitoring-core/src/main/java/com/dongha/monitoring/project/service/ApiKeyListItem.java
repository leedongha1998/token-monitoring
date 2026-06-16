package com.dongha.monitoring.project.service;

import java.time.Instant;

public record ApiKeyListItem(Long id, String prefix, boolean active, Instant createdAt) {}
