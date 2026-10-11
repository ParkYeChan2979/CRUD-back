# crud-back

Spring Boot 실습 과제 — 아이템(Item) CRUD REST API

- Java 21 / Spring Boot 4.1.1 / Gradle
- 데이터는 메모리(Map)에 저장 (서버 재시작 시 초기화)

## 실행 방법

```bash
./gradlew bootRun
```

서버 주소: `http://localhost:8080`

## 프로젝트 구조

교수님 예제(springboot-tutorial)와 같은 폴더 구조를 따릅니다.

```
src/main/java/kr/ac/jbnu/ycp/crudback
├── CrudBackApplication.java              # 시작점 (main)
├── config
│   ├── WebConfig.java                    # 인터셉터(미들웨어) 등록
│   ├── LoggingInterceptor.java           # 미들웨어 1: 요청/응답 로그
│   └── ErrorSimulationInterceptor.java   # 미들웨어 2: 5xx 오류 시뮬레이션
└── api
    ├── dto
    │   ├── ItemDto.java                  # 아이템 응답 데이터
    │   └── ApiResponse.java              # 표준 응답 포맷
    ├── request
    │   ├── ItemCreateRequest.java        # 생성 요청
    │   ├── ItemBulkCreateRequest.java    # 일괄 생성 요청
    │   ├── ItemUpdateRequest.java        # 전체 수정 요청
    │   └── ItemPriceUpdateRequest.java   # 가격 수정 요청
    ├── exception
    │   ├── ApiException.java             # 상태코드를 담는 커스텀 예외
    │   └── GlobalExceptionHandler.java   # 예외 → 표준 에러 응답 변환
    └── v1
        └── ItemController.java           # API 8개
```

## A. API 목록 (메소드별 2개, 총 8개)

| 메소드 | URL | 설명 | 성공 | 실패 |
|---|---|---|---|---|
| POST | `/api/v1/items` | 아이템 생성 | 201 | 400, 409 |
| POST | `/api/v1/items/bulk` | 아이템 일괄 생성 | 201 | 400, 409 |
| GET | `/api/v1/items?keyword=` | 전체 조회 (이름 검색) | 200 | - |
| GET | `/api/v1/items/{id}` | 단건 조회 | 200 | 400, 404 |
| PUT | `/api/v1/items/{id}` | 이름·가격 수정 | 200 | 400, 404, 409 |
| PUT | `/api/v1/items/{id}/price` | 가격만 수정 | 200 | 400, 404 |
| DELETE | `/api/v1/items/{id}` | 단건 삭제 | 200 | 404 |
| DELETE | `/api/v1/items` | 전체 삭제 | 200 | - |

## B. 미들웨어 (Spring `HandlerInterceptor`)

`/api/**` 경로에 두 개의 인터셉터를 등록했습니다.

1. **LoggingInterceptor**: 요청 시작 시 메소드·URI, 완료 시 상태코드와 처리 시간을 로그로 출력
   ```
   [REQUEST]  GET /api/v1/items/1
   [RESPONSE] GET /api/v1/items/1 -> status=200 (2ms)
   ```
2. **ErrorSimulationInterceptor**: 요청 헤더 `X-Simulate-Error` 값으로 5xx 오류를 강제로 발생 (테스트용)
   - `X-Simulate-Error: 500` → 500 Internal Server Error
   - `X-Simulate-Error: 503` → 503 Service Unavailable

## C. 사용한 응답 코드

| 범주 | 코드 | 발생 상황 |
|---|---|---|
| 2xx | 200 OK | 조회, 수정, 삭제 성공 |
| 2xx | 201 Created | 생성 성공 |
| 4xx | 400 Bad Request | 입력값 검증 실패, JSON 형식 오류, id 타입 오류 |
| 4xx | 404 Not Found | 없는 아이템 id, 없는 URL |
| 4xx | 405 Method Not Allowed | 지원하지 않는 메소드 (예: PATCH) |
| 4xx | 409 Conflict | 이미 존재하는 이름으로 생성/수정 |
| 5xx | 500 Internal Server Error | 처리되지 않은 서버 예외 |
| 5xx | 503 Service Unavailable | 서비스 점검 중 |

## D. 표준 응답 포맷

모든 응답(에러 포함)은 `ApiResponse`로 감싸서 반환합니다. 값이 없는 필드는 생략됩니다.

성공:
```json
{
  "status": "success",
  "data": { "id": 1, "name": "키보드", "price": 50000 },
  "message": "아이템이 생성되었습니다."
}
```

실패:
```json
{
  "status": "error",
  "message": "id=99 아이템을 찾을 수 없습니다."
}
```

검증 실패 (어떤 필드가 왜 틀렸는지 `data`에 포함):
```json
{
  "status": "error",
  "data": { "name": "이름은 필수입니다.", "price": "가격은 0 이상이어야 합니다." },
  "message": "입력값이 올바르지 않습니다."
}
```

## 테스트 예시 (curl)

```bash
# 201 생성
curl -X POST localhost:8080/api/v1/items -H "Content-Type: application/json" -d '{"name":"키보드","price":50000}'
# 201 일괄 생성
curl -X POST localhost:8080/api/v1/items/bulk -H "Content-Type: application/json" \
  -d '{"items":[{"name":"마우스","price":20000},{"name":"모니터","price":300000}]}'
# 200 전체 조회 / 단건 조회
curl localhost:8080/api/v1/items
curl localhost:8080/api/v1/items/1
# 200 수정
curl -X PUT localhost:8080/api/v1/items/1 -H "Content-Type: application/json" -d '{"name":"기계식 키보드","price":89000}'
curl -X PUT localhost:8080/api/v1/items/2/price -H "Content-Type: application/json" -d '{"price":25000}'
# 200 삭제
curl -X DELETE localhost:8080/api/v1/items/3
curl -X DELETE localhost:8080/api/v1/items

# 400 검증 실패
curl -X POST localhost:8080/api/v1/items -H "Content-Type: application/json" -d '{"name":"","price":-1}'
# 404 없는 아이템
curl localhost:8080/api/v1/items/99
# 409 이름 중복 (키보드를 한 번 더 생성)
curl -X POST localhost:8080/api/v1/items -H "Content-Type: application/json" -d '{"name":"키보드","price":1}'
# 500 / 503
curl localhost:8080/api/v1/items -H "X-Simulate-Error: 500"
curl localhost:8080/api/v1/items -H "X-Simulate-Error: 503"
```
