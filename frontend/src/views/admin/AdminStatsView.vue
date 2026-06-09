<template>
  <div class="qs-admin-page">
    <h1 class="qs-admin-page-title">대시보드</h1>

    <div v-if="statsLoading" class="qs-admin-stats-grid">
      <div v-for="i in 5" :key="i" class="qs-admin-stat-card">
        <div class="qs-skel" style="width:60%;height:13px;margin-bottom:10px" />
        <div class="qs-skel" style="width:40%;height:28px" />
      </div>
    </div>

    <div v-else-if="stats" class="qs-admin-stats-grid">
      <div class="qs-admin-stat-card">
        <div class="qs-admin-stat-label">전체 사용자</div>
        <div class="qs-admin-stat-value">{{ stats.totalUsers.toLocaleString() }}</div>
      </div>
      <div class="qs-admin-stat-card">
        <div class="qs-admin-stat-label">전체 서명</div>
        <div class="qs-admin-stat-value">{{ stats.totalSignatures.toLocaleString() }}</div>
      </div>
      <div class="qs-admin-stat-card">
        <div class="qs-admin-stat-label">대기 중</div>
        <div class="qs-admin-stat-value qs-admin-stat-warning">{{ stats.pendingSignatures.toLocaleString() }}</div>
      </div>
      <div class="qs-admin-stat-card">
        <div class="qs-admin-stat-label">완료</div>
        <div class="qs-admin-stat-value qs-admin-stat-success">{{ stats.completedSignatures.toLocaleString() }}</div>
      </div>
      <div class="qs-admin-stat-card">
        <div class="qs-admin-stat-label">취소</div>
        <div class="qs-admin-stat-value qs-admin-stat-muted">{{ stats.cancelledSignatures.toLocaleString() }}</div>
      </div>
    </div>

    <section class="qs-admin-section">
      <h2 class="qs-admin-section-title">최근 감사 이벤트</h2>
      <div v-if="auditLoading" class="qs-admin-audit-skeleton">
        <div v-for="i in 5" :key="i" class="qs-skel" style="height:40px;border-radius:6px;margin-bottom:8px" />
      </div>
      <div v-else-if="recentAudit.length === 0" class="qs-admin-empty">기록된 이벤트가 없습니다.</div>
      <table v-else class="qs-admin-table">
        <thead>
          <tr>
            <th>이벤트</th>
            <th>사용자</th>
            <th>IP</th>
            <th>일시</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in recentAudit" :key="log.id">
            <td><span :class="['qs-admin-event-badge', eventBadgeClass(log.eventType)]">{{ eventLabel(log.eventType) }}</span></td>
            <td class="qs-admin-td-email">{{ log.actorEmail }}</td>
            <td class="qs-admin-td-mono">{{ log.ipAddress }}</td>
            <td class="qs-admin-td-time">{{ formatDate(log.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/lib/api'

interface AdminStats {
  totalUsers: number
  totalSignatures: number
  pendingSignatures: number
  completedSignatures: number
  cancelledSignatures: number
}

interface AuditLog {
  id: number
  eventType: string
  actorEmail: string
  ipAddress: string
  createdAt: string
}

const stats = ref<AdminStats | null>(null)
const statsLoading = ref(true)
const recentAudit = ref<AuditLog[]>([])
const auditLoading = ref(true)

onMounted(async () => {
  const [statsRes, auditRes] = await Promise.allSettled([
    api.get<{ data: AdminStats }>('/api/admin/stats'),
    api.get<{ data: { content: AuditLog[] } }>('/api/admin/audit?size=10'),
  ])
  if (statsRes.status === 'fulfilled') stats.value = statsRes.value.data.data
  if (auditRes.status === 'fulfilled') recentAudit.value = auditRes.value.data.data.content
  statsLoading.value = false
  auditLoading.value = false
})

const EVENT_LABELS: Record<string, string> = {
  SIGN_REQUEST_CREATED:       '서명 요청 생성',
  BUNDLE_REQUEST_CREATED:     '번들 요청 생성',
  SIGNED:                     '서명 완료',
  BUNDLE_SIGNED:              '번들 서명 완료',
  SIGNER_CANCELLED:           '서명 취소',
  BUNDLE_SIGNER_CANCELLED:    '번들 취소',
  SIGNED_DOCUMENT_DOWNLOADED: '문서 다운로드',
  TIMESTAMP_OBTAINED:         '타임스탬프 획득',
}

const EVENT_BADGE_CLASSES: Record<string, string> = {
  SIGNED:       'is-success',
  BUNDLE_SIGNED:'is-success',
  SIGNER_CANCELLED:        'is-warning',
  BUNDLE_SIGNER_CANCELLED: 'is-warning',
}

function eventLabel(type: string) { return EVENT_LABELS[type] ?? type }
function eventBadgeClass(type: string) { return EVENT_BADGE_CLASSES[type] ?? '' }

function formatDate(d: string) {
  const dt = new Date(d)
  if (isNaN(dt.getTime())) return d
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}.${pad(dt.getMonth()+1)}.${pad(dt.getDate())} ${pad(dt.getHours())}:${pad(dt.getMinutes())}`
}
</script>
