<template>
  <div class="qs-page">
    <AppTopbar />

    <main class="qs-main">
      <button class="qs-back-link" @click="router.push('/received')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        받은 문서 목록
      </button>

      <!-- Loading -->
      <template v-if="isLoading">
        <div class="qs-detail-card">
          <div class="qs-detail-head">
            <div class="qs-skel qs-skel-icon" style="width:48px;height:48px;border-radius:14px;flex-shrink:0"></div>
            <div class="qs-detail-head-info">
              <div class="qs-skel qs-skel-line" style="width:60%;margin-bottom:12px"></div>
              <div class="qs-skel qs-skel-pill"></div>
            </div>
          </div>
          <div class="qs-detail-body">
            <div v-for="i in 4" :key="i" class="qs-skel qs-skel-line" :style="{ width: (40 + i * 10) + '%' }"></div>
          </div>
        </div>
      </template>

      <!-- Error -->
      <template v-else-if="fetchError">
        <div class="qs-empty">
          <h3 class="qs-empty-title">불러올 수 없어요</h3>
          <p class="qs-empty-desc">{{ fetchError }}</p>
          <button class="qs-btn qs-btn-primary qs-btn-md" @click="router.push('/received')">목록으로 돌아가기</button>
        </div>
      </template>

      <!-- Detail -->
      <template v-else-if="info">
        <div class="qs-detail-card">
          <div class="qs-detail-head">
            <div :class="['qs-status-icon', statusIconClass]">
              <!-- PENDING: clock -->
              <svg v-if="effectiveStatus === 'PENDING'" width="24" height="24" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
                <path d="M12 7v5l3 3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <!-- SIGNED: check -->
              <svg v-else-if="effectiveStatus === 'SIGNED'" width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <!-- CANCELLED: x -->
              <svg v-else-if="effectiveStatus === 'CANCELLED'" width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <!-- EXPIRED: hourglass -->
              <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M5 2h14M5 22h14M17 2v4l-5 6 5 6v4M7 2v4l5 6-5 6v4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="qs-detail-head-info">
              <h2 class="qs-detail-title">{{ docTitle }}</h2>
              <span :class="['qs-badge', statusBadgeClass]">
                <span class="qs-badge-dot" :style="{ background: statusDotColor }"></span>
                {{ statusLabel }}
              </span>
            </div>
          </div>

          <div class="qs-detail-body">
            <div class="qs-detail-row">
              <span class="qs-detail-label">요청자</span>
              <span class="qs-detail-value" style="font-family:var(--font-mono)">{{ info.requesterEmail }}</span>
            </div>
            <div class="qs-detail-row">
              <span class="qs-detail-label">요청일</span>
              <span class="qs-detail-value">{{ formatDateTime(info.requestedAt) }}</span>
            </div>
            <div class="qs-detail-row">
              <span class="qs-detail-label">만료일</span>
              <span class="qs-detail-value" :style="effectiveStatus === 'PENDING' ? 'color:var(--color-error)' : ''">
                {{ formatDateTime(info.expiresAt) }}
              </span>
            </div>
            <!-- 포함 문서 목록 (미서명 번들) -->
            <div v-if="info.isBundle && effectiveStatus !== 'SIGNED'" class="qs-detail-row">
              <span class="qs-detail-label">포함 문서</span>
              <span class="qs-detail-value">
                <span v-for="doc in bundleDocs" :key="doc.index" style="display:block;margin-bottom:2px">
                  {{ doc.index + 1 }}. {{ doc.filename }}
                </span>
              </span>
            </div>

            <div class="qs-detail-row">
              <span class="qs-detail-label">문서 해시</span>
              <span class="qs-detail-value" style="font-family:var(--font-mono);font-size:11px;word-break:break-all;color:var(--text-tertiary)">
                {{ info.hashSha3256 }}
              </span>
            </div>
            <div v-if="info.message" class="qs-detail-message">
              "{{ info.message }}"
            </div>

            <!-- 번들 SIGNED: 파일별 다운로드 행 -->
            <div v-if="effectiveStatus === 'SIGNED' && info.isBundle" class="qs-dl-section">
              <div class="qs-dl-header">서명된 문서</div>
              <div class="qs-dl-list">
                <div v-for="doc in bundleDocs" :key="doc.index" class="qs-dl-row">
                  <svg width="20" height="26" viewBox="0 0 20 26" fill="none" aria-hidden="true" class="qs-dl-file-icon">
                    <rect width="20" height="26" rx="3" fill="var(--color-error-bg)"/>
                    <text x="3.5" y="20" font-size="5" font-weight="700" fill="var(--color-error)" font-family="monospace">PDF</text>
                  </svg>
                  <span class="qs-dl-name">{{ doc.filename }}</span>
                  <button class="qs-btn qs-btn-secondary qs-btn-sm" @click="handleBundleDownload(doc.index, doc.filename)">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <path d="M12 4v12M6 10l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M4 20h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    다운로드
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="qs-detail-actions">
            <button v-if="effectiveStatus === 'PENDING'"
              class="qs-btn qs-btn-primary qs-btn-md"
              @click="goToSign"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M12 20h9M16.5 3.5a2.121 2.121 0 013 3L7 19l-4 1 1-4L16.5 3.5z"
                  stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              서명하기
            </button>

            <!-- 단건 SIGNED: 하단 다운로드 버튼 -->
            <button v-if="effectiveStatus === 'SIGNED' && !info.isBundle"
              class="qs-btn qs-btn-primary qs-btn-md"
              @click="handleDownload"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3"
                  stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              서명된 문서 다운로드
            </button>

            <button class="qs-btn qs-btn-secondary qs-btn-md" @click="router.push('/received')">
              목록으로
            </button>
          </div>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/lib/api'
