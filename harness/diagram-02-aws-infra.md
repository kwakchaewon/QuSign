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

━━━ ROW 1 — VPC GROUP ━━━━━━━━━━━━━━━━━━━━━━━━━

Group color : #147eba (AWS blue)
Group tint  : #eef6fc
Group label : [AWS VPC icon 16px]  "VPC  ·  qusign_vpc  ·  10.0.0.0/16  ·  ap-southeast-1"
Group width : ~1000px  (dominant, ~70% canvas width)

Inside VPC:

  ┌── Subnet badge ──────────────────────────────┐
  │  "퍼블릭 서브넷  ·  ap-southeast-1a"          │
  │  12px JetBrains Mono, #888888                 │
  └──────────────────────────────────────────────┘

  EC2 GROUP (inside VPC, center):
    Group color : #e8760a (AWS orange)
    Group tint  : #fff8f2
    Group label : [EC2 icon 16px]  "EC2  ·  qusign_app  ·  t3.small  ·  3.0.193.52 (Elastic IP)"
    Group width : ~680px

    Cards arranged in TWO rows inside EC2:

    Row A — 요청 경로 (좌→우):
      Card: [Nginx green N]                name: "Nginx"
      Caption: "80 → 443 리다이렉트 · /api 프록시 · SPA 서빙"

      Arrow →: 2px #e8760a, label "proxy :8080"

      Card: [Spring leaf + Kotlin K]       name: "Spring Boot 3"
      Caption: "Docker container · --network host"

    Row B — 데이터 레이어 (Spring Boot 아래, 나란히):
      Card: [MariaDB gold logo]            name: "MariaDB 10.11"
      Caption: "Docker · 127.0.0.1:3306 · /var/lib/qusign-db 볼륨"

      Card: [Redis red logo]              name: "Redis 7"
      Caption: "Docker · redis:7-alpine · 포트 6379"

      Arrow UP from Spring Boot → MariaDB: 2px #aaaaaa, label "JDBC"
      Arrow UP from Spring Boot → Redis  : 2px #aaaaaa, label "Pub/Sub"

  Elastic IP badge (floating, overlapping EC2 group top-right corner):
    Pill shape, background: #fff8f2, border: 2px solid #e8760a
    Text: "Elastic IP  3.0.193.52"  11px JetBrains Mono, #e8760a

  Security Group badge (overlapping EC2 group border, bottom-left):
    Pill shape, background: #fff0f0, border: 1px solid #cc3333
    Text: "SG: 22(내IP) · 80 · 443"  10px JetBrains Mono, #cc3333

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
Total width = VPC group width (~1000px). Each ~308px wide. 24px gaps. (308×3 + 24×2 = 972px)

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
  Caption line 2: "DB 비밀번호 · JWT 시크릿 · S3 버킷명"

━━━ ARROWS — EC2 → BOTTOM BOXES ━━━━━━━━━━━━━━━

All downward from EC2 group bottom edge (passing through VPC border):
  → S3          : 2px solid #3d8f3d,  label "PDF 업로드 / 다운로드 (VPC Endpoint)"
  → SES         : 2px solid #c47a1e,  label "서명 링크 · 완료 알림 이메일"
  → SSM         : 2px dashed #9b7ee0, label "EC2 시작 시 SecureString 조회"

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

- Total elements on canvas: ~10 cards, ~8 arrows — keep it this count, no more
- VPC group is the dominant visual (~70% canvas width, ~50% canvas height)
- EC2 group inside VPC is the compute anchor
- 48px white margin on all 4 edges
- 32px gap between all group boxes
- No gradients, no drop shadows, flat design
- All brand logos must be recognizable official icons (not generic shapes)
- Korean labels on arrows and captions exactly as written above
