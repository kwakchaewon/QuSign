<template>
  <div class="qs-audit-timeline">
    <div v-if="isLoading" class="qs-audit-loading">
      <div v-for="i in 3" :key="i" class="qs-audit-skel-row">
        <div class="qs-skel" style="width:32px;height:32px;border-radius:50%;flex-shrink:0" />
        <div style="flex:1">
          <div class="qs-skel" style="width:55%;height:13px;margin-bottom:6px" />
          <div class="qs-skel" style="width:30%;height:11px" />
        </div>
      </div>
    </div>

    <div v-else-if="error" class="qs-audit-empty">감사 로그를 불러오지 못했습니다.</div>

    <div v-else-if="logs.length === 0" class="qs-audit-empty">아직 기록된 이벤트가 없습니다.</div>

    <div v-else class="qs-audit-list">
      <div v-for="(log, idx) in logs" :key="log.id" class="qs-audit-item">
        <div class="qs-audit-line-wrap" aria-hidden="true">
          <div :class="['qs-audit-dot', eventDotClass(log.eventType)]" />
          <div v-if="idx < logs.length - 1" class="qs-audit-connector" />
        </div>
        <div class="qs-audit-body">
          <div class="qs-audit-event">{{ eventLabel(log.eventType) }}</div>
          <div class="qs-audit-meta">
            <span class="qs-audit-actor">{{ log.actorEmail }}</span>
            <span class="qs-audit-sep" aria-hidden="true">·</span>
            <span class="qs-audit-ip">{{ log.ipAddress }}</span>
            <span class="qs-audit-sep" aria-hidden="true">·</span>
            <span class="qs-audit-time">{{ formatDate(log.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="!isLoading && !error && logs.length > 0" class="qs-audit-export">
      <button type="button" class="qs-link-quiet" @click="downloadAudit">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M12 4v12M6 10l6 6 6-6" stroke="currentColor"
            stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M4 20h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        JSON 내보내기 (법적 분쟁 제출용)
      </button>
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
  signatureRequestId: number | null
  bundleId: number | null
  documentId: number | null
}

const props = defineProps<{
  documentId?: number
  bundleId?: number
}>()

const logs = ref<AuditLog[]>([])
const isLoading = ref(true)
const error = ref(false)

async function downloadAudit() {
  const url = props.documentId
    ? `/api/documents/${props.documentId}/audit/export`
    : `/api/bundles/${props.bundleId}/audit/export`
  const filename = props.documentId
    ? `audit_document_${props.documentId}.json`
    : `audit_bundle_${props.bundleId}.json`
  const res = await api.get(url, { responseType: 'blob' })
  const href = URL.createObjectURL(new Blob([res.data], { type: 'application/json' }))
  const a = document.createElement('a')
  a.href = href
  a.download = filename
  a.click()
  URL.revokeObjectURL(href)
}

onMounted(async () => {
  try {
    const url = props.documentId
      ? `/api/documents/${props.documentId}/audit`
      : `/api/bundles/${props.bundleId}/audit`
    const res = await api.get<{ data: AuditLog[] }>(url)
    logs.value = res.data.data
  } catch {
    error.value = true
  } finally {
    isLoading.value = false
  }
})

const EVENT_LABELS: Record<string, string> = {
  SIGN_REQUEST_CREATED:      '서명 요청 생성',
  BUNDLE_REQUEST_CREATED:    '번들 서명 요청 생성',
  SIGNED:                    '서명 완료',
  BUNDLE_SIGNED:             '번들 서명 완료',
  SIGNER_CANCELLED:          '서명 요청 취소',
  BUNDLE_SIGNER_CANCELLED:   '번들 서명 요청 취소',
  SIGNED_DOCUMENT_DOWNLOADED:'서명 문서 다운로드',
}

const EVENT_DOT_CLASSES: Record<string, string> = {
  SIGN_REQUEST_CREATED:      'qs-audit-dot-info',
  BUNDLE_REQUEST_CREATED:    'qs-audit-dot-info',
  SIGNED:                    'qs-audit-dot-success',
  BUNDLE_SIGNED:             'qs-audit-dot-success',
  SIGNER_CANCELLED:          'qs-audit-dot-warning',
  BUNDLE_SIGNER_CANCELLED:   'qs-audit-dot-warning',
  SIGNED_DOCUMENT_DOWNLOADED:'qs-audit-dot-default',
}

function eventLabel(type: string) {
  return EVENT_LABELS[type] ?? type
}

function eventDotClass(type: string) {
  return EVENT_DOT_CLASSES[type] ?? 'qs-audit-dot-default'
}

function formatDate(d: string) {
  const dt = new Date(d)
  if (isNaN(dt.getTime())) return d
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}.${pad(dt.getMonth() + 1)}.${pad(dt.getDate())} ${pad(dt.getHours())}:${pad(dt.getMinutes())}:${pad(dt.getSeconds())}`
}
</script>

<style scoped>
.qs-audit-timeline { display: flex; flex-direction: column; gap: 0 }

.qs-audit-loading { display: flex; flex-direction: column; gap: 16px; padding: 8px 0 }
.qs-audit-skel-row { display: flex; align-items: flex-start; gap: 12px }

.qs-audit-empty {
  color: var(--text-tertiary);
  font-size: 13px;
  padding: 16px 0;
  text-align: center;
}

.qs-audit-list { display: flex; flex-direction: column }

.qs-audit-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.qs-audit-line-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  width: 32px;
}

.qs-audit-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-top: 4px;
  flex-shrink: 0;
  border: 2px solid transparent;
}

.qs-audit-dot-success  { background: var(--color-success);  border-color: var(--color-success-bg) }
.qs-audit-dot-info     { background: var(--color-primary);  border-color: rgba(99,102,241,.15) }
.qs-audit-dot-warning  { background: var(--color-warning);  border-color: var(--color-warning-bg, #fef3c7) }
.qs-audit-dot-default  { background: var(--text-tertiary);  border-color: var(--border-subtle) }

.qs-audit-connector {
  width: 1px;
  flex: 1;
  min-height: 20px;
  background: var(--border-subtle);
  margin: 4px 0;
}

.qs-audit-body {
  padding-bottom: 20px;
  flex: 1;
}

.qs-audit-event {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
}

.qs-audit-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 3px;
  font-size: 12px;
  color: var(--text-tertiary);
  flex-wrap: wrap;
}

.qs-audit-sep { opacity: .5 }

.qs-audit-ip { font-family: monospace; font-size: 11px }

.qs-audit-export {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--border-subtle);
  font-size: 12px;
}

.qs-audit-export button {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: var(--text-tertiary);
  background: none;
  border: none;
  padding: 0;
  font-size: inherit;
  cursor: pointer;
}
.qs-audit-export button:hover { color: var(--text-secondary) }
</style>
