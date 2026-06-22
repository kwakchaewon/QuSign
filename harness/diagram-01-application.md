---
title: QuSign — Diagram 01: Application Layer Architecture
version: 1.0
usage: claude.ai/design — "Start with" 파일로 첨부
---

Draw a "Application Layer Architecture" diagram for QuSign, a PQC electronic signature
web service. Style: clean, light-background, AWS reference architecture slide.
One page, no scrolling.

━━━ CANVAS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Size       : 1440 × 810px  (16:9)
Background : #ffffff
Title      : "Application Layer Architecture"
             top-left, 36px Inter weight 700, #000000
Subtitle   : "내부 요청 흐름 · 데이터 레이어 · PQC 암호 · 실시간 알림"
             below title, 14px Inter 400, #888888
Divider    : 1px solid #e6e6e6, full width below subtitle

━━━ LAYOUT (3-column pipeline, left → right) ━━

                      ┌── SSE 알림 ──────────────────────────────┐
                      ↑                                           │
[CLIENT GROUP]  ──→  [GATEWAY GROUP]  ──→  [BACKEND GROUP]       │
                                              ↙    ↓    ↘        │
                                        [DATA]  [PQC]  [STORAGE] │
                                                                  └→ (back to CLIENT)

All groups arranged left-to-right in one horizontal band.
Data/PQC/Storage sit BELOW the Backend Group (second row, right half).

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

━━━ GROUP 1 — CLIENT ━━━━━━━━━━━━━━━━━━━━━━━━━━

Group color : #5aad6e (green)
Group tint  : #f2faf4
Group label : "CLIENT"
Group width : ~220px

Cards (vertical stack):
  Card 1: [Vue.js green V logo]   name: "Vue 3 + Vite"
  Card 2: [Pinia pineapple icon]  name: "Pinia"

Caption below Card 1: "SPA · Vue Router · TypeScript"  11px Inter, #666666
Caption below Card 2: "Auth Store · Notification Store"

Arrow → right toward Gateway:
  Style : 2px solid #5aad6e, filled arrowhead
  Label : "HTTPS 443"  11px JetBrains Mono, #5aad6e

SSE arrow ← left FROM Backend group:
  Style : 2px dashed #f0a040, arrow pointing LEFT back to Client
  Label : "SSE / EventSource"  11px JetBrains Mono, #f0a040
  Position: above the main arrow row

━━━ GROUP 2 — GATEWAY ━━━━━━━━━━━━━━━━━━━━━━━━━

Group color : #e8760a (AWS orange)
Group tint  : #fff8f2
Group label : "GATEWAY"
Group width : ~220px

Cards (single):
  Card: [Nginx green N logo]   name: "Nginx"

Caption (BELOW card name, centered, word-wrap, max-width 200px):
  Line 1: "리버스 프록시 · SSL 종료 · SPA 서빙"
  Line 2: "/ → /dist  ·  /api → :8080"
  Line 3: "/api/sse → buffering off"
  Font: 11px Inter, #666666

Arrow → right toward Backend:
  Style : 2px solid #e8760a
  Label : "proxy :8080"

━━━ GROUP 3 — BACKEND ━━━━━━━━━━━━━━━━━━━━━━━━━

Group color : #6fa8dc (blue)
Group tint  : #f0f5fc
Group label : "BACKEND"
Group width : ~260px  (tallest group — anchors the diagram)

Cards (vertical stack):
  Card A: [Spring leaf + Kotlin K icon]  name: "Spring Boot 3 / Kotlin"

  Caption (DIRECTLY BELOW card name, centered, max-width 220px, word-wrap):
    "JWT 인증 · 서명 플로우 · 감사 로그 · 관리자 API"

Feature sub-tags (flex-wrap row BELOW caption, centered inside group):
  [ JWT ]  [ ML-DSA ]  [ AuditLog ]  [ SSE Emitter ]  [ @Scheduled ]
  Font: 10px JetBrains Mono, color: #6fa8dc
  Pills wrap to next line if needed — never extend beyond group border

Arrow ↓ downward to DATA group:
  Style : 2px solid #aaaaaa, label "JDBC / Redis Pub·Sub"
Arrow ↓ downward to PQC group:
  Style : 2px dashed #9b7ee0, label "JCA (BC Provider)"
Arrow ↓ downward to STORAGE group:
  Style : 2px solid #3d8f3d, label "SDK"

━━━ GROUP 4 — DATA LAYER ━━━━━━━━━━━━━━━━━━━━━━

Position: below-left of Backend group
Group color : #c47a1e (amber)
Group tint  : #fffaf2
Group label : "DATA LAYER"
Group width : ~340px

