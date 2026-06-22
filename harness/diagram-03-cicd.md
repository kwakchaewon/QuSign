---
title: QuSign — Diagram 03: CI/CD & Automation Pipeline
version: 1.0
usage: claude.ai/design — "Start with" 파일로 첨부
---

Draw a "CI/CD & Automation Pipeline" diagram for QuSign, a PQC electronic signature
web service. Style: clean, light-background, pipeline flow diagram.
One page, no scrolling.

━━━ CANVAS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Size       : 1440 × 810px  (16:9)
Background : #ffffff
Title      : "CI/CD & Automation Pipeline"
             top-left, 36px Inter weight 700, #000000
Subtitle   : "GitHub Actions 자동 배포 · EventBridge 야간 절전 스케줄러 · Loki + Grafana 모니터링"
             below title, 14px Inter 400, #888888
Divider    : 1px solid #e6e6e6, full width below subtitle

━━━ LAYOUT (two horizontal lanes) ━━━━━━━━━━━━━

LANE 1 — CI/CD 파이프라인 (top half):
  Left → Right pipeline:
  [DEVELOPER]  ──git push──→  [GITHUB]  ──trigger──→  [GITHUB ACTIONS]
                                                             ↓
                                              ┌────────────────────────┐
                                              │  Build · Test · Package│
                                              └────────────┬───────────┘
                                                           │
                                              ┌────────────┴───────────┐
                                              ↓                        ↓
                                           [ECR]                    [EC2]
                                       (Docker push)             (SSH deploy)

LANE 2 — 자동화 스케줄러 (bottom half):
  Left → Right:
  [EventBridge Scheduler]  ──invoke──→  [Lambda]  ──start/stop──→  [EC2]
  (KST 09:00 / 21:30)                                    ↓
                                                   [CloudWatch Logs]

Lane separator: 1.5px dashed #e6e6e6, full width, labeled "AUTOMATION" on left

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
  Max-width  : 그룹 inner width; word-wrap 항상 활성화
  Word-wrap  : 항상 활성화 — 긴 텍스트는 다음 줄로 줄바꿈, 절대 경계 밖으로 오버플로우 금지

━━━ LANE 1 — CI/CD PIPELINE ━━━━━━━━━━━━━━━━━━━

Elements left → right:

ELEMENT A — DEVELOPER
  No border box. Single floating card + label above.
  Label: "DEVELOPER"  12px JetBrains Mono, #888888
  Icon: person / laptop icon  (64px)

  Arrow → right:
    Style: 2px solid #24292e (GitHub dark), filled arrowhead
    Label: "git push main"  11px JetBrains Mono, #24292e

ELEMENT B — GITHUB
  No border box.
  Card: [GitHub octocat icon]  name: "GitHub"

  Arrow → right:
    Style: 2px solid #24292e
    Label: "on: push → main"  11px JetBrains Mono, #24292e

