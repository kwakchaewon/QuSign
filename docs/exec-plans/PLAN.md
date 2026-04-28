# PQC 전자서명 서비스 — 개발 & 학습 체크리스트

> 재직 중 사이드 프로젝트 / 12개월 목표  
> 학습 직후 바로 구현 — 배운 것을 즉시 코드로 굳힌다

---

## 진행 현황

| 단계 | 내용 | 상태 |
|------|------|------|
| 1단계 | 환경 세팅 + PQC 핵심 검증 | ⬜ 진행 전 |
| 2단계 | 백엔드 핵심 구현 | ⬜ 진행 전 |
| 3단계 | 프론트엔드 + 이메일 연동 | ⬜ 진행 전 |
| 4단계 | AWS 배포 + GitHub Actions | ⬜ 진행 전 |
| 5단계 | Terraform + 수익화 | ⬜ 진행 전 |
| 6단계 | Loki + Grafana + 이직 준비 | ⬜ 진행 전 |

---

## 1단계: 환경 세팅 + PQC 핵심 검증
> 기간: 0~6주 / ⚠️ 가장 중요한 단계 — 여기서 막히면 전체가 흔들림

### 1-1. Kotlin 기초 → Spring Boot 프로젝트 세팅 (2주)

**목표:** Kotlin 문법을 익히고 바로 프로젝트 뼈대를 만든다

- [ ] null safety 이해 (`?.` `!!` `?:`)
- [ ] data class 개념 및 사용법
- [ ] extension function 개념 및 사용법
- [ ] when expression 사용법
- [ ] Kotlin 공식 문서 Basics 섹션 완독
- [ ] coroutine 기초 개념 파악 (suspend, launch) — 깊이는 나중에
- [x] **→ 즉시 적용:** Gradle Kotlin DSL 기반 Spring Boot 3.5 프로젝트 생성
- [x] **→ 즉시 적용:** Logback JSON 구조화 로그 설정
- [x] **→ 즉시 적용:** GitHub 레포지토리 생성 + 초기 커밋

**완료 기준:** Kotlin으로 Spring Boot REST API 하나 혼자 짤 수 있으면 됨

---

### 1-2. liboqs-java + ML-DSA → 암호화 코드 구현 (2주)

**목표:** ML-DSA 개념을 이해하고 바로 키 생성·서명·검증 코드를 작성한다

- [ ] 기존 RSA/ECDSA 서명 동작 원리 복습
- [ ] ML-DSA(Dilithium)가 RSA와 다른 점 이해
- [ ] 격자 암호(Lattice Cryptography) 개념 수준 파악 (깊이 X, 방향만)
- [ ] NIST PQC 표준 4종 이름과 용도 파악 (ML-KEM, ML-DSA, SLH-DSA, FALCON)
- [ ] ML-DSA 키쌍 구조 이해 (공개키/개인키/서명값 각각 크기)
- [x] **→ 즉시 적용:** liboqs 공식 Docker 이미지 pull
  ```bash
  docker pull openquantumsafe/liboqs
  ```
- [x] **→ 즉시 적용:** liboqs-java 네이티브 빌드 Dockerfile 작성
- [x] **→ 즉시 적용:** Spring Boot 프로젝트에 ML-DSA 의존성 연동 (Bouncy Castle 1.84, 로컬) / liboqs-java (Docker)
- [x] **→ 즉시 적용:** ML-DSA 키쌍 생성 코드 작성
- [x] **→ 즉시 적용:** ML-DSA 서명 코드 작성
- [x] **→ 즉시 적용:** ML-DSA 검증 코드 작성
- [x] ✅ 단위 테스트 통과 ← **전체 프로젝트 핵심 관문**
  ```kotlin
  @Test
  fun `ML-DSA 서명 검증 성공`() {
      val keyPair = generateMLDSAKeyPair()
      val message = "테스트 문서 해시".toByteArray()
      val signature = sign(keyPair.private, message)
      assertTrue(verify(keyPair.public, message, signature))
  }
  ```

**완료 기준:** 위 테스트 통과

---

### 1-3. PDFBox → PDF 서명 삽입·추출 구현 (1주)

**목표:** PDFBox 구조를 익히고 바로 ML-DSA 서명값을 PDF에 넣고 꺼낸다

