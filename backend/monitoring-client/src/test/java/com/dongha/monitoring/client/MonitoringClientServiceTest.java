package com.dongha.monitoring.client;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonitoringClientServiceTest {

  @Mock HttpClient mockHttpClient;

  @SuppressWarnings("unchecked")
  @Mock
  HttpResponse<String> mockResponse;

  private MonitoringClientProperties props(boolean enabled) {
    return new MonitoringClientProperties("test-key", "http://localhost:8080", enabled, 3000, 5000);
  }

  @Test
  void enabled가_false이면_HTTP_요청을_보내지_않는다() throws Exception {
    // given
    MonitoringClientService service = new MonitoringClientService(props(false), mockHttpClient);

    // when
    service.sendEvent("claude-sonnet-4-5", 100, 50, "key-001");

    // then
    verify(mockHttpClient, never()).send(any(), any());
  }

  @Test
  @SuppressWarnings("unchecked")
  void enabled가_true이고_202_응답이면_정상_완료된다() throws Exception {
    // given
    when(mockResponse.statusCode()).thenReturn(202);
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);
    MonitoringClientService service = new MonitoringClientService(props(true), mockHttpClient);

    // when & then (예외 없이 완료)
    assertThatCode(() -> service.sendEvent("claude-sonnet-4-5", 100, 50, "key-002"))
        .doesNotThrowAnyException();
  }

  @Test
  @SuppressWarnings("unchecked")
  void HTTP_오류가_발생해도_예외를_전파하지_않는다() throws Exception {
    // given
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenThrow(new IOException("connection refused"));
    MonitoringClientService service = new MonitoringClientService(props(true), mockHttpClient);

    // when & then (예외가 전파되지 않음)
    assertThatCode(() -> service.sendEvent("claude-sonnet-4-5", 100, 50, "key-003"))
        .doesNotThrowAnyException();
  }

  @Test
  @SuppressWarnings("unchecked")
  void HTTP_상태코드가_202가_아니어도_예외를_전파하지_않는다() throws Exception {
    // given
    when(mockResponse.statusCode()).thenReturn(500);
    when(mockResponse.body()).thenReturn("Internal Server Error");
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);
    MonitoringClientService service = new MonitoringClientService(props(true), mockHttpClient);

    // when & then
    assertThatCode(() -> service.sendEvent("claude-sonnet-4-5", 100, 50, "key-004"))
        .doesNotThrowAnyException();
  }
}