GROUP C — GITHUB ACTIONS
  Group color : #2088ff (Actions blue)
  Group tint  : #f0f6ff
  Group label line 1 : [GitHub Actions icon 16px]  "GITHUB ACTIONS"
  Group label line 2 : "deploy-backend ∥ deploy-frontend · path-filter 게이트(병렬 job)"  10px Inter, #888888
  Group width : ~600px  (widest group in Lane 1)

  Inside: vertical pipeline of step cards (NOT component cards — smaller):

  Step card style:
    Background: #ffffff, border: 1px solid #d0d7de, radius: 8px
    Width: ~560px, Height: ~48px
    Left: step number circle (filled #2088ff, white text, 24px)
    Center: step name (13px Inter 600, #000000) + sub (12px Inter, #666666)
    Right: tool badge pill

  Steps (top → bottom):
    ① Build Backend    │ ./gradlew bootJar                          │ [ Gradle / Java 21 ]
    ② Build Frontend   │ cd frontend && npm run build-only          │ [ Node / Vite ]
    ③ Docker Build     │ docker build -t qusign_backend             │ [ Docker ]
    ④ ECR Push         │ docker push ECR:latest                     │ [ ECR ]
    ⑤ SSH Deploy BE    │ SSM 조회 → ECR 로그인 → docker-compose pull/up │ [ SSH ]
    ⑥ SCP Deploy FE    │ frontend/dist → /var/www/qusign/dist       │ [ SCP ]

  Vertical arrows between steps: thin 1px #2088ff dashed, no label

  Group output arrows (from right side of group, split into two):
    Arrow ↓ toward ECR card  : 2px solid #e8760a, label "docker push"
    Arrow ↓ toward EC2 card  : 2px solid #e8760a, label "SSH + SCP"

ELEMENT D — ECR (below/right of Actions group)
  No border box.
  Card: [AWS ECR orange icon]  name: "Amazon ECR"
  Caption: "qusign_backend · 최신 3개 이미지 유지"  11px Inter, #666666

  Arrow → right to EC2:
    Style: 2px dashed #e8760a
    Label: "docker-compose pull (EC2 에서 실행)"  11px JetBrains Mono, #e8760a

ELEMENT E — EC2 (rightmost, shared with Lane 2)
  Large card (shared target for both lanes):
    Background: #fff8f2, border: 2px solid #e8760a, radius: 12px
    Width/Height: 130 × 130px
    Icon: [AWS EC2 orange icon]  (72px)
    Name: "EC2  qusign_app"  14px Inter 600, #e8760a
    Sub: "t3.small · 3.0.193.52"  11px Inter, #888888

━━━ LANE SEPARATOR ━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Horizontal dashed line between Lane 1 and Lane 2:
  Style: 1.5px dashed #e6e6e6
  Left label: "AUTOMATION"  11px JetBrains Mono ALL CAPS, #aaaaaa

━━━ LANE 2 — SCHEDULER AUTOMATION ━━━━━━━━━━━━━

Elements left → right:

GROUP F — EVENTBRIDGE SCHEDULER
  Group color : #cc3333 (red)
  Group tint  : #fff5f5
  Group label : "SCHEDULER"
  Group width : ~320px

  Two schedule cards inside (horizontal):
    Card A (mini, not full component card):
      Background: #fff5f5, border: 1px solid #cc3333, radius: 8px, width: ~140px, height: ~80px
      Icon: clock icon (32px, #cc3333)
      Line 1: "KST 09:00"  13px Inter 700, #cc3333
      Line 2: "EC2 시작"   11px Inter, #666666
      Line 3: "cron(0 9 * * ? *)  Asia/Seoul"  10px JetBrains Mono, #888888

    Card B (mini):
      Background: #fff5f5, border: 1px solid #cc3333, radius: 8px, width: ~140px, height: ~80px
      Icon: moon icon (32px, #cc3333)
      Line 1: "KST 21:30"  13px Inter 700, #cc3333
      Line 2: "EC2 정지"   11px Inter, #666666
      Line 3: "cron(30 21 * * ? *)  Asia/Seoul"  10px JetBrains Mono, #888888

  Arrow → right:
    Style: 2px solid #cc3333, filled arrowhead
    Label: "invoke event"  11px JetBrains Mono, #cc3333

GROUP G — LAMBDA
  Group color : #f0a040 (Lambda orange)
  Group tint  : #fffaf0
  Group label : "LAMBDA"
  Group width : ~320px

  Card: [AWS Lambda orange λ icon]  name: "Lambda"
  Sub (BELOW card name, centered, max-width 280px):
    "qusign_start_instances · Python 3.14"  11px Inter, #666666
  Caption (BELOW Sub, max-width 280px, word-wrap, centered, 10px JetBrains Mono, #888888):
    "start → ec2.start_instances()"
    "stop  → ec2.stop_instances()"

  Arrow → right toward EC2:
    Style: 2px solid #cc3333, filled arrowhead
    Label: "start / stop"  11px JetBrains Mono, #cc3333

  Arrow ↓ downward:
    Style: 1px solid #aaaaaa
    Label: "execution log"

ELEMENT H — CLOUDWATCH LOGS (below Lambda)
  No border box.
  Card: [CloudWatch eye icon]  name: "CloudWatch Logs"
  Caption: "Lambda 실행 이력 확인"  11px Inter, #666666

GROUP I — APP MONITORING (오른쪽, EC2 카드 아래)
  Group color : #e56717 (Grafana orange)
  Group tint  : #fff8f4
  Group label : "MONITORING"
  Group width : ~280px

  Cards (horizontal, side by side — each card+caption is an independent vertical column):
    Card: [Grafana Loki icon]  name: "Loki"
    Caption (BELOW card name, max-width 120px, word-wrap, centered):
      "앱 로그 집계"
      "Promtail 에이전트"

    Card: [Grafana orange G icon]  name: "Grafana"
    Caption (BELOW card name, max-width 120px, word-wrap, centered):
      "대시보드 · 이상 알림"
      "서명 건수 · 응답 시간"

  Arrow from EC2 → Loki:
    Style: 2px solid #e56717, filled arrowhead
    Label: "앱 로그 (Promtail)"  11px JetBrains Mono, #e56717

Lane separator note: CloudWatch = Lambda 실행 로그 (AWS managed) / Loki+Grafana = Spring Boot 앱 로그 (self-hosted)

━━━ COST SAVING BADGE (bottom-right corner) ━━━━

Info box in bottom-right:
  Background: #f2faf2, border: 1px solid #3d8f3d, radius: 12px
  Width: ~280px
  Title: "비용 절감 효과"  11px JetBrains Mono ALL CAPS, #3d8f3d
  Line 1: "야간 정지 (11.5h/day)  →  약 33% 절감"  10px Inter, #444444
  Line 2: "24시간 가동 대비  ~$18/월 → ~$12/월"    10px Inter, #444444

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
  Step name       : 13px Inter 600, #000000
  Step sub        : 12px Inter 400, #666666

━━━ CONSTRAINTS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

- Total elements on canvas: ~10 cards + 6 step rows + 2 schedule mini-cards — Group I adds Loki + Grafana
- GitHub Actions group is the dominant element in Lane 1 (~40% canvas width)
- EC2 card is shared between Lane 1 and Lane 2 (rightmost column, spans both lanes)
- 48px white margin on all 4 edges
- 32px gap between all group boxes
- No gradients, no drop shadows, flat design
- All brand logos must be recognizable official icons (not generic shapes)
- Korean labels on captions and badges exactly as written above
- All captions: BELOW their card name in a vertical stack — never beside or to the right of the icon
- Text overflow prevention: captions word-wrap within their column; no text extends beyond group box boundary
