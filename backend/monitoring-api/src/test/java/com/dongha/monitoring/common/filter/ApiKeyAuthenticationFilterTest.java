package com.dongha.monitoring.common.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dongha.monitoring.project.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import java.util.Optional;
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
  void events_경로에서_X_API_Key_헤더가_없으면_401을_반환하고_체인을_호출하지_않는다() throws Exception {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/v1/events");
    MockHttpServletResponse response = new MockHttpServletResponse();

    // when
    filter.doFilter(request, response, filterChain);

    // then
    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentType()).contains("application/json");
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  void events_경로에서_유효하지_않은_API_키면_401을_반환하고_체인을_호출하지_않는다() throws Exception {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/v1/events");
    request.addHeader("X-API-Key", "invalid-key");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(apiKeyService.findProjectIdByKey("invalid-key")).thenReturn(Optional.empty());

    // when
    filter.doFilter(request, response, filterChain);

    // then
    assertThat(response.getStatus()).isEqualTo(401);
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  void events_경로에서_유효한_API_키면_다음_필터로_통과한다() throws Exception {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/v1/events");
    request.addHeader("X-API-Key", "valid-key");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(apiKeyService.findProjectIdByKey("valid-key")).thenReturn(Optional.of(1L));

    // when
    filter.doFilter(request, response, filterChain);

    // then
    verify(filterChain).doFilter(any(), any());
  }

  @Test
  void events_경로에서_빈_문자열_API_키면_401을_반환한다() throws Exception {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/v1/events");
    request.addHeader("X-API-Key", "   ");
    MockHttpServletResponse response = new MockHttpServletResponse();

    // when
    filter.doFilter(request, response, filterChain);

    // then
    assertThat(response.getStatus()).isEqualTo(401);
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  void 관리_경로는_API_키_없이도_필터를_건너뛴다() throws Exception {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/v1/projects");
    MockHttpServletResponse response = new MockHttpServletResponse();

    // when
    filter.doFilter(request, response, filterChain);

    // then
    verify(filterChain).doFilter(any(), any());
    assertThat(response.getStatus()).isNotEqualTo(401);
  }

  @Test
  void api_keys_경로는_API_키_없이도_필터를_건너뛴다() throws Exception {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/v1/api-keys/1");
    MockHttpServletResponse response = new MockHttpServletResponse();

    // when
    filter.doFilter(request, response, filterChain);

    // then
    verify(filterChain).doFilter(any(), any());
    assertThat(response.getStatus()).isNotEqualTo(401);
  }
}
