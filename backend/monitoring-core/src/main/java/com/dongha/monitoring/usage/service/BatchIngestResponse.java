package com.dongha.monitoring.usage.service;

import java.util.List;

public record BatchIngestResponse(int accepted, int duplicated, List<IngestResult> results) {}
