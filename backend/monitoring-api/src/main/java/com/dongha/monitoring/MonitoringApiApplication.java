package com.dongha.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MonitoringApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(MonitoringApiApplication.class, args);
  }
}
