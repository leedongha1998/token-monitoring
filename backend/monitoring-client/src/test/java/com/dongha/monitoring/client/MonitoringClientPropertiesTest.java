package com.dongha.monitoring.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = MonitoringClientPropertiesTest.Config.class)
class MonitoringClientPropertiesTest {

  @EnableConfigurationProperties(MonitoringClientProperties.class)
  static class Config {}

  @Autowired MonitoringClientProperties properties;

  @Test
  void 기본값이_올바르게_설정된다() {
    assertThat(properties.serverUrl()).isEqualTo("http://localhost:8080");
    assertThat(properties.enabled()).isTrue();
    assertThat(properties.connectTimeoutMs()).isEqualTo(3000);
    assertThat(properties.readTimeoutMs()).isEqualTo(5000);
  }

  @Test
  void 레코드_생성자로_커스텀_설정값이_적용된다() {
    MonitoringClientProperties custom =
        new MonitoringClientProperties(
            "test-key", "https://monitoring.example.com", false, 1000, 2000);

    assertThat(custom.apiKey()).isEqualTo("test-key");
    assertThat(custom.serverUrl()).isEqualTo("https://monitoring.example.com");
    assertThat(custom.enabled()).isFalse();
    assertThat(custom.connectTimeoutMs()).isEqualTo(1000);
    assertThat(custom.readTimeoutMs()).isEqualTo(2000);
  }
}
