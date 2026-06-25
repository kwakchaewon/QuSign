# Loki + Grafana — 운영 모니터링 (8-1단계)

> PLAN.md §8-1 대응 — 로그 수집·시각화·이상 접근 탐지

---

## 이론

### 관찰 가능성(Observability) 3대 요소

| 요소 | 도구 | 설명 |
|---|---|---|
| **Logs** | **Loki** | 구조화 로그 수집·질의 |
| Metrics | Prometheus | 수치 시계열 데이터 |
| Traces | Jaeger/Zipkin | 요청 분산 추적 |

QuSign은 8단계에서 **Loki + Grafana** 조합으로 로그 관찰 가능성을 구축합니다.

### Loki 구조

```
Spring Boot (Logback JSON)
    ↓ 로그 파일 또는 stdout
Promtail (로그 수집 에이전트)
    ↓ push
Loki (로그 저장·인덱싱)
    ↓ query
Grafana (시각화 대시보드)
```

Prometheus와 달리 Loki는 **레이블(label)만 인덱싱**합니다.
로그 내용 전체를 인덱싱하지 않아 메모리 사용량이 적습니다.

### LogQL — Loki 쿼리 언어

```logql
# 레이블 필터 — service=backend의 ERROR 로그
{service="backend"} |= "ERROR"

# JSON 파싱 → 필드 추출
{service="backend"} | json | level="ERROR" | line_format "{{.message}}"

# 분당 에러 횟수 (메트릭 쿼리)
rate({service="backend"} |= "ERROR" [1m])
```

### Promtail 설정

```yaml
# promtail-config.yml
scrape_configs:
  - job_name: qusign-backend
    static_configs:
      - targets: [localhost]
        labels:
          service: backend
          __path__: /var/log/qusign/*.log   # 로그 파일 경로
```

---

## Spring Boot Logback JSON 연동

구조화 JSON 로그를 출력하면 Loki에서 필드 파싱이 가능합니다.

```xml
<!-- logback-spring.xml -->
<appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
  <encoder class="net.logstash.logback.encoder.LogstashEncoder">
    <customFields>{"service":"qusign-backend","env":"${SPRING_PROFILES_ACTIVE}"}</customFields>
  </encoder>
</appender>
```

출력 예시:
```json
{
  "timestamp": "2025-01-15T09:30:00.123Z",
  "level": "INFO",
  "service": "qusign-backend",
  "env": "prod",
  "message": "서명 완료: document=42, signer=user@example.com",
  "traceId": "abc123"
}
```

---

## QuSign 모니터링 대시보드 계획

### 패널 1 — 일별 서명 요청 건수
```logql
sum by (day) (
  count_over_time({service="backend"} |= "SIGN_REQUEST_CREATED" [1d])
)
```

### 패널 2 — 검증 성공/실패 비율
```logql
# 성공률
rate({service="backend"} |= "검증 성공" [5m])
  /
(rate({service="backend"} |= "검증 성공" [5m]) + rate({service="backend"} |= "검증 실패" [5m]))
```

### 패널 3 — API 응답 시간 분포
Spring Boot Actuator + Micrometer → Prometheus 메트릭으로 수집:
```logql
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))
```

### 패널 4 — 이상 접근 탐지

```logql
# 동일 IP에서 1분 내 10회 이상 실패 로그
sum by (ip) (
  count_over_time({service="backend"} |= "401" [1m])
) > 10
```

---

## Docker Compose로 모니터링 스택 추가

```yaml
# docker-compose.monitoring.yml
services:
  loki:
    image: grafana/loki:2.9.0
    ports:
      - "3100:3100"
    command: -config.file=/etc/loki/local-config.yaml
    volumes:
      - loki_data:/loki

  promtail:
    image: grafana/promtail:2.9.0
    volumes:
      - /var/log:/var/log    # 호스트 로그 디렉토리 마운트
      - ./promtail-config.yml:/etc/promtail/config.yml
    command: -config.file=/etc/promtail/config.yml

  grafana:
    image: grafana/grafana:10.0.0
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana_data:/var/lib/grafana
```

---

## 이상 접근 알림 설정

```yaml
# Grafana Alert Rule
- alert: HighErrorRate
  expr: rate({service="backend"} |= "ERROR" [5m]) > 0.1
  for: 5m
  annotations:
    summary: "에러율 급증 — 5분 내 분당 0.1건 초과"

- alert: BruteForceDetected
  expr: sum by (ip) (count_over_time({service="backend"} |= "401" [1m])) > 10
  annotations:
    summary: "브루트포스 의심 — 동일 IP 1분 내 401 10회 초과"
```

---

## 확인 질문 & 답변

**Q1. Loki가 Elasticsearch와 다른 점은?**

> Elasticsearch는 로그 내용 전체를 인덱싱하여 강력한 검색이 가능하지만 메모리와 비용이 많이 듭니다. Loki는 레이블(서비스명·환경·호스트)만 인덱싱하고 내용은 압축 저장합니다. 검색은 레이블로 범위를 좁힌 뒤 내용을 grep 방식으로 스캔합니다. 소규모 프로젝트에서는 Loki가 운영 비용이 훨씬 저렴합니다.

**Q2. Promtail이 파일에서 로그를 읽는 방식은?**

> `tail -f`처럼 파일 끝을 계속 읽습니다. 마지막으로 읽은 위치(offset)를 로컬 파일에 저장하여 Promtail 재시작 시 중복 수집을 방지합니다. stdout 로그는 Docker log driver로 Promtail에 전달할 수 있습니다.

**Q3. 구조화 JSON 로그와 일반 텍스트 로그의 차이는?**

> 텍스트 로그: `2025-01-15 ERROR NullPointerException at line 42` — 파싱이 어렵습니다. JSON 로그: `{"level":"ERROR","message":"NPE","class":"SignService","line":42}` — Loki에서 `| json` 파이프로 즉시 필드 추출이 가능하여 `level="ERROR"` 같은 구조화 쿼리를 쓸 수 있습니다.

**Q4. 이상 접근 탐지 알림이 false positive(오탐)를 낼 수 있는 상황은?**

> 부하 테스트 실행 시 정상 트래픽에서도 401이 대량 발생할 수 있습니다. 알림 임계값(`> 10`)을 낮게 설정하면 일반 로그인 실패 급증 시에도 알림이 울립니다. 실제 서비스에서는 IP 단위 화이트리스트 적용, 알림 조건에 시간대 제한(야간 기준 강화), 또는 임계값 조정으로 오탐을 줄입니다.
