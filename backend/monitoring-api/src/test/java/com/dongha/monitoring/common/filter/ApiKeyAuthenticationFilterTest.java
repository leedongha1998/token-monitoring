package com.dongha.monitoring.common.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dongha.monitoring.project.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class ApiKeyAuthenticationFilterTest {

  @Mock private ApiKeyService apiKeyService;
  @Mock private FilterChain filterChain;
  private ApiKeyAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    filter = new ApiKeyAuthenticationFilter(apiKeyService);
  }

  @Test
  void X_API_Key_헤더가_없으면_401을_반환하고_체인을_호출하지_않는다() throws Exception {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    // when
    filter.doFilter(request, response, filterChain);

    // then
    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentType()).contains("application/json");
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  void 유효하지_않은_API_키면_401을_반환하고_체인을_호출하지_않는다() throws Exception {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-API-Key", "invalid-key");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(apiKeyService.validateKey("invalid-key")).thenReturn(false);

    // when
    filter.doFilter(request, response, filterChain);

    // then
    assertThat(response.getStatus()).isEqualTo(401);
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  void 유효한_API_키면_다음_필터로_통과한다() throws Exception {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-API-Key", "valid-key");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(apiKeyService.validateKey("valid-key")).thenReturn(true);

    // when
    filter.doFilter(request, response, filterChain);

    // then
    verify(filterChain).doFilter(any(), any());
  }

  @Test
  void 빈_문자열_API_키면_401을_반환한다() throws Exception {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-API-Key", "   ");
    MockHttpServletResponse response = new MockHttpServletResponse();

    // when
    filter.doFilter(request, response, filterChain);

    // then
    assertThat(response.getStatus()).isEqualTo(401);
    verify(filterChain, never()).doFilter(any(), any());
  }
}