- [ ] PDFBox 기본 구조 파악 (PDDocument, PDPage 등)
- [ ] PDF 메타데이터 구조 이해
- [x] **→ 즉시 적용:** PDFBox 의존성 추가 (Gradle)
- [x] **→ 즉시 적용:** PDF 파일 열기 / 저장 기본 코드 작성
- [x] **→ 즉시 적용:** ML-DSA 서명값 Base64 인코딩 후 PDF 메타데이터에 삽입
- [x] **→ 즉시 적용:** PDF에서 서명값 추출 및 디코딩
- [x] **→ 즉시 적용:** 삽입 → 추출 → 검증 연동 테스트

**완료 기준:** PDF에 서명값 넣고 꺼내서 ML-DSA 검증 통과

---

### 1-4. 로컬 Docker Compose 환경 완성 (1주)

- [x] Docker Compose 작성 (MinIO 전용 — 앱·MariaDB는 호스트에서 직접 실행)
  - [ ] ~~Spring Boot 앱 컨테이너~~ → 호스트에서 `./gradlew bootRun`
  - [ ] ~~MariaDB 10.11 컨테이너~~ → 호스트 MariaDB 사용 (root/root)
  - [x] MinIO 컨테이너 (로컬 S3 대체)
- [ ] 로컬 환경 실행 확인
  ```bash
  docker compose up -d   # MinIO 기동
  ./gradlew bootRun      # 앱 호스트 직접 실행 (local 프로파일)
  ```

**1단계 완료 기준**
- [x] ML-DSA 단위 테스트 통과
- [x] PDF 서명값 삽입/추출 성공
- [ ] Docker Compose로 로컬 환경 실행

---

## 2단계: 백엔드 핵심 구현
> 기간: 6주~3개월

### 2-1. 암호화 설계 → 인증 API 구현

**목표:** 개인키 암호화 저장 전략을 이해하고 바로 회원가입 API를 만든다

- [ ] PBKDF2 개념 이해 (비밀번호 기반 키 파생)
- [ ] AES-256 대칭 암호화 개념 이해
- [ ] SHA3-256 해시 함수 개념 이해
- [ ] 개인키 암호화 저장 전략 이해
  ```
  비밀번호 → PBKDF2 → 파생 키 → AES-256으로 개인키 암호화 → DB 저장
  ```
- [ ] JWT 보안 설계 복습 (만료 시간, 갱신 전략)
- [ ] **→ 즉시 적용:** 회원가입 API
  - [ ] ML-DSA 키쌍 생성
  - [ ] PBKDF2로 비밀번호 기반 파생 키 생성
  - [ ] AES-256으로 개인키 암호화
  - [ ] 암호화된 개인키 + 공개키 DB 저장
- [ ] **→ 즉시 적용:** JWT 로그인 API
- [ ] **→ 즉시 적용:** JWT 인증 필터 구현

---

### 2-2. 문서 처리 API 구현

- [ ] 1회용 토큰 만료 처리 패턴 학습
- [ ] **→ 즉시 적용:** PDF 업로드 API
  - [ ] MultipartFile 수신
  - [ ] SHA3-256 해시 생성
  - [ ] MinIO 저장
  - [ ] documents 테이블 저장
- [ ] **→ 즉시 적용:** 문서 목록 조회 API
- [ ] **→ 즉시 적용:** 서명된 PDF 다운로드 API

---

### 2-3. 서명 플로우 API 구현

- [ ] **→ 즉시 적용:** 서명 요청 API
  - [ ] 1회용 토큰 생성 (UUID + 24시간 만료)
  - [ ] signature_requests 테이블 저장
  - [ ] (이메일은 3단계에서 연동, 지금은 토큰 반환만)
- [ ] **→ 즉시 적용:** 서명 실행 API
  - [ ] 토큰 유효성 검증 (존재 여부 + 만료 여부)
  - [ ] 사용자 개인키 복호화
  - [ ] ML-DSA 서명값 생성
  - [ ] PDF에 서명값 삽입
  - [ ] S3 저장
  - [ ] signatures 테이블 기록
  - [ ] 개인키 메모리에서 즉시 삭제
- [ ] **→ 즉시 적용:** 무결성 검증 API (공개, 인증 불필요)
  - [ ] PDF 업로드 → 서명값 추출
  - [ ] 공개키로 검증
  - [ ] 결과 반환 (서명자 / 서명 시각 / 변조 여부)

