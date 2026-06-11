plugins {
  id("com.diffplug.spotless") version "7.0.2" apply false
}

val springBootVersion = "3.3.5"

subprojects {
  apply(plugin = "java")
  apply(plugin = "com.diffplug.spotless")

  repositories {
    mavenCentral()
  }

  configure<JavaPluginExtension> {
    toolchain {
      languageVersion = JavaLanguageVersion.of(21)
    }
  }

  dependencies {
    "implementation"(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    "testImplementation"(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
  }

  configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    encoding("UTF-8")
    lineEndings = com.diffplug.spotless.LineEnding.UNIX
    java {
      googleJavaFormat()
    }
  }

  tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
  }

  tasks.withType<Test> {
    useJUnitPlatform()
  }

  tasks.named("check") {
    dependsOn("spotlessCheck")
  }
}
