package com.dongha.monitoring.common.filter;

import com.dongha.monitoring.project.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

  private static final String API_KEY_HEADER = "X-API-Key";
  private static final String UNAUTHORIZED_TEMPLATE =
      "{\"code\":\"AUTH-001\",\"message\":\"유효하지 않은 API 키입니다\",\"timestamp\":\"%s\"}";

  private final ApiKeyService apiKeyService;

  public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
    this.apiKeyService = apiKeyService;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    // X-API-Key 인증은 수집 API(/v1/events/**)에만 적용한다. 관리 API는 내부망 가정.
    return !request.getRequestURI().startsWith("/v1/events");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String rawKey = request.getHeader(API_KEY_HEADER);
    if (rawKey == null || rawKey.isBlank()) {
      writeUnauthorized(response);
      return;
    }
    Optional<Long> projectId = apiKeyService.findProjectIdByKey(rawKey);
    if (projectId.isEmpty()) {
      writeUnauthorized(response);
      return;
    }
    request.setAttribute("authenticatedProjectId", projectId.get());
    chain.doFilter(request, response);
  }

  private void writeUnauthorized(HttpServletResponse response) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    response
        .getWriter()
        .write(String.format(UNAUTHORIZED_TEMPLATE, OffsetDateTime.now(ZoneOffset.UTC)));
  }
}
