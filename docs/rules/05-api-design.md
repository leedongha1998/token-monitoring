# API 설계 규칙

모든 REST API는 이 문서를 따른다. 여기 정의되지 않은 패턴이 필요하면
임의로 만들지 말고 사용자에게 제안한다.

## 1. URL 규칙

- 모든 엔드포인트는 `/v1` prefix로 시작한다 (버저닝)
- 리소스는 복수형 명사, kebab-case: `/v1/projects`, `/v1/usage-events`
- 동사 금지. 행위는 HTTP 메서드로 표현한다
  - 예외: 리소스로 표현 불가능한 행위만 하위 경로 동사 허용 (`/v1/api-keys/{id}/rotate`)
- 계층은 2단계까지만: `/v1/projects/{projectId}/api-keys` (그 이상 중첩 금지)

## 2. 엔드포인트 명세 (핵심 도메인)

```
# 수집 (인증: X-API-Key)
POST   /v1/events                  # 단건 수집 → 202
POST   /v1/events/batch            # 배치 수집 (최대 100건) → 202

# 관리 (인증: 추후 관리자 인증, 초기엔 내부망 가정)
POST   /v1/projects                # 프로젝트 등록 → 201 + Location 헤더
GET    /v1/projects                # 목록 (페이징)
GET    /v1/projects/{id}
POST   /v1/projects/{id}/api-keys  # 키 발급 (평문 키는 이 응답에서 단 1회만 노출)
DELETE /v1/api-keys/{id}           # 키 비활성화 → 204

# 조회 (대시보드용)
GET    /v1/stats/daily?projectId=&from=&to=&model=
GET    /v1/stats/summary?from=&to=
```

## 3. 요청/응답 규칙

- 요청/응답 본문은 camelCase JSON
- DTO는 Java record로 작성, 요청 DTO에 Bean Validation 적용
  (`@NotNull`, `@Size`, `@Positive` 등). 컨트롤러에서 수동 검증 코드 작성 금지
- 날짜/시각은 ISO-8601 + 오프셋 포함: `2026-06-10T14:30:00+09:00`.
  서버 저장은 UTC(timestamptz), 변환은 경계(컨트롤러/DTO)에서만
- 페이징: `?page=0&size=20&sort=createdAt,desc` (Spring Pageable 규약),
  응답은 `content`, `totalElements`, `totalPages`, `number`를 포함한 공통 페이지 포맷

## 4. 공통 에러 응답

모든 에러는 `@RestControllerAdvice` 전역 핸들러에서 다음 포맷으로 반환한다.
컨트롤러/서비스에서 개별 try-catch로 에러 응답을 만들지 않는다.

```json
{
  "code": "USAGE-001",
  "message": "idempotencyKey는 필수입니다",
  "timestamp": "2026-06-10T14:30:00+09:00",
  "errors": [
    { "field": "idempotencyKey", "reason": "must not be blank" }
  ]
}
```

- `code`는 `{도메인}-{번호}` 형식으로 ErrorCode enum에서 중앙 관리한다
- 스택트레이스, 내부 예외 메시지를 응답에 노출하지 않는다

### HTTP 상태코드 매핑

| 상황 | 상태 |
|---|---|
| 수집 접수 성공 (비동기 처리 전제) | 202 Accepted |
| 생성 성공 | 201 + Location |
| 삭제/비활성화 성공 | 204 |
| 요청 형식 오류 (validation) | 400 |
| API key 없음/무효/만료 | 401 |
| 권한 없는 프로젝트 접근 | 403 |
| 리소스 없음 | 404 |
| idempotency key 충돌 (이미 처리됨) | 200 (멱등 — 아래 §5) |
| 배치 건수 초과 (100건) | 413 |
| 서버 오류 | 500 |

## 5. Ingestion API 동작 명세 (멱등성)

- 동일 `idempotencyKey` 재수신 시: **에러가 아니라 200으로 응답**하고
  저장은 건너뛴다 (클라이언트 재시도를 정상 동작으로 취급)
- 배치 요청 내 일부 건이 중복 키인 경우: **부분 성공**으로 처리하고
  응답에 건별 결과를 반환한다:
  ```json
  {
    "accepted": 97,
    "duplicated": 3,
    "results": [ { "idempotencyKey": "...", "status": "DUPLICATED" } ]
  }
  ```
- 수집 API는 본 서비스 장애가 클라이언트에 전파되지 않도록
  요청 검증 외의 무거운 처리(가격 계산 등)를 동기 경로에 두지 않는다

## 6. 인증

- 수집 API: `X-API-Key` 헤더. 키는 DB에 해시(SHA-256)로만 저장하고
  평문은 발급 응답에서 1회만 노출한다
- 인증 실패 응답도 §4의 공통 에러 포맷을 따른다 (`AUTH-001` 등)
- API key를 로그에 남기지 않는다 (마스킹 필수)
