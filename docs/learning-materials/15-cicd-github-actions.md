# GitHub Actions CI/CD (6-11단계)

> PLAN.md §6-11 대응 — push → 자동 빌드 → ECR → EC2 배포 파이프라인

---

## 이론

### CI/CD 개념

```
CI (Continuous Integration)  ← 코드 병합 시 자동 빌드·테스트
CD (Continuous Delivery)     ← 빌드 성공 시 자동 배포
```

GitHub Actions는 `.github/workflows/*.yml` 파일로 파이프라인을 정의합니다.
이벤트(push, PR, schedule)가 발생하면 GitHub 인프라에서 자동으로 실행됩니다.

### 워크플로우 기본 구조

```yaml
name: Deploy

on:
  push:
    branches: [main]         # main 브랜치에 push 시
  workflow_dispatch:          # 수동 실행 버튼

jobs:
  build:
    runs-on: ubuntu-latest   # GitHub 제공 러너

    steps:
      - uses: actions/checkout@v4        # 코드 체크아웃
      - uses: actions/setup-java@v4
        with:
          java-version: '21'

      - name: Build
        run: ./gradlew build             # 셸 명령

      - name: Configure AWS
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ap-southeast-1
```

### Secrets

민감한 값은 **GitHub Settings → Secrets**에 저장합니다.
`${{ secrets.SECRET_NAME }}` 문법으로 워크플로우에서 참조합니다.
로그에 출력되지 않고 마스킹됩니다.

---

## QuSign 배포 파이프라인

### 경로 기반 분기

```yaml
# dorny/paths-filter 사용
- uses: dorny/paths-filter@v3
  id: changes
  with:
    filters: |
      backend:
        - 'backend/**'
        - 'docker-compose.prod.yml'
      frontend:
        - 'frontend/**'

deploy-backend:
  needs: [detect-changes]
  if: needs.detect-changes.outputs.backend == 'true' || github.event_name == 'workflow_dispatch'

deploy-frontend:
  needs: [detect-changes]
  if: needs.detect-changes.outputs.frontend == 'true' || github.event_name == 'workflow_dispatch'
```

`backend/**` 변경 시에만 백엔드 잡 실행 → 빌드 시간 절약

### 백엔드 배포 흐름

```yaml
deploy-backend:
  steps:
    # 1. AWS 자격증명 구성 + ECR 로그인
    - uses: aws-actions/configure-aws-credentials@v4
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ap-southeast-1
    - uses: aws-actions/amazon-ecr-login@v2

    # 2. Buildx로 이미지 빌드 + 푸시 (레이어 캐시를 GitHub Actions 캐시에 저장)
    - uses: docker/setup-buildx-action@v3
    - uses: docker/build-push-action@v5
      with:
        context: backend
        push: true
        tags: ${{ secrets.ECR_REGISTRY }}/qusign_backend:latest
        cache-from: type=gha
        cache-to: type=gha,mode=max

    # 3. docker-compose.prod.yml을 SCP로 EC2에 미리 전송
    - run: scp -i ~/.ssh/ec2.pem docker-compose.prod.yml ec2-user@$EC2_HOST:/home/ec2-user/

    # 4. EC2 SSH → SSM에서 7개 파라미터 읽기 → .env 생성 → docker-compose up
    - uses: appleboy/ssh-action@v1
      with:
        host: ${{ secrets.EC2_HOST }}
        key: ${{ secrets.EC2_SSH_KEY }}
        script: |
          # db-password, jwt-secret, s3-bucket, cors-origins, server-url, admin-email, admin-password
          DB_PASSWORD=$(aws ssm get-parameter --name /qusign/prod/db-password --with-decryption --query Parameter.Value --output text)
          JWT_SECRET=$(aws ssm get-parameter --name /qusign/prod/jwt-secret --with-decryption --query Parameter.Value --output text)
          # ... (S3_BUCKET, CORS_ORIGINS, SERVER_URL, ADMIN_EMAIL, ADMIN_PASSWORD도 동일 패턴)

          cat > /home/ec2-user/.env << ENVEOF
          ECR_REGISTRY=$ECR_REGISTRY
          DB_PASSWORD=$DB_PASSWORD
          JWT_SECRET=$JWT_SECRET
          ENVEOF

          aws ecr get-login-password --region ap-southeast-1 | \
            docker login --username AWS --password-stdin "$ECR_REGISTRY"
          docker-compose -f /home/ec2-user/docker-compose.prod.yml pull backend
          docker-compose -f /home/ec2-user/docker-compose.prod.yml up -d
```
`docker build`/`docker push`를 직접 호출하지 않고 `docker/build-push-action@v5`(Buildx 기반)를 씁니다 — GitHub Actions 캐시(`type=gha`)를 재사용해 매번 전체 레이어를 새로 빌드하지 않습니다. `appleboy/ssh-action`은 `@master`가 아니라 고정 버전(`@v1`)을 사용합니다 — 태그가 아닌 브랜치 참조는 언제든 내용이 바뀔 수 있어 CI 재현성에 좋지 않습니다.

