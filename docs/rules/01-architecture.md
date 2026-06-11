# 아키텍처 규칙

이 문서의 레이어/모듈 규칙은 ArchUnit 테스트(`monitoring-core/src/test/.../ArchitectureTest.java`)로
강제된다. 규칙을 어기면 빌드가 실패한다. **규칙이 불합리하다고 판단되면 ArchUnit 테스트를
고치지 말고 사용자에게 보고한다.**

## 1. 모듈 구조 (Gradle multi-module)

```
token-monitoring/
├── monitoring-core      # 도메인 엔티티, 리포지토리, 도메인 서비스
├── monitoring-api       # REST 컨트롤러, 인증 필터, DTO
├── monitoring-batch     # JSONL 파서, daily rollup 배치
└── monitoring-client    # (후순위) Spring Boot Starter
```

### 의존 방향 (단방향, 위반 시 빌드 실패)

```
monitoring-api    → monitoring-core
monitoring-batch  → monitoring-core
monitoring-core   → (다른 모듈 의존 금지)
```

- core는 web 의존성(spring-boot-starter-web)을 가질 수 없다
- api와 batch는 서로를 참조할 수 없다
- 순환 의존은 어떤 경우에도 금지

## 2. 레이어 규칙 (모듈 내부)

```
controller → service → repository
```

- controller는 repository를 직접 참조할 수 없다
- controller는 다른 controller를 참조할 수 없다
- repository는 service/controller를 참조할 수 없다
- 도메인 엔티티는 어떤 레이어에도 의존하지 않는다 (POJO 유지)

## 3. 패키지 구조

도메인 기준으로 패키지를 나눈다 (레이어 기준 금지):

```
com.dongha.monitoring
├── project/          # Project, ApiKey
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── domain/
├── usage/            # UsageEvent, ingestion
├── pricing/          # ModelPricing
└── rollup/           # DailyRollup
└── common/           # 공통 예외, 응답 포맷, 설정
```

## 4. Entity / DTO 규칙

- 엔티티를 컨트롤러 요청/응답에 직접 노출하는 것을 금지한다
- 요청 DTO: `{도메인}Request` (record 사용), 응답 DTO: `{도메인}Response` (record 사용)
- 엔티티 ↔ DTO 변환은 DTO의 정적 팩토리 메서드(`from(entity)`) 또는 전용 매퍼에서 수행
- 엔티티의 setter 사용 금지. 상태 변경은 의도가 드러나는 메서드로 (`deactivate()`, `rotate()`)

## 5. 영속성 규칙

- 스키마 변경은 **Flyway 마이그레이션으로만** 수행한다 (`db/migration/V{n}__설명.sql`)
- `ddl-auto: validate` 고정. 변경 금지
- 테이블/컬럼은 snake_case, 엔티티/필드는 camelCase
- `usage_event` 테이블은 `occurred_at` 기준 월 단위 파티셔닝을 유지한다
- N+1 가능성이 있는 연관관계는 기본 LAZY + fetch join/EntityGraph로 해결

## 6. 기술 스택 (고정 — 변경/추가는 사용자 승인 필수)

| 영역 | 선택 | 비고 |
|---|---|---|
| Language | Java 21 | |
| Framework | Spring Boot 3.x | |
| Build | Gradle (Kotlin DSL) | |
| DB | PostgreSQL 16 | |
| Migration | Flyway | |
| ORM | Spring Data JPA | QueryDSL 등 추가 금지 |
| Test | JUnit 5, AssertJ, Testcontainers, ArchUnit | Mockito 최소화 |
| Format | Spotless (google-java-format) | |

위 표에 없는 라이브러리가 필요하다고 판단되면, 추가하지 말고
**왜 필요한지 + 대안 비교**를 사용자에게 보고한 뒤 승인을 기다린다.
