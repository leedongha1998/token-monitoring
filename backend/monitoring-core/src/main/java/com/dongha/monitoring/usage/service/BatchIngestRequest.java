package com.dongha.monitoring.usage.service;

import java.util.List;

public record BatchIngestRequest(List<UsageEventRequest> events) {}