### 프론트엔드 배포 흐름

```yaml
deploy-frontend:
  steps:
    - uses: actions/setup-node@v4
      with:
        node-version: '22'
        cache: 'npm'
        cache-dependency-path: frontend/package-lock.json

    - working-directory: frontend
      run: npm ci && npm run build-only   # dist/ 생성

    # SSH로 대상 디렉터리 준비 후, scp -r 로 dist/ 전체 전송
    - run: |
        ssh -i ~/.ssh/ec2.pem ec2-user@$EC2_HOST \
          "sudo mkdir -p /var/www/qusign/dist && sudo chown -R ec2-user:ec2-user /var/www/qusign"
        scp -i ~/.ssh/ec2.pem -r frontend/dist/* ec2-user@$EC2_HOST:/var/www/qusign/dist/
```
프론트엔드는 ECR 불필요 — Vue 빌드 결과물(정적 파일)을 직접 EC2로 전송합니다. `appleboy/scp-action` 같은 전용 액션 대신, SSH로 디렉터리를 준비한 뒤 원시 `scp -r`로 전송하는 2단계 방식입니다. 빌드 명령은 `npm run build`가 아니라 `npm run build-only`입니다(타입체크를 별도 단계로 분리한 스크립트 이름).

---

## ECR (Elastic Container Registry)

Docker Hub의 AWS 버전입니다. 프라이빗 컨테이너 이미지 저장소입니다.

```bash
# ECR 로그인
aws ecr get-login-password --region ap-southeast-1 | \
  docker login --username AWS --password-stdin \
  285868221698.dkr.ecr.ap-southeast-1.amazonaws.com

# 이미지 빌드 + 태깅
docker build -t 285868221698.dkr.ecr.ap-southeast-1.amazonaws.com/qusign_backend:latest .

# 푸시
docker push 285868221698.dkr.ecr.ap-southeast-1.amazonaws.com/qusign_backend:latest
```

수명 주기 정책 — 최신 3개 이미지만 유지 (스토리지 비용 절감):
```json
{"rules": [{"rulePriority": 1,
  "selection": {"tagStatus": "any", "countType": "imageCountMoreThan", "countNumber": 3},
  "action": {"type": "expire"}}]}
```

---

## Multi-Stage Dockerfile

실제 `backend/Dockerfile`은 스테이지가 3개입니다 — liboqs C 라이브러리 빌드, Spring Boot 앱 빌드, 최종 런타임 이미지.

