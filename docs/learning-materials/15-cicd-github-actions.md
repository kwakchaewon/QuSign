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
    # 1. JAR 빌드
    - run: ./gradlew bootJar

    # 2. ECR 로그인
    - uses: aws-actions/amazon-ecr-login@v2

    # 3. Docker 이미지 빌드 + ECR 푸시
    - run: |
        docker build -t $ECR_REGISTRY/qusign_backend:latest ./backend
        docker push $ECR_REGISTRY/qusign_backend:latest

    # 4. EC2 SSH → SSM 값 읽기 → docker-compose up
    - uses: appleboy/ssh-action@master
      with:
        host: ${{ secrets.EC2_HOST }}
        key: ${{ secrets.EC2_SSH_KEY }}
        script: |
          # SSM에서 비밀값 읽기
          DB_PASS=$(aws ssm get-parameter --name /qusign/prod/db-password \
            --with-decryption --query Parameter.Value --output text)
          JWT_SECRET=$(aws ssm get-parameter --name /qusign/prod/jwt-secret \
            --with-decryption --query Parameter.Value --output text)

          # .env 파일 생성 (메모리에만 — 파일 삭제)
          cat > /home/ec2-user/.env << EOF
          DB_PASSWORD=$DB_PASS
          JWT_SECRET=$JWT_SECRET
          EOF

          # ECR 로그인 → 최신 이미지 풀 → 재시작
          aws ecr get-login-password --region ap-southeast-1 | \
            docker login --username AWS --password-stdin $ECR_REGISTRY
          docker-compose -f docker-compose.prod.yml pull
          docker-compose -f docker-compose.prod.yml up -d
          rm -f /home/ec2-user/.env
```

### 프론트엔드 배포 흐름

```yaml
deploy-frontend:
  steps:
    - run: |
        cd frontend
        npm ci
        npm run build   # dist/ 생성

    # SCP로 EC2에 전송
    - uses: appleboy/scp-action@master
      with:
        host: ${{ secrets.EC2_HOST }}
        key: ${{ secrets.EC2_SSH_KEY }}
        source: "frontend/dist/*"
        target: "/var/www/qusign/dist"
        strip_components: 2
```

프론트엔드는 ECR 불필요 — Vue 빌드 결과물(정적 파일)을 직접 EC2로 전송합니다.

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

```dockerfile
# 빌드 스테이지
FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# 런타임 스테이지 (작은 이미지)
FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# liboqs 네이티브 라이브러리 포함
COPY --from=build /app/libs/liboqs.so /usr/local/lib/

ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
```

빌드 도구(Gradle, 소스코드)는 최종 이미지에 포함되지 않아 이미지 크기가 작습니다.

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
