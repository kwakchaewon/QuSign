# Docker Compose — 로컬 개발 환경 (1-4단계)

> PLAN.md §1-4 대응 — MinIO·MariaDB·Redis를 컨테이너로 띄운다

---

## 이론

### 컨테이너 vs 가상머신

| 항목 | VM | 컨테이너 |
|---|---|---|
| OS 커널 | 각자 보유 | 호스트 공유 |
| 시작 시간 | 분 단위 | 초 단위 |
| 이미지 크기 | GB 단위 | MB 단위 |
| 격리 수준 | 강함 | 프로세스 수준 |

컨테이너는 **프로세스 격리** + **파일 시스템 레이어** 조합입니다.
Docker는 컨테이너를 관리하는 런타임이고, Docker Compose는 **여러 컨테이너를 정의·실행하는 오케스트레이션 도구**입니다.

### docker-compose.yml 핵심 개념

```yaml
version: '3.8'

services:         # 실행할 컨테이너 목록
  app:
    image: openjdk:21          # Docker Hub 이미지
    build: ./backend           # 로컬 Dockerfile 빌드
    ports:
      - "8080:8080"            # 호스트:컨테이너
    environment:               # 환경변수 주입
      - SPRING_PROFILES_ACTIVE=local
    depends_on:                # 시작 순서 힌트 (healthcheck 아님)
      - mariadb
    volumes:
      - ./data:/app/data       # 호스트 디렉토리:컨테이너 경로

  mariadb:
    image: mariadb:10.11
    volumes:
      - mariadb_data:/var/lib/mysql   # named volume — 컨테이너 재시작해도 유지

volumes:
  mariadb_data:   # 선언된 named volume
```

### 네트워크

같은 `docker-compose.yml` 안의 서비스들은 **자동으로 같은 네트워크**에 속합니다.
서비스명이 DNS 호스트명으로 등록되므로 `mariadb:3306`처럼 접근할 수 있습니다.

### Healthcheck vs depends_on

`depends_on`은 컨테이너 **시작 순서**만 보장합니다. 서비스가 실제로 준비될 때까지 기다리려면 `healthcheck`가 필요합니다.

```yaml
mariadb:
  healthcheck:
    test: ["CMD", "healthcheck.sh", "--connect"]
    interval: 10s
    timeout: 5s
    retries: 5

backend:
  depends_on:
    mariadb:
      condition: service_healthy   # healthy 상태일 때만 시작
```

---

## QuSign 로컬 환경 구성

```yaml
# docker-compose.yml (로컬)
services:
  minio:
    image: minio/minio
    command: server /data --console-address ":9001"
    ports:
      - "9000:9000"   # S3 API
      - "9001:9001"   # 웹 콘솔
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
```

백엔드는 `./gradlew bootRun`으로 로컬에서 직접 실행합니다.
MinIO는 S3 호환 API를 제공하므로 코드 변경 없이 프로덕션 S3와 교체할 수 있습니다.

### 프로파일 분리 전략

```
application.yml          ← 공통 설정
application-local.yml    ← 로컬 (MinIO, 콘솔 이메일)
application-prod.yml     ← 프로덕션 (S3, SES, SSM 환경변수)
```

`SPRING_PROFILES_ACTIVE=local` 환경변수로 활성 프로파일을 지정합니다.

---

## 자주 쓰는 명령어

```bash
# 백그라운드로 전체 실행
docker compose up -d

# 로그 스트리밍 (서비스명 생략 시 전체)
docker compose logs -f minio

# 컨테이너 상태 확인
docker compose ps

# 컨테이너 안에서 명령 실행
docker exec -it qusign-mariadb mariadb -uqusign -p

# 전체 정지 + 컨테이너 삭제 (볼륨 유지)
docker compose down

# 볼륨까지 삭제 (DB 초기화 필요 시)
docker compose down -v
```

---

## 확인 질문 & 답변

**Q1. `ports: "9000:9000"` 에서 앞의 9000과 뒤의 9000의 차이는?**

> 앞이 호스트(내 PC) 포트, 뒤가 컨테이너 내부 포트입니다. `"8888:9000"`으로 설정하면 `localhost:8888`로 접속했을 때 컨테이너의 9000 포트로 연결됩니다.

**Q2. named volume(`mariadb_data`)을 사용하는 이유는?**

> `docker compose down` 후 컨테이너가 삭제되어도 데이터가 호스트에 남습니다. 바인드 마운트(`./data:/var/lib/mysql`)도 가능하지만 named volume은 Docker가 경로를 관리해 OS간 경로 이슈가 없습니다.

**Q3. 로컬에서 MinIO를 쓰는 이유는?**

> MinIO는 S3 호환 API를 제공합니다. `StorageService` 인터페이스 + `application-local.yml` endpoint 설정만으로 로컬(MinIO)과 프로덕션(AWS S3) 간 코드 변경 없이 전환이 가능합니다.

**Q4. `depends_on`만 있고 `healthcheck`가 없으면 어떤 문제가 생기나?**

> MariaDB 컨테이너가 시작됐지만 아직 연결을 받을 준비가 안 된 상태에서 Spring Boot가 연결을 시도하면 `Connection refused` 오류로 앱이 죽습니다. `condition: service_healthy` + healthcheck를 쓰거나, Spring Boot `datasource.hikari.connection-timeout`을 늘려 재시도를 유도합니다.