```dockerfile
# Stage 1: liboqs C 네이티브 라이브러리 빌드
FROM ubuntu:22.04 AS liboqs-builder
RUN apt-get update && apt-get install -y --no-install-recommends \
    cmake ninja-build gcc g++ libssl-dev git ca-certificates
WORKDIR /build
RUN git clone --depth 1 --branch main https://github.com/open-quantum-safe/liboqs.git
WORKDIR /build/liboqs
RUN cmake -GNinja -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX=/opt/liboqs \
    -DBUILD_SHARED_LIBS=ON -DOQS_BUILD_ONLY_LIB=ON . && ninja && ninja install

# Stage 2: Spring Boot 앱 빌드
FROM eclipse-temurin:21-jdk-jammy AS app-builder
WORKDIR /workspace
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts build.gradle.kts ./
COPY gradle/ gradle/
RUN ./gradlew dependencies --no-daemon -q   # 의존성 레이어를 소스 변경 전에 캐시
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# Stage 3: 운영 이미지 (JDK가 아닌 JRE — 실행에 컴파일러는 불필요)
FROM eclipse-temurin:21-jre-jammy
COPY --from=liboqs-builder /opt/liboqs/lib /opt/liboqs/lib
COPY --from=app-builder /workspace/build/libs/*.jar app.jar
ENV LD_LIBRARY_PATH=/opt/liboqs/lib
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.library.path=/opt/liboqs/lib", "-jar", "/app.jar"]
```

빌드 도구(cmake/gcc, Gradle 전체, liboqs 소스 저장소)는 최종 이미지에 포함되지 않아 이미지 크기가 작습니다.
liboqs를 소스에서 직접 컴파일하는 이유는 CPU 아키텍처(x86_64/ARM)에 맞는 네이티브 바이너리를 빌드 시점에 생성하기 위해서입니다 — 미리 빌드된 `.so`를 저장소에 커밋해두지 않습니다.

> 참고: 실제 서명·검증 로직은 liboqs가 아니라 BouncyCastle(`"ML-DSA"` provider)을 사용합니다 — [[06-pqc-mldsa]]. liboqs 빌드 스테이지는 향후 liboqs-java(JNI) 전환을 대비한 것으로, 현재 런타임에서 이 라이브러리를 직접 호출하지는 않습니다.

---

## 확인 질문 & 답변

**Q1. `secrets.EC2_SSH_KEY`에 PEM 파일 내용 전체를 저장하는 이유는?**

> GitHub Actions 러너는 EC2에 비밀 파일을 저장하지 않습니다. `appleboy/ssh-action`이 Secrets에서 키 내용을 읽어 임시 파일로 만들고 SSH 연결 후 삭제합니다. 파일이 아닌 Secrets에 저장하면 GitHub 인프라에서 암호화되어 관리됩니다.

**Q2. `workflow_dispatch`를 추가하는 이유는?**

> 경로 필터 적용 시 특정 경로에 변경이 없으면 해당 잡이 실행되지 않습니다. 강제로 전체 배포가 필요할 때(설정 변경, 긴급 재배포) `workflow_dispatch`로 수동 트리거할 수 있습니다.

**Q3. Multi-Stage Dockerfile에서 빌드 스테이지와 런타임 스테이지를 분리하는 이유는?**

> 최종 이미지에 Gradle, 소스코드, 테스트 코드가 포함되지 않습니다. 런타임에 필요한 JAR 파일만 포함되어 이미지 크기가 수백 MB 줄어듭니다. 이미지가 작을수록 ECR 저장 비용, 네트워크 전송 시간, 취약점 공격 면이 줄어듭니다.

**Q4. 배포 후 `.env` 파일을 `rm -f`로 삭제하는 이유는?**

> `.env` 파일에는 DB 비밀번호, JWT 시크릿 등이 평문으로 들어 있습니다. 파일로 남아 있으면 서버 접근 시 즉시 노출됩니다. `docker-compose up` 직후 삭제하면 비밀값이 파일 시스템에 남지 않습니다. 이상적으로는 Docker Secrets나 환경변수 직접 주입이 더 안전합니다.
