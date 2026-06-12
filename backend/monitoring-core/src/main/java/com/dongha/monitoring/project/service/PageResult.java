package com.dongha.monitoring.project.service;

import java.util.List;

public record PageResult<T>(List<T> content, long totalElements, int totalPages, int number) {}
