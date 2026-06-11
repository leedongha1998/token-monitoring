package com.dongha.monitoring;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** docs/rules/01-architecture.md 의 규칙을 빌드 타임에 강제한다. 이 테스트가 실패하면 코드를 고쳐야 한다. 테스트를 고치는 것이 아니다. */
@AnalyzeClasses(
    packages = "com.dongha.monitoring",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  @ArchTest
  static final ArchRule controllers_must_not_access_repositories =
      noClasses()
          .that()
          .resideInAPackage("..controller..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..repository..")
          .allowEmptyShould(true)
          .because("controller는 service를 통해서만 데이터에 접근한다 (01-architecture.md §2)");

  @ArchTest
  static final ArchRule repositories_must_not_access_services =
      noClasses()
          .that()
          .resideInAPackage("..repository..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..service..", "..controller..")
          .allowEmptyShould(true)
          .because("의존 방향은 controller → service → repository 단방향이다");

  @ArchTest
  static final ArchRule controllers_must_not_depend_on_other_controllers =
      noClasses()
          .that()
          .resideInAPackage("..controller..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..controller..")
          .allowEmptyShould(true)
          .because("controller 간 직접 참조 금지 (01-architecture.md §2)");

  @ArchTest
  static final ArchRule domain_must_not_depend_on_upper_layers =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..controller..", "..service..", "..repository..")
          .allowEmptyShould(true)
          .because("엔티티는 POJO를 유지한다 (01-architecture.md §2)");

  @ArchTest
  static final ArchRule entities_must_not_be_used_in_controllers =
      noClasses()
          .that()
          .resideInAPackage("..controller..")
          .should()
          .dependOnClassesThat()
          .areAnnotatedWith(jakarta.persistence.Entity.class)
          .allowEmptyShould(true)
          .because("컨트롤러는 DTO만 다룬다. 엔티티 직접 노출 금지 (01-architecture.md §4)");

  @ArchTest
  static final ArchRule controllers_naming =
      classes()
          .that()
          .resideInAPackage("..controller..")
          .and()
          .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
          .should()
          .haveSimpleNameEndingWith("Controller")
          .allowEmptyShould(true)
          .because("컨트롤러 클래스는 Controller 접미사를 가진다 (02-coding-convention.md §1)");

  @ArchTest
  static final ArchRule no_cycles_between_domain_packages =
      slices()
          .matching("com.dongha.monitoring.(*)..")
          .should()
          .beFreeOfCycles()
          .allowEmptyShould(true)
          .because("도메인 패키지 간 순환 의존 금지 (01-architecture.md §1)");
}
