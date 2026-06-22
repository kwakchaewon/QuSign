---
title: QuSign — Diagram 02: AWS Cloud Infrastructure
version: 1.0
usage: claude.ai/design — "Start with" 파일로 첨부
---

Draw an "AWS Cloud Infrastructure" diagram for QuSign, a PQC electronic signature
web service. Style: clean, light-background, AWS reference architecture slide.
One page, no scrolling.

━━━ CANVAS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Size       : 1440 × 810px  (16:9)
Background : #ffffff
Title      : "AWS Cloud Infrastructure"
             top-left, 36px Inter weight 700, #000000
Subtitle   : "ap-southeast-1 (싱가포르) · EC2 단일 구성 · 월 ~$12"
             below title, 14px Inter 400, #888888
Divider    : 1px solid #e6e6e6, full width below subtitle

━━━ LAYOUT (top → center → bottom) ━━━━━━━━━━━━

Row 0 (top, centered):
  [INTERNET]  ──→  [ROUTE 53]

Row 0.5 (top-right corner, outside VPC):
  [EVENTBRIDGE SCHEDULER GROUP]  ──start/stop──→  (into EC2 GROUP, Row 1)

Row 1 (center, dominant):
  Large VPC group box containing:
    - EC2 GROUP (main compute, left-center inside VPC)
    - SECURITY GROUP badge (small, overlapping EC2 group border)

Row 2 (bottom, three boxes under EC2):
  [S3 STORAGE]    [AWS SES]    [SSM PARAMETER STORE]

