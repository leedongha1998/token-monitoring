package com.dongha.monitoring.project.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProjectTest {

  @Test
  void create_메서드로_활성_프로젝트를_생성한다() {
    // given
    String name = "test-project";
    String description = "테스트 설명";

    // when
    Project project = Project.create(name, description);

    // then
    assertThat(project.getName()).isEqualTo(name);
    assertThat(project.getDescription()).isEqualTo(description);
    assertThat(project.isActive()).isTrue();
    assertThat(project.getCreatedAt()).isNotNull();
    assertThat(project.getId()).isNull();
  }

  @Test
  void deactivate_호출_시_프로젝트가_비활성화된다() {
    // given
    Project project = Project.create("test", null);
    assertThat(project.isActive()).isTrue();

    // when
    project.deactivate();

    // then
    assertThat(project.isActive()).isFalse();
  }
}
