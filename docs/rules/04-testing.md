# 테스트 규칙

테스트는 에이전트 작업 결과를 검증하는 핵심 게이트다.
08-definition-of-done.md에 따라 전체 테스트 통과 없이는 어떤 작업도 완료가 아니다.

## 1. 레이어별 테스트 전략

| 대상 | 테스트 종류 | 도구 | DB |
|---|---|---|---|
| 도메인 로직 (엔티티, 도메인 서비스) | 순수 단위 테스트 | JUnit 5 + AssertJ | 없음 |
| Repository (쿼리, 파티셔닝, JSONB) | 슬라이스 테스트 | @DataJpaTest | Testcontainers PostgreSQL |
| Controller (요청/응답, 인증 필터) | 슬라이스 테스트 | @WebMvcTest + MockMvc | 없음 (service mock) |
| 핵심 시나리오 (ingestion 전체 흐름) | 통합 테스트 | @SpringBootTest | Testcontainers PostgreSQL |
| 아키텍처 규칙 | ArchUnit | ArchitectureTest.java | 없음 |

- 비중 가이드: 단위 > 슬라이스 > 통합. **@SpringBootTest 남용 금지** —
  통합 테스트는 도메인당 핵심 시나리오 1~2개로 제한한다 (빌드 시간 보호)
- **H2 사용 금지.** usage_event는 PostgreSQL 파티셔닝과 JSONB를 사용하므로
  H2로는 실제 동작을 검증할 수 없다. DB가 필요한 테스트는 반드시
  Testcontainers PostgreSQL 16을 사용한다 (싱글톤 컨테이너 패턴으로 재사용)

## 2. 작성 규칙

- 구조는 given / when / then 주석으로 구분한다
- 테스트 메서드명은 한국어 백틱 네이밍으로 동작을 서술한다:
  ```java
  @Test
  void 동일한_idempotency_key로_재요청하면_이벤트가_중복_저장되지_않는다() { ... }
  ```
- 하나의 테스트는 하나의 동작만 검증한다 (assert 여러 개는 허용,
  서로 다른 시나리오를 한 테스트에 섞는 것은 금지)
- 검증은 AssertJ만 사용한다 (`assertThat`). JUnit 기본 assert 금지
- 테스트 간 실행 순서 의존 금지. 각 테스트는 독립적으로 통과해야 한다
- `Thread.sleep()` 사용 금지. 비동기 검증은 Awaitility를 사용한다

## 3. 테스트 데이터

- 테스트 데이터 생성은 도메인별 픽스처 빌더로 통일한다:
  ```java
  // src/test/java/.../fixture/UsageEventFixture.java
  UsageEvent event = UsageEventFixture.builder()
      .model("claude-sonnet-4-5")
      .inputTokens(1024)
      .build();
  ```
- 테스트 안에서 엔티티를 직접 new 하거나 긴 셋업 코드를 반복하지 않는다
- 시간 의존 로직(rollup 날짜 경계, pricing effective_from)은
  `Clock`을 주입받아 고정 시각으로 테스트한다. `LocalDate.now()` 직접 호출 금지

## 4. 반드시 테스트해야 하는 경계 케이스

이 서비스의 도메인 특성상 다음은 테스트 없이 완료로 간주하지 않는다:

- idempotency: 동일 키 재요청, 동시 요청(unique 제약 충돌 처리)
- pricing: effective_from 경계일(당일/전일), 단가 이력이 없는 모델
- rollup: 일 경계(자정, KST/UTC), 재실행 시 멱등성
- batch ingestion: 일부 실패 시 응답 (전체 롤백인지 부분 성공인지 — API 명세대로)
- 인증: 잘못된 API key, 만료된 key, 비활성 key

## 5. AI 에이전트 금지 사항

- 실패하는 테스트의 기대값을 구현 결과에 맞춰 수정하는 행위
  (기대값이 잘못됐다고 판단되면 수정하지 말고 보고한다)
- 검증 없는 테스트(assert 없이 실행만 하는 테스트)로 커버리지를 채우는 행위
- mock 검증만으로 끝나는 테스트 남용 — repository/통합 레이어는 실제 동작을 검증한다
- @Disabled, @Ignore 사용 (08-definition-of-done.md §2와 동일)
- 테스트 통과를 위한 프로덕션 코드의 테스트 전용 분기 추가
