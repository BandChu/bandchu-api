# BandChu 모니터링 & 로깅 가이드

## 아키텍처

```
┌────────────────────────────────────────────────────────────────────────────┐
│                            Docker Compose                                 │
│                                                                           │
│  ┌─────────┐         ┌───────────┐                                       │
│  │  Nginx  │ ──────→ │ Spring    │                                       │
│  │ :80/443 │         │ Boot API  │                                       │
│  └─────────┘         │  :8080    │                                       │
│                      └─────┬─────┘                                       │
│                            │                                              │
│               ┌────────────┼────────────┐                                │
│               │            │            │                                 │
│               ▼            ▼            ▼                                │
│        JSON 로그파일   /actuator/    PostgreSQL                           │
│        /app/logs/     prometheus      :5432                              │
│               │            │                                              │
│    ┌──────────┘            └──────────┐                                   │
│    ▼                                  ▼                                   │
│  ┌──────────┐                  ┌────────────┐                            │
│  │ Logstash │                  │ Prometheus │                            │
│  │ (파싱)    │                  │   :9090    │                            │
│  └────┬─────┘                  └──────┬─────┘                            │
│       ▼                               │                                   │
│  ┌───────────────┐                    │                                   │
│  │ Elasticsearch │                    │                                   │
│  │    :9200      │                    │                                   │
│  └───────┬───────┘                    │                                   │
│          │                            │                                   │
│          ▼                            ▼                                   │
│  ┌───────────┐               ┌────────────┐                              │
│  │  Kibana   │               │  Grafana   │                              │
│  │  :5601    │               │   :3000    │                              │
│  │ (로그 검색) │               │ (메트릭 대시보드)│                          │
│  └───────────┘               └────────────┘                              │
└────────────────────────────────────────────────────────────────────────────┘
```

### 데이터 흐름 요약

| 파이프라인 | 역할 | 흐름 |
|-----------|------|------|
| **로그** | 에러 추적, 요청 이력 조회 | API → JSON 로그파일 → Logstash → Elasticsearch → **Kibana** |
| **메트릭** | 성능 지표, 시스템 상태 | API → Actuator → Prometheus → **Grafana** |

---

## 1. 실행 방법

### 전체 스택 실행

```bash
docker-compose up -d
```

### 모니터링만 실행

```bash
docker-compose up -d elasticsearch logstash kibana prometheus grafana
```

### 상태 확인

```bash
# 전체 컨테이너 상태
docker-compose ps

# Elasticsearch 정상 여부
curl http://localhost:9200/_cluster/health?pretty

# Prometheus 타겟 확인
curl http://localhost:9090/api/v1/targets

# API Actuator 확인
curl http://localhost:8080/actuator/health
```

---

## 2. 접속 정보

| 서비스 | URL | 계정 |
|--------|-----|------|
| **Swagger UI** | http://localhost:8080/swagger-ui.html | `bandchu-admin` / `bandchu-swagger-2024!` |
| **Kibana** | http://localhost:5601 | 인증 없음 |
| **Grafana** | http://localhost:3000 | `admin` / `bandchu-grafana-2024!` |
| **Prometheus** | http://localhost:9090 | 인증 없음 |
| **Elasticsearch** | http://localhost:9200 | 인증 없음 |

> 프로덕션에서는 Kibana, Prometheus, Elasticsearch 포트를 외부에 노출하지 마세요.

---

## 3. Kibana 사용법 (로그 조회)

### 최초 설정

1. http://localhost:5601 접속
2. 좌측 메뉴 → **Management** → **Stack Management**
3. **Index Patterns** → **Create index pattern**
4. 패턴 입력: `bandchu-api-*`
5. Time field: `@timestamp` 선택 → Create

### 로그 검색

1. 좌측 메뉴 → **Discover**
2. Index pattern `bandchu-api-*` 선택
3. 시간 범위 설정 (우측 상단)

### 자주 쓰는 검색 쿼리

```
# 특정 에러 코드 검색
level: "ERROR"

# 특정 API 요청만 보기
requestUri: "/api/members/login"

# 특정 traceId로 요청 추적
traceId: "a1b2c3d4"

# 500 에러만 필터
level: "ERROR" AND message: "Uncaught"

# 특정 시간대 느린 요청 찾기
message: "POST" AND message: "/api/posts"
```

### 유용한 시각화

- **Error Rate Over Time**: Y축 Count, X축 @timestamp, 필터 `level: ERROR`
- **Top Endpoints**: Terms aggregation on `requestUri`
- **Response Time Distribution**: 로그 메시지에서 duration 파싱

---

## 4. Grafana 사용법 (메트릭 대시보드)

### 최초 접속

1. http://localhost:3000 접속
2. `admin` / `bandchu-grafana-2024!` 로그인
3. 비밀번호 변경 (권장)
4. 좌측 **Dashboards** → **BandChu** 폴더 → **BandChu API Dashboard**

### 기본 대시보드 패널