---

### 2-4. 레이어 분리

- [ ] `PqcSignatureService` — liboqs-java 래핑, ML-DSA 전담
- [ ] `PdfSignatureService` — PDFBox 래핑, 서명 삽입/추출 전담
- [ ] `StorageService` — MinIO/S3 추상화

**2단계 완료 기준**
- [ ] Postman으로 회원가입 → 업로드 → 서명 요청 → 서명 → 검증 전체 플로우 동작

---

## 3단계: 프론트엔드 + 이메일 연동
> 기간: 3~5개월

### 3-1. Vue 3 Composition API + Pinia → 화면 구현

**목표:** Vue 3 개념을 익히고 바로 각 페이지를 만든다

- [ ] `setup()` 이해
- [ ] `ref`, `reactive` 차이 이해
- [ ] `computed`, `watch` 사용법
- [ ] `onMounted` 등 라이프사이클 훅
- [ ] **→ 즉시 적용:** Vue 3 + Vite + Pinia + Vue Router 프로젝트 세팅
- [ ] **→ 즉시 적용:** Axios 인스턴스 + JWT 인터셉터 설정
- [ ] **→ 즉시 적용:** 인증 스토어 (Pinia) — 로그인 상태 관리
- [ ] **→ 즉시 적용:** 로그인 / 회원가입 페이지

---

### 3-2. Vue Router + Pinia 심화 → 나머지 화면 구현

- [ ] 라우트 정의 방법
- [ ] 네비게이션 가드 (로그인 체크)
- [ ] 동적 라우트 (`/sign/:token`)
- [ ] 스토어 정의 (`defineStore`) / state, getters, actions 구조
- [ ] **→ 즉시 적용:** 문서 목록 페이지
- [ ] **→ 즉시 적용:** PDF 업로드 페이지
- [ ] **→ 즉시 적용:** 서명 요청 페이지 (서명자 이메일 입력)
- [ ] **→ 즉시 적용:** 서명자 페이지 (`/sign/:token`)
  - [ ] 비회원 접근 가능
  - [ ] 이메일 OTP 인증
  - [ ] 문서 미리보기 (PDF.js 활용)
  - [ ] 서명 버튼
- [ ] **→ 즉시 적용:** 무결성 검증 페이지
  - [ ] PDF 드래그앤드롭 업로드
  - [ ] 검증 결과 표시 (서명자 / 서명 시각 / 변조 여부)

---

### 3-3. AWS SES → 이메일 연동

- [ ] AWS SES 샌드박스 설정 및 이메일 인증 프로세스 이해
- [ ] **→ 즉시 적용:** 이메일 발송 서비스 구현
- [ ] **→ 즉시 적용:** 서명 요청 이메일 템플릿 작성
- [ ] **→ 즉시 적용:** 서명 완료 이메일 템플릿 작성
- [ ] **→ 즉시 적용:** 실제 이메일 수신 테스트

**3단계 완료 기준**
- [ ] 로컬에서 실제 이메일 받고 서명까지 전체 플로우 동작

---

## 4단계: AWS 배포 + GitHub Actions
> 기간: 5~7개월

### 4-1. AWS 네트워크 개념 → 인프라 콘솔 구성

**목표:** VPC/IAM 개념을 이해하고 바로 콘솔에서 손으로 만든다 (Terraform 전 필수)

- [ ] VPC 개념 이해 (서브넷, 라우팅 테이블, 인터넷 게이트웨이)
- [ ] 보안 그룹 vs NACL 차이 이해
- [ ] IAM 역할 / 정책 설계 원칙 이해 (최소 권한 원칙)
- [ ] RDS 서브넷 그룹 개념 이해
- [ ] S3 버킷 정책 및 ACL 이해
- [ ] **→ 즉시 적용:** VPC 생성 (퍼블릭 / 프라이빗 서브넷 분리)
- [ ] **→ 즉시 적용:** 보안 그룹 생성 (EC2, RDS 각각)
- [ ] **→ 즉시 적용:** EC2 생성 + Docker 설치
- [ ] **→ 즉시 적용:** RDS MariaDB 생성 (프라이빗 서브넷)
- [ ] **→ 즉시 적용:** S3 버킷 생성 + IAM 정책 설정
- [ ] **→ 즉시 적용:** SES 이메일 도메인 인증
- [ ] **→ 즉시 적용:** IAM 역할 생성 (EC2 → S3, SES 접근)

