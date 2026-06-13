package com.dongha.monitoring.client;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(MonitoringClientProperties.class)
public class MonitoringClientAutoConfiguration {

  @Bean
  public MonitoringClientService monitoringClientService(MonitoringClientProperties properties) {
    return new MonitoringClientService(properties);
  }
}
