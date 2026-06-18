---
title: QuSign Service Architecture Diagram
version: 1.0
usage: claude.ai/design — "Start with" 파일로 첨부
---

Draw a "Service Architecture" diagram for QuSign, a PQC (Post-Quantum Cryptography)
electronic signature web service. Style: clean, light-background, AWS reference architecture
slide. One page, no scrolling.

━━━ CANVAS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Size       : 1440 × 810px  (16:9)
Background : #ffffff
Title      : "Service Architecture"
             top-left, 36px Inter weight 700, #000000
Divider    : 1px solid #e6e6e6, full width below title

━━━ LAYOUT (2-row, matches AWS reference style) ━

                  [ROUTE 53 — small, above EC2 group]
                          ↓
Row 1 (top half):
  [CLIENT]  ──→  [EC2 GROUP — large center box]

Row 2 (bottom half, three boxes centered under EC2):
  [S3 STORAGE]    [AWS SES]    [GitHub Actions + ECR]

Vertical arrows from EC2 GROUP bottom edge down to each bottom box.
Bottom boxes are centered horizontally under the EC2 group.

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

━━━ ROW 1 — LEFT: CLIENT ━━━━━━━━━━━━━━━━━━━━━━

No border box. Single card + label above.

  Label (above card) : "CLIENT"  12px JetBrains Mono, #888888
  Card 1 : [Vue.js green V logo]   name: "Vue 3 SPA"

Arrow → right toward EC2:
  Style : 2px solid #5aad6e, filled arrowhead
  Label : "HTTPS 443"  on arrow, 11px JetBrains Mono, #5aad6e

━━━ ROW 1 — CENTER: EC2 GROUP ━━━━━━━━━━━━━━━━━

Group color : #e8760a (AWS orange)
Group tint  : #fff8f2
Group label : [AWS EC2 icon 16px]  "EC2  ·  ap-southeast-1a  ·  t3.small"
Group width : ~780px  (dominant element, ~54% of canvas width)

Cards inside, arranged in THREE rows top → bottom:

  Row A — 요청 경로 (상단, 좌→우):
    Card: [Nginx green N]          name: "Nginx"
    Card: [Spring leaf + Kotlin K] name: "Spring Boot 3 / Kotlin"

    Arrow between: ←→ 2px #e8760a, label "proxy"

  Row B — 데이터 레이어 (중단, Spring Boot 바로 아래에 나란히):
    Card: [MariaDB gold logo]  name: "MariaDB 10.11"
    Card: [Redis red logo]     name: "Redis 7"

    Arrow UP: Spring Boot → MariaDB : 2px #aaaaaa, label "JDBC"
    Arrow UP: Spring Boot → Redis   : 2px #aaaaaa, label "Pub/Sub"

  Row C — PQC 암호 (하단, 점선 서브박스):
    Inner dashed box:
      Border: 1.5px dashed #9b7ee0, background: #f5f1fd, radius: 12px
      Label: "PQC CRYPTO"  11px JetBrains Mono, #9b7ee0
      Card: [shield icon]          name: "liboqs-java"
      Card: [Apache feather icon]  name: "PDFBox"

    Arrow UP: Spring Boot → PQC box : 2px dashed #9b7ee0, label "JNI"

  NOTE: Row A and Row B are visually separated — Row A shows the request path,
        Row B shows data storage. MariaDB/Redis are BELOW Spring Boot, not beside it.

━━━ ROUTE 53 (EC2 그룹 위, 중앙 상단) ━━━━━━━━━━━

Small floating element, centered above the EC2 group box. No group border.

  Label (above card) : "DNS"  12px JetBrains Mono, #888888
  Card: [Route 53 purple icon]  name: "Route 53"
  Sub  : "qusign.link"  11px Inter, #888888

  Arrow ↓ downward → EC2 group top edge:
    Style: 2px solid #aaaaaa, filled arrowhead pointing down
    Label: "qusign.link → 3.0.193.52"  11px JetBrains Mono, #888888

━━━ ROW 2 — BOTTOM THREE BOXES ━━━━━━━━━━━━━━━━

Three equal-width group boxes, horizontally centered under EC2 group.
Total width of three boxes = EC2 group width (~780px).
Each box ~244px wide. 24px gaps between them. (244×3 + 24×2 = 780px)

BOX A — S3 STORAGE
  Group color : #3d8f3d (green)
  Group tint  : #f2faf2
  Label       : "STORAGE"
  Card: [S3 green bucket icon]  name: "Amazon S3"
  Caption below card: "PDF 파일 저장 · VPC Endpoint"  11px Inter, #666666

BOX B — AWS SES
  Group color : #c47a1e (amber)
  Group tint  : #fffaf2
  Label       : "EMAIL"
  Card: [SES orange envelope icon]  name: "Amazon SES"
  Caption: "서명 요청 · 만료 알림 이메일"  11px Inter, #666666

BOX C — CI/CD
  Group color : #9b7ee0 (lilac)
  Group tint  : #f5f1fd
  Label       : "CI / CD"
  Card: [GitHub octocat]         name: "GitHub"
  Card: [GitHub Actions icon]    name: "GitHub Actions"
  Card: [AWS ECR orange icon]    name: "Amazon ECR"
  (3 cards in a row inside this box)

━━━ ARROWS — EC2 → BOTTOM BOXES ━━━━━━━━━━━━━━━

All downward from EC2 group bottom edge:
  → S3     : 2px solid #3d8f3d,  label "PDF 업로드 / 다운로드"
  → SES    : 2px solid #c47a1e,  label "서명 링크 이메일 발송"
  → CI/CD  : 2px dashed #9b7ee0, label "docker pull / SSM deploy"

━━━ ARROW LABEL STYLE ━━━━━━━━━━━━━━━━━━━━━━━━━

  Font       : 11px JetBrains Mono
  Color      : same as arrow line
  Background : white pill (#ffffff), 1px solid [arrow color] 40% opacity
  Padding    : 2px 8px, border-radius 50px
  Position   : centered on arrow line

━━━ TYPOGRAPHY ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Page title      : 36px Inter 700, #000000
  Group label     : 12px JetBrains Mono ALL CAPS, letter-spacing 0.5px
  Card name       : 13px Inter 500, #000000
  Caption text    : 11px Inter 400, #666666
  Arrow label     : 11px JetBrains Mono, arrow color

━━━ CONSTRAINTS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

- Total elements on canvas: ~14 cards, ~9 arrows — keep it this count, no more
- EC2 group is the dominant visual (~54% canvas width, ~45% canvas height)
- 48px white margin on all 4 edges
- 32px gap between all group boxes
- No gradients, no drop shadows, flat design
- All brand logos must be recognizable official icons (not generic shapes)
- Korean labels on arrows exactly as written above
