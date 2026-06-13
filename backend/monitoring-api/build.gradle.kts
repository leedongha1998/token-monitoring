plugins {
  id("org.springframework.boot") version "3.3.5"
}

dependencies {
  implementation(project(":monitoring-core"))
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
}
