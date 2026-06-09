<template>
  <div class="qs-admin-page">
    <h1 class="qs-admin-page-title">감사 로그</h1>

    <div class="qs-admin-toolbar qs-admin-toolbar-wrap">
      <select v-model="filterType" class="qs-admin-select">
        <option value="">전체 이벤트</option>
        <option v-for="t in EVENT_TYPES" :key="t.value" :value="t.value">{{ t.label }}</option>
      </select>
      <input v-model="startDate" type="date" class="qs-admin-input-date" />
      <span class="qs-admin-date-sep">~</span>
      <input v-model="endDate" type="date" class="qs-admin-input-date" />
      <button class="qs-admin-btn" @click="fetchLogs(0)">조회</button>
      <button class="qs-admin-btn-ghost" @click="handleExport">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M12 4v12M6 10l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M4 20h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        JSON 내보내기
      </button>
    </div>

    <div v-if="loading" class="qs-admin-table-wrap">
      <div v-for="i in 10" :key="i" class="qs-skel" style="height:40px;border-radius:6px;margin-bottom:6px" />
    </div>

    <div v-else-if="logs.length === 0" class="qs-admin-empty">조건에 맞는 감사 로그가 없습니다.</div>

    <div v-else class="qs-admin-table-wrap">
      <table class="qs-admin-table">
        <thead>
          <tr>
            <th>이벤트</th>
            <th>사용자</th>
            <th>IP</th>
            <th>일시</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in logs" :key="log.id">
            <td><span :class="['qs-admin-event-badge', eventBadgeClass(log.eventType)]">{{ eventLabel(log.eventType) }}</span></td>
            <td class="qs-admin-td-email">{{ log.actorEmail }}</td>
            <td class="qs-admin-td-mono">{{ log.ipAddress }}</td>
            <td class="qs-admin-td-time">{{ formatDate(log.createdAt) }}</td>
          </tr>
        </tbody>
      </table>

      <div class="qs-admin-pagination">
        <button class="qs-admin-btn-ghost" :disabled="page === 0" @click="fetchLogs(page - 1)">이전</button>
        <span class="qs-admin-page-info">{{ page + 1 }} / {{ totalPages }} (총 {{ totalElements.toLocaleString() }}건)</span>
        <button class="qs-admin-btn-ghost" :disabled="page >= totalPages - 1" @click="fetchLogs(page + 1)">다음</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/lib/api'

interface AuditLog {
  id: number
  eventType: string
  actorEmail: string
  ipAddress: string
  createdAt: string
}

const logs = ref<AuditLog[]>([])
const loading = ref(true)
const page = ref(0)
const totalPages = ref(1)
const totalElements = ref(0)
const filterType = ref('')
const startDate = ref('')
const endDate = ref('')

const EVENT_TYPES = [
  { value: 'SIGN_REQUEST_CREATED',       label: '서명 요청 생성' },
  { value: 'BUNDLE_REQUEST_CREATED',     label: '번들 요청 생성' },
  { value: 'SIGNED',                     label: '서명 완료' },
  { value: 'BUNDLE_SIGNED',              label: '번들 서명 완료' },
  { value: 'SIGNER_CANCELLED',           label: '서명 취소' },
  { value: 'BUNDLE_SIGNER_CANCELLED',    label: '번들 취소' },
  { value: 'SIGNED_DOCUMENT_DOWNLOADED', label: '문서 다운로드' },
  { value: 'TIMESTAMP_OBTAINED',         label: '타임스탬프 획득' },
]

const EVENT_LABELS: Record<string, string> = Object.fromEntries(EVENT_TYPES.map((t) => [t.value, t.label]))

const EVENT_BADGE_CLASSES: Record<string, string> = {
  SIGNED: 'is-success',
  BUNDLE_SIGNED: 'is-success',
  SIGNER_CANCELLED: 'is-warning',
  BUNDLE_SIGNER_CANCELLED: 'is-warning',
}

async function fetchLogs(p: number) {
  loading.value = true
  try {
    const params = new URLSearchParams({ page: String(p), size: '50' })
    if (filterType.value) params.set('eventType', filterType.value)
    if (startDate.value) params.set('startDate', `${startDate.value}T00:00:00`)
    if (endDate.value) params.set('endDate', `${endDate.value}T23:59:59`)
    const res = await api.get<{ data: { content: AuditLog[]; totalPages: number; totalElements: number } }>(
      `/api/admin/audit?${params}`
    )
    logs.value = res.data.data.content
    totalPages.value = res.data.data.totalPages || 1
    totalElements.value = res.data.data.totalElements || 0
    page.value = p
  } finally {
    loading.value = false
  }
}

async function handleExport() {
  const res = await api.get<{ data: AuditLog[] }>('/api/admin/audit/export')
  const blob = new Blob([JSON.stringify(res.data.data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `audit-export-${new Date().toISOString().slice(0, 10)}.json`
  a.click()
  URL.revokeObjectURL(url)
}

function eventLabel(type: string) { return EVENT_LABELS[type] ?? type }
function eventBadgeClass(type: string) { return EVENT_BADGE_CLASSES[type] ?? '' }

function formatDate(d: string) {
  const dt = new Date(d)
  if (isNaN(dt.getTime())) return d
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}.${pad(dt.getMonth()+1)}.${pad(dt.getDate())} ${pad(dt.getHours())}:${pad(dt.getMinutes())}:${pad(dt.getSeconds())}`
}

onMounted(() => fetchLogs(0))
</script>