Cards (horizontal, side by side — each card+caption is an independent vertical column):
  Card A: [MariaDB gold dolphin logo]  name: "MariaDB 10.11"
  Caption (BELOW Card A, max-width 130px, word-wrap, centered):
    "서명 기록 · 사용자 · 감사 로그 (append-only)"

  Card B: [Redis red logo]             name: "Redis 7"
  Caption (BELOW Card B, max-width 130px, word-wrap, centered):
    "Pub/Sub 알림 채널 · SseEmitter 브로드캐스트"

━━━ GROUP 5 — PQC CRYPTO ━━━━━━━━━━━━━━━━━━━━━━

Position: below-center of Backend group
Group color : #9b7ee0 (lilac)
Group tint  : #f5f1fd
Group label : "PQC CRYPTO"
Border style: 1.5px dashed #9b7ee0  (dashed to emphasize special layer)
Group width : ~340px

Cards (horizontal, side by side — each card+caption is an independent vertical column):
  Card A: [Bouncy Castle official logo]  name: "Bouncy Castle 1.84"
  Caption (BELOW Card A, max-width 130px, word-wrap, centered):
    "ML-DSA-65 (BC Provider) 키 생성 · 서명 · 검증"
    "PBKDF2 + AES-256-GCM 키 암호화"

  Card B: [Apache feather icon]         name: "PDFBox"
  Caption (BELOW Card B, max-width 130px, word-wrap, centered):
    "서명값 PDF 메타데이터 삽입 · 추출"
    "SHA3-256 해시"

━━━ GROUP 6 — STORAGE ━━━━━━━━━━━━━━━━━━━━━━━━━

Position: below-right of Backend group
Group color : #3d8f3d (green)
Group tint  : #f2faf2
Group label : "STORAGE"
Group width : ~260px

Cards arranged in a vertical column inside the group:

  [Card A] logo: MinIO elephant or cube  name: "MinIO"
  Caption (DIRECTLY BELOW Card A name, centered, max-width 220px):
    "로컬 환경 · Docker 컨테이너"

  Horizontal divider: 1px solid #dddddd, full width
  Divider label (centered on line): "로컬 ↔ 프로덕션"  10px Inter, #aaaaaa

  [Card B] logo: S3 green bucket  name: "Amazon S3"
  Caption (DIRECTLY BELOW Card B name, centered, max-width 220px):
    "프로덕션 환경 · VPC Endpoint"

━━━ GROUP 7 — MONITORING ━━━━━━━━━━━━━━━━━━━━━━

Position: right side of canvas — vertical strip to the right of the 3 bottom groups
Group color : #e56717 (Grafana orange)
Group tint  : #fff8f4
Group label : "MONITORING"
Border style: 1.5px dashed #e56717  (dashed to emphasize observability layer)
Group width : ~200px

Cards (vertical stack):
  Card A: [Promtail icon]  name: "Promtail"
  Caption (BELOW card name, centered, max-width 170px):
    "Logback 로그 수집 에이전트"

  Card B: [Grafana Loki icon]  name: "Grafana Loki"
  Caption (BELOW card name, centered, max-width 170px):
    "로그 집계 · LogQL 쿼리"

  Card C: [Grafana orange G icon]  name: "Grafana"
  Caption (BELOW card name, centered, max-width 170px):
    "서명 건수 · API 응답 시간"
    "이상 접근 탐지 · 알림"

Arrow from Backend group → MONITORING group:
  Style: 2px solid #e56717, filled arrowhead, direction right
  Label: "Logback / Promtail"  11px JetBrains Mono, #e56717

━━━ ARROWS — BACKEND → BOTTOM GROUPS ━━━━━━━━━━

All downward from Backend group bottom edge:
  → Data Layer  : 2px solid #c47a1e,  label "JDBC / Redis Pub·Sub"
  → PQC Crypto  : 2px dashed #9b7ee0, label "JCA (BC Provider)"
  → Storage     : 2px solid #3d8f3d,  label "presigned URL / 스트림"
  → Monitoring  : 2px solid #e56717,  label "Logback / Promtail"  (direction: right)

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
  Feature pill    : 10px JetBrains Mono, group color

━━━ CONSTRAINTS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

- Total elements on canvas: ~13 cards, ~9 arrows — Monitoring group adds 3 cards (Promtail / Loki / Grafana)
- Backend group is the visual anchor (tallest element)
- 48px white margin on all 4 edges
- 32px gap between all group boxes
- No gradients, no drop shadows, flat design
- All brand logos must be recognizable official icons (not generic shapes)
- Korean labels on arrows and captions exactly as written above
- All captions: BELOW their card name in a vertical stack — never beside or to the right of the icon
- Text overflow prevention: captions word-wrap within their column; no text extends beyond group box boundary