import AppTopbar from '@/components/layout/AppTopbar.vue'
import '@/assets/received.css'

interface BundleDocumentInfoDto {
  index: number
  filename: string
  hashSha3256: string
}

interface SignerRequestInfoResponse {
  documentName: string
  requesterEmail: string
  message: string | null
  requestedAt: string
  expiresAt: string
  hashSha3256: string
  cancelled: boolean
  signed: boolean
  documents: BundleDocumentInfoDto[]
  isBundle: boolean
}

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const token = route.params.token as string

const isLoading = ref(true)
const fetchError = ref('')
const info = ref<SignerRequestInfoResponse | null>(null)

onMounted(async () => {
  try {
    const res = await api.get<{ data: SignerRequestInfoResponse }>(`/api/signature-requests/${token}/info`)
    info.value = res.data.data
  } catch (e: any) {
    fetchError.value = e?.response?.status === 403
      ? '접근 권한이 없어요.'
      : '요청 정보를 불러오지 못했어요.'
  } finally {
    isLoading.value = false
  }
})

const effectiveStatus = computed(() => {
  if (!info.value) return 'PENDING'
  if (info.value.cancelled) return 'CANCELLED'
  if (info.value.signed) return 'SIGNED'
  if (new Date(info.value.expiresAt) < new Date()) return 'EXPIRED'
  return 'PENDING'
})

const docTitle = computed(() => {
  if (!info.value) return ''
  if (info.value.isBundle && info.value.documents.length > 1) {
    return `${info.value.documentName} 외 ${info.value.documents.length - 1}건`
  }
  return info.value.documentName
})

const statusLabel = computed(() => {
  switch (effectiveStatus.value) {
    case 'PENDING':   return '서명 대기'
    case 'SIGNED':    return '서명 완료'
    case 'CANCELLED': return '취소됨'
    case 'EXPIRED':   return '만료됨'
  }
})

const statusBadgeClass = computed(() => {
  switch (effectiveStatus.value) {
    case 'PENDING':   return 'qs-badge-pending'
    case 'SIGNED':    return 'qs-badge-success'
    case 'CANCELLED': return 'qs-badge-error'
    case 'EXPIRED':   return 'qs-badge-pending'
  }
})

const statusDotColor = computed(() => {
  switch (effectiveStatus.value) {
    case 'PENDING':   return 'var(--color-warning)'
    case 'SIGNED':    return 'var(--badge-success-text)'
    case 'CANCELLED': return 'var(--badge-error-text)'
    case 'EXPIRED':   return 'var(--color-gray-400)'
  }
})

const statusIconClass = computed(() => {
  switch (effectiveStatus.value) {
    case 'PENDING':   return 'qs-status-icon qs-status-icon-pending'
    case 'SIGNED':    return 'qs-status-icon qs-status-icon-signed'
    case 'CANCELLED': return 'qs-status-icon qs-status-icon-cancel'
    case 'EXPIRED':   return 'qs-status-icon qs-status-icon-expired'
  }
})

const bundleDocs = computed(() => info.value?.documents ?? [])

function goToSign() {
  router.push('/sign/' + token)
}

async function handleBundleDownload(index: number, filename: string) {
  try {
    const res = await api.get(`/api/signature-requests/${token}/signed-bundle-documents/${index}`, { responseType: 'blob' })
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = filename.replace(/\.pdf$/i, '_qusigned.pdf')
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    alert('다운로드에 실패했어요.')
  }
}

async function handleDownload() {
  try {
    const res = await api.get(`/api/signature-requests/${token}/signed-document`, { responseType: 'blob' })
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = docTitle.value.replace('.pdf', '_qusigned.pdf')
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    alert('다운로드에 실패했어요.')
  }
}

function formatDateTime(d: string) {
  if (!d) return '-'
  return d.slice(0, 16).replace('T', ' ')
}
</script>