| 패널 | 내용 | 확인 포인트 |
|------|------|------------|
| **HTTP Request Rate** | 초당 요청 수 | 트래픽 급증 감지 |
| **HTTP Response Time (p95)** | 95퍼센타일 응답 시간 | 느린 API 식별 |
| **JVM Heap Memory** | 힙 메모리 사용량 | 메모리 누수 감지 |
| **JVM Thread Count** | 활성 스레드 수 | 스레드 고갈 감지 |
| **HTTP Error Rate (5xx)** | 서버 에러 빈도 | 장애 감지 |
| **DB Connection Pool** | HikariCP 커넥션 상태 | DB 병목 감지 |
| **GC Pause Time** | 가비지 컬렉션 시간 | GC 튜닝 필요 여부 |

### 알림 설정 (선택)

1. 대시보드에서 패널 클릭 → **Edit**
2. **Alert** 탭 → **Create alert rule**
3. 조건 설정 예시:
   - 5xx 에러가 1분간 10건 이상 → Slack 알림
   - 응답 시간 p95가 3초 초과 → 이메일 알림
   - JVM 힙 사용률 90% 초과 → 알림

### 커스텀 대시보드 추가

1. **+** → **New Dashboard** → **Add visualization**
2. Datasource: `Prometheus` 선택
3. PromQL 쿼리 입력

자주 쓰는 PromQL:

```promql
# API별 초당 요청 수
rate(http_server_requests_seconds_count{application="bandchu-api"}[1m])

# API별 평균 응답 시간
rate(http_server_requests_seconds_sum{application="bandchu-api"}[5m])
/ rate(http_server_requests_seconds_count{application="bandchu-api"}[5m])

# JVM 메모리 사용률
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100

# 활성 DB 커넥션 수
hikaricp_connections_active{application="bandchu-api"}

# 분당 에러 수
increase(http_server_requests_seconds_count{status=~"5.."}[1m])
```

---

## 5. Prometheus 사용법 (메트릭 직접 조회)

http://localhost:9090 접속 후 쿼리 입력창에서 직접 조회 가능.

### 타겟 확인

**Status** → **Targets** 에서 `bandchu-api` 타겟이 `UP` 상태인지 확인.

### 주요 메트릭 목록

| 메트릭 | 설명 |
|--------|------|
| `http_server_requests_seconds_*` | HTTP 요청 횟수, 응답 시간 |
| `jvm_memory_used_bytes` | JVM 메모리 사용량 |
| `jvm_threads_live_threads` | 활성 스레드 수 |
| `jvm_gc_pause_seconds_*` | GC 일시정지 시간 |
| `hikaricp_connections_*` | DB 커넥션 풀 상태 |
| `process_cpu_usage` | CPU 사용률 |
| `system_load_average_1m` | 시스템 로드 |

---

## 6. 로그 구조

모든 로그는 JSON 형식으로 저장됩니다.

```json
{
  "@timestamp": "2024-12-01T10:30:00.123Z",
  "level": "INFO",
  "logger_name": "c.b.a.g.s.HttpRequestLoggingFilter",
  "message": "GET /api/posts 200 45ms",
  "thread_name": "http-nio-8080-exec-1",
  "traceId": "a1b2c3d4",
  "requestUri": "/api/posts",
  "requestMethod": "GET",
  "clientIp": "123.456.78.90"
}
```

### MDC 필드

| 필드 | 설명 |
|------|------|
| `traceId` | 요청별 고유 ID (8자리) |
| `requestUri` | 요청 경로 |
| `requestMethod` | HTTP 메서드 |
| `clientIp` | 클라이언트 IP |

---

## 7. 운영 팁

### 디스크 관리

```bash
# Elasticsearch 인덱스 용량 확인
curl http://localhost:9200/_cat/indices?v

# 30일 이상 된 인덱스 삭제
curl -X DELETE http://localhost:9200/bandchu-api-2024.10.*

# Docker 볼륨 확인
docker system df -v
```

### 트러블슈팅

```bash
# Logstash 로그 확인 (파싱 에러 등)
docker logs logstash --tail 50

# Elasticsearch 클러스터 상태
curl http://localhost:9200/_cluster/health?pretty

# Prometheus 스크래핑 에러 확인
curl http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | {job: .labels.job, health: .health, lastError: .lastError}'

# API 로그파일 직접 확인
docker exec bandchu-api tail -20 /app/logs/bandchu-api.json
```

### 환경변수 커스터마이징

`.env` 파일에 추가:

```properties
# Swagger 인증
SWAGGER_USERNAME=my-username
SWAGGER_PASSWORD=my-strong-password

# Grafana 인증
GRAFANA_USER=admin
GRAFANA_PASSWORD=my-grafana-password
```

---

## 8. 파일 구조

```
monitoring/
├── logstash/
│   └── pipeline/
│       └── logstash.conf          # 로그 파싱 → Elasticsearch 전송 규칙
├── prometheus/
│   └── prometheus.yml             # 메트릭 수집 대상 설정
└── grafana/
    └── provisioning/
        ├── datasources/
        │   └── datasources.yml    # Prometheus + Elasticsearch 연결
        └── dashboards/
            ├── dashboards.yml     # 대시보드 자동 로드 설정
            └── json/
                └── bandchu-api.json  # 기본 대시보드
```