---

### 4-2. GitHub Actions → CI/CD 파이프라인 구축

- [ ] workflow 파일 구조 이해 (on, jobs, steps)
- [ ] secrets 등록 및 사용법
- [ ] Docker 이미지 빌드 + 푸시 액션
- [ ] SSH를 통한 EC2 배포 방법
- [ ] 브랜치별 배포 전략 (main → 운영, develop → 스테이징)
- [ ] **→ 즉시 적용:** GitHub Actions workflow 파일 작성
  ```yaml
  push → main
    ↓
  1. Gradle 빌드 + 테스트
  2. Docker 이미지 빌드
  3. ECR 푸시
  4. EC2 SSH 배포
  ```
- [ ] **→ 즉시 적용:** GitHub Secrets 등록 (AWS 키, EC2 SSH 키 등)
- [ ] **→ 즉시 적용:** 파이프라인 동작 확인

---

### 4-3. 배포 마무리

- [ ] MinIO → AWS S3 전환
- [ ] 로컬 DB → AWS RDS 전환
- [ ] 도메인 구매 + Route53 연결
- [ ] HTTPS 설정 (Let's Encrypt)
- [ ] 베타 사용자 10명 모집

**4단계 완료 기준**
- [ ] 실제 도메인으로 HTTPS 접속 가능
- [ ] GitHub Actions 푸시 시 자동 배포 동작

---

## 5단계: Terraform + 수익화
> 기간: 7~10개월

### 5-1. Terraform 개념 → 인프라 코드화

**목표:** IaC 개념을 익히고 바로 4단계 인프라를 코드로 옮긴다

- [ ] IaC 개념 이해 (왜 코드로 인프라를 관리하는가)
- [ ] Terraform 기본 명령어 (`init`, `plan`, `apply`, `destroy`)
- [ ] HCL 문법 기초 (resource, variable, output, data)
- [ ] provider 개념 이해 (AWS provider)
- [ ] state 파일 개념 이해 (로컬 vs S3 백엔드)
- [ ] 모듈 개념 이해
- [ ] **→ 즉시 적용:** Terraform 프로젝트 구조 설계
  ```
  terraform/
    ├── main.tf
    ├── vpc.tf
    ├── ec2.tf
    ├── rds.tf
    ├── s3.tf
    └── variables.tf
  ```
- [ ] **→ 즉시 적용:** VPC / 서브넷 / 보안 그룹 코드화
- [ ] **→ 즉시 적용:** EC2 인스턴스 코드화
- [ ] **→ 즉시 적용:** RDS MariaDB 코드화
- [ ] **→ 즉시 적용:** S3 버킷 코드화
- [ ] **→ 즉시 적용:** Terraform state S3 백엔드 설정
- [ ] **→ 즉시 적용:** `terraform plan` 으로 기존 인프라와 diff 확인
- [ ] **→ 즉시 적용:** `terraform apply` 검증

---

### 5-2. 수익화 구현

- [ ] 결제 모듈 연동 (토스페이먼츠 또는 아임포트)
- [ ] 무료 플랜 (월 5건) 구현
- [ ] 유료 플랜 (월 50건, 9,900원) 구현
- [ ] 플랜별 사용량 제한 로직
- [ ] 다중 서명자 기능 추가

---

### 5-3. 문서화 + 홍보

- [ ] RSA vs ML-DSA 성능 벤치마크 측정
  - [ ] 키 생성 시간 비교
  - [ ] 서명 시간 비교
  - [ ] 검증 시간 비교
  - [ ] 벤치마크 결과 README에 수치로 정리
- [ ] 기술 블로그 연재 시작
  - [ ] liboqs-java Docker 환경 세팅기
  - [ ] ML-DSA 실전 적용기
  - [ ] Kotlin + Spring Boot PQC 구현기

**5단계 완료 기준**
- [ ] Terraform으로 인프라 재현 가능
- [ ] 첫 유료 결제 발생

---

## 6단계: Loki + Grafana + 이직 준비
> 기간: 10~12개월

### 6-1. Loki + Grafana 개념 → 모니터링 스택 구축

**목표:** 로그 수집 구조를 이해하고 바로 대시보드를 만든다

- [ ] Loki 개념 이해 (Elasticsearch와 차이점)
- [ ] Promtail 개념 이해 (로그 수집 에이전트)
- [ ] Grafana 대시보드 기본 사용법
- [ ] LogQL 기본 쿼리 문법
- [ ] 알림(Alert) 설정 방법
- [ ] **→ 즉시 적용:** Loki + Promtail + Grafana Docker Compose 구성
- [ ] **→ 즉시 적용:** Spring Boot Logback → Promtail 연동
- [ ] **→ 즉시 적용:** Grafana 데이터소스 Loki 연결
- [ ] **→ 즉시 적용:** 대시보드 구성
  - [ ] 일별 서명 요청 건수
  - [ ] 검증 성공/실패 비율
  - [ ] API 응답 시간 분포
  - [ ] 이상 접근 탐지 (짧은 시간 내 대량 요청)
- [ ] **→ 즉시 적용:** 이상 접근 알림 설정

---

### 6-2. 포트폴리오 완성

- [ ] GitHub README 기술 문서화
  - [ ] 서비스 소개 및 PQC 적용 배경
  - [ ] 아키텍처 다이어그램
  - [ ] ML-DSA vs RSA 벤치마크 결과
  - [ ] 로컬 실행 방법
  - [ ] API 문서 링크
- [ ] 이력서 업데이트
  ```
  - NIST 표준 ML-DSA 기반 전자서명 서비스 설계 및 개발 (Kotlin + Spring Boot)
  - liboqs-java 기반 PQC 암호화 실전 적용
  - Terraform으로 AWS 인프라 코드화 (VPC / EC2 / RDS / S3)
  - GitHub Actions CI/CD 파이프라인 구축 및 운영
  - Loki + Grafana 기반 운영 모니터링 구성
  - 실 서비스 운영 (유료 사용자 보유)
  ```

---

### 6-3. 이직 준비

- [ ] AWS SAA 시험 준비 (7~8개월부터 병행)
  - [ ] SAA 시험 범위 파악
  - [ ] 기출 문제 풀기
  - [ ] 취약 영역 보완
  - [ ] 시험 접수 및 응시
- [ ] 클라우드 보안 엔지니어 채용 공고 분석
  - [ ] KT / SKT / 삼성SDS / LG CNS / 금융보안원 공고 수집
  - [ ] 요구 스킬 정리
  - [ ] 부족한 부분 보완
- [ ] 이직 지원 시작

**6단계 완료 기준**
- [ ] Loki + Grafana 운영 중
- [ ] GitHub README 완성
- [ ] 이직 서류 제출

---

## 마일스톤 체크

| 시점 | 목표 | 완료 |
|------|------|------|
| 2주 | Kotlin + Spring Boot 실행 확인 | ⬜ |
| 4주 | **ML-DSA 서명 / 검증 단위 테스트 통과** | ⬜ |
| 6주 | PDF ML-DSA 서명값 삽입 / 추출 성공 | ⬜ |
| 3개월 | 백엔드 API 전체 완성 | ⬜ |
| 5개월 | Vue 3 프론트 + SES 이메일 연동 완성 | ⬜ |
| 7개월 | AWS 배포 완료 + GitHub Actions 운영 중 | ⬜ |
| 9개월 | Terraform 코드화 완료 + 유료 플랜 출시 | ⬜ |
| 12개월 | Loki + Grafana 운영 + 이직 지원 시작 | ⬜ |

---

## 자격증 체크

| 순서 | 자격증 | 목표 시점 | 완료 |
|------|--------|-----------|------|
| 1 | AWS SAA | 7~8개월 | ⬜ |
| 2 | 정보보안기사 | 이직 후 | ⬜ |

---

## 막혔을 때 체크리스트

> 개발 중 막히면 이 순서대로 확인

- [ ] Docker 로그 확인 (`docker compose logs -f`)
- [ ] liboqs 빌드 에러 → OQS 공식 이슈 트래커 확인
- [ ] PDFBox 서명 에러 → PDFBox 공식 예제 코드와 비교
- [ ] AWS 배포 에러 → CloudWatch 로그 확인
- [ ] Terraform 에러 → `terraform plan`으로 변경사항 먼저 확인

---

> **지금 당장 할 것 딱 하나:**  
> `docker pull openquantumsafe/liboqs`
