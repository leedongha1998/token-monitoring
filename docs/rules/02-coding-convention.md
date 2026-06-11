# 코딩 컨벤션

코드 포맷(들여쓰기, 줄바꿈, import 순서)은 Spotless(google-java-format)가 강제하므로
이 문서에서 다루지 않는다. 여기서는 포맷터가 잡지 못하는 설계 수준 규칙만 정의한다.

## 1. 네이밍

- 클래스: 역할 접미사 고정 — `Controller`, `Service`, `Repository`, `Request`, `Response`
- 메서드: 의도가 드러나는 동사구. `process()`, `handle()`, `doLogic()` 같은
  무의미한 이름 금지
- boolean: `is`/`has`/`can` 접두사
- 상수: UPPER_SNAKE_CASE. **매직 넘버/문자열 금지** — 의미 있는 상수로 추출
  (예: `MAX_BATCH_SIZE = 100`)
- 약어는 단어 취급: `apiKey`(O) `APIKey`(X), `usageId`(O) `usageID`(X)

## 2. Java 21 활용 규칙

- DTO, 값 객체는 record로 작성한다
- `var`는 우변에서 타입이 명백한 경우만 허용 (`var list = new ArrayList<UsageEvent>()`).
  메서드 반환값을 받을 때는 명시적 타입 사용
- switch는 화살표(arrow) 문법 + 가능하면 패턴 매칭 사용, fall-through 금지
- Stream은 가독성이 좋아지는 경우만. 3단계 이상 중첩되면 메서드 추출 또는 for문

## 3. Lombok 정책 (제한적 허용)

| 어노테이션 | 정책 |
|---|---|
| @Getter | 엔티티에 허용 |
| @NoArgsConstructor(access = PROTECTED) | JPA 엔티티에 허용 |
| @RequiredArgsConstructor | 허용 (생성자 주입용) |
| @Builder | 엔티티 생성 시 허용 (빌더 또는 정적 팩토리 중 클래스당 하나로 통일) |
| @Setter, @Data, @ToString, @EqualsAndHashCode | **금지** |

- @Setter 금지 이유: 상태 변경은 의도가 드러나는 도메인 메서드로 (01-architecture.md §4)
- DTO는 record를 쓰므로 Lombok이 필요 없다

## 4. 의존성 주입 / 트랜잭션

- **생성자 주입만 사용한다.** 필드 주입(@Autowired 필드) 금지
- @Transactional은 service 레이어에만 선언한다 (controller/repository 금지)
- 조회 메서드는 `@Transactional(readOnly = true)` 명시
- 클래스 레벨 readOnly=true + 쓰기 메서드에 개별 @Transactional 패턴 권장

## 5. null / Optional

- 메서드 파라미터, 필드에 Optional 사용 금지. **반환 타입에만** 사용
- 컬렉션 반환은 null 대신 빈 컬렉션 (`List.of()`)
- "없을 수 있음"이 정상 흐름이면 Optional 반환, 비정상이면 예외
  (`findById` → Optional, `getById` → 없으면 예외)

## 6. 예외 처리

- 비즈니스 예외는 `BusinessException(ErrorCode)` 단일 계층으로 통일,
  ErrorCode enum에 코드/메시지/HTTP 상태를 함께 정의한다 (05-api-design.md §4)
- 예외를 잡아서 무시(`catch + 로그만`)하는 코드 금지. 처리 못 하면 전파한다
- 예외를 흐름 제어에 사용하지 않는다 (존재 검사는 exists 쿼리로)
- catch 후 재던질 때 원인 예외를 반드시 포함한다 (`throw new X(e)`)

## 7. 로깅

- SLF4J(@Slf4j)만 사용. System.out 금지
- 레벨: ERROR(즉시 조치 필요) / WARN(이상 징후) / INFO(상태 변화, 배치 시작·종료) /
  DEBUG(개발용 상세)
- 플레이스홀더 사용: `log.info("rollup 완료: date={}, rows={}", date, count)`.
  문자열 연결 금지
- **API key, 토큰 값을 로그에 출력 금지** (마스킹: `sk-ab****`)
- 수집 API 경로에는 건별 INFO 로그 금지 (대량 트래픽 — 배치 단위 요약만)

## 8. 주석

- "무엇을"은 코드가 말한다. 주석은 **"왜"**만 쓴다
  (예: `// 중복 키는 정상 재시도로 간주 — 05-api-design.md §5`)
- 주석 처리된 죽은 코드 커밋 금지 (git이 이력을 가진다)
- public API(컨트롤러 DTO, client 모듈 공개 클래스)에는 Javadoc 작성

## 9. 크기 가이드 (soft limit — 초과 시 분리를 먼저 검토)

- 메서드 30줄, 클래스 300줄, 파라미터 4개
- 초과가 정당하다고 판단되면 그대로 두되 PR "리뷰 포인트"에 사유를 적는다