━━━ COMPONENT CARD ━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Every technology = one card:
  Background  : #f7f7f5
  Border      : 1px solid #e6e6e6
  Border-radius: 8px
  Width/Height: 110 × 110px
  Content     : official brand logo (64px) centered + name below (13px Inter 500, #000000)
  NO sub-text, NO drop shadow

━━━ GROUP BOX ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Rounded rect enclosing related cards:
  Border-radius : 20px
  Border        : 2px solid [group color]
  Background    : [group tint]
  Label         : top of box, 12px JetBrains Mono ALL CAPS, [group color]

━━━ CAPTION LAYOUT RULE (전체 적용) ━━━━━━━━━━━━

모든 캡션은 해당 카드 name 바로 아래에 수직 배치:
  Placement  : card name 아래에 세로 스택 — 아이콘 옆(오른쪽)에 절대 배치 금지
  Alignment  : 카드 열 내 수평 중앙 정렬
  Max-width  : 가로 나열(side-by-side) 레이아웃 → 110px; 세로 스택 그룹 → 그룹 inner width
  Word-wrap  : 항상 활성화 — 긴 텍스트는 다음 줄로 줄바꿈, 절대 경계 밖으로 오버플로우 금지

가로 나열(side-by-side) 카드 레이아웃:
  [카드 + 캡션] 쌍은 각각 독립적인 수직 열(column)을 형성.
  캡션은 로고 옆이 아닌 name 텍스트 아래에 위치.

━━━ ROW 0 — INTERNET + ROUTE 53 ━━━━━━━━━━━━━━━

Two floating elements, centered horizontally at top. No group border.

  Element A:
    Label (above): "INTERNET"  12px JetBrains Mono, #888888
    Icon: globe / cloud icon  (64px)

  Arrow → right:
    Style: 2px solid #aaaaaa, filled arrowhead
    Label: "HTTP/HTTPS"

  Element B:
    Label (above): "DNS"  12px JetBrains Mono, #888888
    Card: [Route 53 purple icon]  name: "Route 53"
    Sub: "qusign.link → 3.0.193.52"  11px Inter, #888888

  Arrow ↓ downward to VPC group:
    Style: 2px solid #9b7ee0, filled arrowhead pointing down
    Label: "A 레코드 / CNAME"  11px JetBrains Mono, #9b7ee0

━━━ ROW 0.5 — EC2 운영시간 스케줄러 (EVENTBRIDGE) ━━━

Floating group, top-right corner of canvas, outside the VPC border
(EventBridge/Lambda are regional services, not inside the VPC).

GROUP — SCHEDULER
  Group color : #cc3333 (red)
  Group tint  : #fff5f5
  Group label line 1 : [clock icon 16px]  "EC2 운영시간 스케줄러"
  Group label line 2 : "KST 09:00 시작 · 21:30 정지 (Asia/Seoul)"  10px Inter, #888888
  Group width : ~240px

  Cards (horizontal, side by side — each card+caption is an independent vertical column):
    Card: [Amazon EventBridge icon]  name: "EventBridge Scheduler"
    Caption (BELOW card name, max-width 110px, word-wrap, centered):
      "cron(0 9 * * ? *)"
      "cron(30 21 * * ? *)"

    Card: [AWS Lambda orange λ icon]  name: "Lambda"
    Caption (BELOW card name, max-width 110px, word-wrap, centered):
      "qusign_start_instances"
      "ec2.start/stop_instances()"

  Arrow ↓ down-left into EC2 group (top edge, passing through VPC border):
    Style: 2px dashed #cc3333, filled arrowhead
    Label: "start / stop (EC2 제어)"  11px JetBrains Mono, #cc3333

━━━ ROW 1 — VPC GROUP ━━━━━━━━━━━━━━━━━━━━━━━━━

Group color : #147eba (AWS blue)
Group tint  : #eef6fc
Group label line 1 : [AWS VPC icon 16px]  "VPC  ·  qusign_vpc"  12px JetBrains Mono, #147eba
Group label line 2 : "10.0.0.0/16  ·  ap-southeast-1"  10px Inter, #888888  (directly below line 1)
Group width : ~1000px  (dominant, ~70% canvas width)

Inside VPC:

  ┌── Subnet badge ──────────────────────────────┐
  │  "퍼블릭 서브넷  ·  ap-southeast-1a"          │
  │  12px JetBrains Mono, #888888                 │
  └──────────────────────────────────────────────┘

  EC2 GROUP (inside VPC, center):
    Group color : #e8760a (AWS orange)
    Group tint  : #fff8f2
    Group label line 1 : [EC2 icon 16px]  "EC2  ·  qusign_app"  12px JetBrains Mono, #e8760a
    Group label line 2 : "t3.small  ·  3.0.193.52"  10px Inter, #888888  (directly below line 1)
    Group width : ~680px

    Cards arranged in TWO rows inside EC2:

    Row A — 요청 경로 (좌→우):
      Card: [Nginx green N]                name: "Nginx"
      Caption (BELOW card name, max-width 110px, word-wrap, centered):
        "80→443 리다이렉트"
        "/api 프록시 · SPA 서빙"

      Arrow →: 2px #e8760a, label "proxy :8080"

      Card: [Spring leaf + Kotlin K]       name: "Spring Boot 3"
      Caption (BELOW card name, max-width 110px, word-wrap, centered):
        "Docker container (compose)"
        "127.0.0.1:8080 포트 매핑"

    Row B — 데이터 레이어 (Spring Boot 아래, 나란히):
      Card: [MariaDB gold logo]            name: "MariaDB 10.11"
      Caption (BELOW card name, max-width 110px, word-wrap, centered):
        "Docker · 내부 네트워크 전용"
        "named volume: mariadb_data"

      Card: [Redis red logo]              name: "Redis 7"
      Caption (BELOW card name, max-width 110px, word-wrap, centered):
        "Docker · redis:7-alpine"
        "내부 네트워크 전용 (포트 미노출)"

      Arrow UP from Spring Boot → MariaDB: 2px #aaaaaa, label "JDBC"
      Arrow UP from Spring Boot → Redis  : 2px #aaaaaa, label "Pub/Sub"

    Row C — 모니터링 레이어 (Row B 아래, 나란히):
      Card: [Grafana Loki icon]           name: "Grafana Loki"
      Caption (BELOW card name, max-width 110px, word-wrap, centered):
        "Docker · Promtail 에이전트 포함"
        "Spring Boot 로그 수집"

      Card: [Grafana orange G icon]       name: "Grafana"
      Caption (BELOW card name, max-width 110px, word-wrap, centered):
        "Docker · :3000 (관리자 접근)"
        "대시보드 · 이상 접근 알림"

      Arrow from Spring Boot → Loki: 2px solid #e56717, label "Promtail 로그 수집"

  Elastic IP badge (floating, overlapping EC2 group top-right corner):
    Pill shape, background: #fff8f2, border: 2px solid #e8760a
    Text: "Elastic IP  3.0.193.52"  11px JetBrains Mono, #e8760a

  Security Group badge (overlapping EC2 group border, bottom-left):
    Pill shape, background: #fff0f0, border: 1px solid #cc3333
    Text: "SG: 22(0.0.0.0/0 · GitHub Actions) · 80 · 443"  10px JetBrains Mono, #cc3333

  S3 VPC Endpoint (inside VPC, right side, outside EC2 group):
    Small floating element:
    Label: "VPC ENDPOINT"  11px JetBrains Mono, #3d8f3d
    Icon: [AWS endpoint icon or route icon]
    Sub: "Gateway type · 무료 · 인터넷 미경유"  10px Inter, #666666

    Arrow from EC2 → VPC Endpoint → S3:
      Style: 2px solid #3d8f3d, dashed inside VPC
      Label: "S3 트래픽 (내부)"

━━━ ROW 2 — BOTTOM THREE BOXES ━━━━━━━━━━━━━━━━

Three equal-width group boxes, horizontally centered under VPC group.
Total width = VPC group width (~1000px). Each ~316px wide. 24px gaps. (316×3 + 24×2 = 996px)

BOX A — S3 STORAGE
  Group color : #3d8f3d (green)
  Group tint  : #f2faf2
  Label       : "STORAGE"
  Card: [S3 green bucket icon]  name: "Amazon S3"
  Caption: "qusign_documents_prod  ·  SSE-S3 암호화"  11px Inter, #666666
  Caption line 2: "퍼블릭 액세스 차단  ·  EC2 IAM 역할만 허용"

BOX B — AWS SES
  Group color : #c47a1e (amber)
  Group tint  : #fffaf2
  Label       : "EMAIL"
  Card: [SES orange envelope icon]  name: "Amazon SES"
  Caption: "서명 요청 · 서명 완료 · 만료 리마인더"  11px Inter, #666666
  Caption line 2: "샌드박스 → 프로덕션 전환 예정"

BOX C — SSM PARAMETER STORE
  Group color : #9b7ee0 (lilac)
  Group tint  : #f5f1fd
  Label       : "SECRETS"
  Card: [AWS Systems Manager icon]  name: "SSM Param Store"
  Caption: "SecureString · KMS 암호화"  11px Inter, #666666
  Caption line 2: "DB 비밀번호 · JWT 시크릿 · S3 버킷명 · CORS Origin"

━━━ ARROWS — EC2 → BOTTOM BOXES ━━━━━━━━━━━━━━━

All downward from EC2 group bottom edge (passing through VPC border):
  → S3          : 2px solid #3d8f3d,  label "PDF 업로드 / 다운로드 (VPC Endpoint)"
  → SES         : 2px solid #c47a1e,  label "서명 링크 · 완료 알림 이메일"
  → SSM         : 2px dashed #9b7ee0, label "EC2 시작 시 SecureString 조회"

━━━ MONITORING STACK NOTE ━━━━━━━━━━━━━━━━━━━━━

Row C (Loki + Grafana)는 EC2 내부 Docker 컨테이너로 운영:
  - Spring Boot → Promtail(사이드카) → Loki 로그 수집
  - Grafana가 Loki를 데이터소스로 연결하여 대시보드 표시
  - 외부 접근: Nginx에서 /grafana 경로 프록시 또는 :3000 직접 접근 (관리자 전용)

━━━ REGION COST BADGE (floating, bottom-left corner) ━━

Small info box, bottom-left corner of canvas (below Row 2, left-aligned with VPC group):
  Background: #f0f6ff, border: 1px solid #147eba, radius: 12px
  Width: ~300px
  Title: "리전 선택 근거"  11px JetBrains Mono ALL CAPS, #147eba
  Line 1: "ap-southeast-1 (싱가포르)  ·  EC2 t3.small $0.023/h"  10px Inter, #444444
  Line 2: "ap-northeast-2 (서울) 대비 약 18% 저렴 → 월 ~$12 달성"  10px Inter, #444444

━━━ IAM ROLE BADGE (floating, right margin) ━━━━━

Small info box, right side of canvas:
  Background: #f7f7f5, border: 1px solid #e6e6e6, radius: 12px
  Title: "IAM ROLES"  11px JetBrains Mono, #888888
  Line 1: "qusign_ec2_role  →  S3 · SES · SSM · ECR 접근"  10px Inter, #444444
  Line 2: "qusign_github_actions_deployer  →  ECR push · SSM · EC2 제어"

━━━ ARROW LABEL STYLE ━━━━━━━━━━━━━━━━━━━━━━━━━

  Font       : 11px JetBrains Mono
  Color      : same as arrow line
  Background : white pill (#ffffff), 1px solid [arrow color] 40% opacity
  Padding    : 2px 8px, border-radius 50px
  Position   : centered on arrow line

━━━ TYPOGRAPHY ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Page title      : 36px Inter 700, #000000
  Page subtitle   : 14px Inter 400, #888888
  Group label     : 12px JetBrains Mono ALL CAPS, letter-spacing 0.5px
  Card name       : 13px Inter 500, #000000
  Caption text    : 11px Inter 400, #666666
  Arrow label     : 11px JetBrains Mono, arrow color
  Badge text      : 10px–11px JetBrains Mono

━━━ CONSTRAINTS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

- Total elements on canvas: ~14 cards, ~11 arrows — Row C adds Loki + Grafana inside EC2
- VPC group is the dominant visual (~70% canvas width, ~50% canvas height)
- EC2 group inside VPC is the compute anchor
- 48px white margin on all 4 edges
- 32px gap between all group boxes
- No gradients, no drop shadows, flat design
- All brand logos must be recognizable official icons (not generic shapes)
- Korean labels on arrows and captions exactly as written above
- All captions: BELOW their card name in a vertical stack — never beside or to the right of the icon
- Text overflow prevention: captions word-wrap within their column; no text extends beyond group box boundary
