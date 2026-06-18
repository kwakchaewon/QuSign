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

Caption: "리버스 프록시 · SSL 종료 · SPA 서빙"  11px Inter, #666666
Caption line 2: "/ → /dist  ·  /api → :8080  ·  /api/sse → buffering off"

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
  Caption: "JWT 인증 · 서명 플로우 · 감사 로그 · 관리자 API"

Feature sub-tags (pill chips below caption, same group tint, darker border):
  [ JWT ]  [ ML-DSA ]  [ AuditLog ]  [ SSE Emitter ]  [ @Scheduled ]
  Font: 10px JetBrains Mono, color: #6fa8dc

Arrow ↓ downward to DATA group:
  Style : 2px solid #aaaaaa, label "JDBC / Redis"
Arrow ↓ downward to PQC group:
  Style : 2px dashed #9b7ee0, label "JNI"
Arrow ↓ downward to STORAGE group:
  Style : 2px solid #3d8f3d, label "SDK"

━━━ GROUP 4 — DATA LAYER ━━━━━━━━━━━━━━━━━━━━━━

Position: below-left of Backend group
Group color : #c47a1e (amber)
Group tint  : #fffaf2
Group label : "DATA LAYER"
Group width : ~340px

Cards (horizontal, side by side):
  Card A: [MariaDB gold dolphin logo]  name: "MariaDB 10.11"
  Caption: "서명 기록 · 사용자 · 감사 로그 (append-only)"

  Card B: [Redis red logo]             name: "Redis 7"
  Caption: "Pub/Sub 알림 채널 · SseEmitter 브로드캐스트"

━━━ GROUP 5 — PQC CRYPTO ━━━━━━━━━━━━━━━━━━━━━━

Position: below-center of Backend group
Group color : #9b7ee0 (lilac)
Group tint  : #f5f1fd
Group label : "PQC CRYPTO"
Border style: 1.5px dashed #9b7ee0  (dashed to emphasize special layer)
Group width : ~340px

Cards (horizontal, side by side):
  Card A: [shield / lock icon]          name: "liboqs-java"
  Caption: "ML-DSA-65 키 생성 · 서명 · 검증 · PBKDF2+AES-256-GCM 키 암호화"

  Card B: [Apache feather icon]         name: "PDFBox"
  Caption: "서명값 PDF 메타데이터 삽입 · 추출 · SHA3-256 해시"

━━━ GROUP 6 — STORAGE ━━━━━━━━━━━━━━━━━━━━━━━━━

Position: below-right of Backend group
Group color : #3d8f3d (green)
Group tint  : #f2faf2
Group label : "STORAGE"
Group width : ~260px

Cards (vertical stack):
  Card A: [MinIO elephant logo or cube] name: "MinIO"
  Caption: "로컬 환경 · Docker 컨테이너"

  Card B: [S3 green bucket icon]        name: "Amazon S3"
  Caption: "프로덕션 환경 · VPC Endpoint"

Horizontal divider between Card A and B:
  "로컬 ↔ 프로덕션" label, 10px Inter, #aaaaaa

━━━ ARROWS — BACKEND → BOTTOM GROUPS ━━━━━━━━━━

All downward from Backend group bottom edge:
  → Data Layer  : 2px solid #c47a1e,  label "JDBC / Redis Pub·Sub"
  → PQC Crypto  : 2px dashed #9b7ee0, label "JNI (liboqs native)"
  → Storage     : 2px solid #3d8f3d,  label "presigned URL / 스트림"

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

- Total elements on canvas: ~10 cards, ~7 arrows — keep it this count, no more
- Backend group is the visual anchor (tallest element)
- 48px white margin on all 4 edges
- 32px gap between all group boxes
- No gradients, no drop shadows, flat design
- All brand logos must be recognizable official icons (not generic shapes)
- Korean labels on arrows and captions exactly as written above
