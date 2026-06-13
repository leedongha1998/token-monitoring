package com.dongha.monitoring.usage.service;

public record IngestResult(String idempotencyKey, IngestStatus status) {}
