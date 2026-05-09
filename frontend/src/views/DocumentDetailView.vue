<template>
  <div class="qs-page qs-page-detail">
    <AppTopbar />

    <main class="qs-main">
      <div class="qs-page-head">
        <h1 class="qs-page-title">서명 요청 상세</h1>
        <span v-if="detail" class="qs-page-sub">#req_{{ detail.id }}</span>
      </div>

      <!-- Loading skeleton -->
      <template v-if="isLoading">
        <section class="qs-card qs-doc-card">
          <div class="qs-skel" style="width:56px;height:68px;border-radius:8px" />
          <div class="qs-doc-main">
            <div class="qs-skel" style="width:70%;height:18px;margin-bottom:10px" />
            <div class="qs-skel" style="width:40%;height:12px;margin-bottom:14px" />
            <div class="qs-skel" style="width:220px;height:28px;border-radius:9px" />
          </div>
          <div class="qs-skel" style="width:88px;height:28px;border-radius:9999px" />
        </section>
        <div class="qs-section-head">
          <h3 class="qs-section-title">서명자 현황</h3>
        </div>
        <section class="qs-card">
          <div class="qs-signers">
            <div v-for="i in 3" :key="i" class="qs-signer-row">
              <div class="qs-skel" style="width:40px;height:40px;border-radius:50%" />
              <div class="qs-signer-main">
                <div class="qs-skel" style="width:60%;height:13px;margin-bottom:8px" />
                <div class="qs-skel" style="width:36%;height:12px" />
              </div>
              <div class="qs-skel" style="width:110px;height:32px;border-radius:9px" />
            </div>
          </div>
        </section>
        <div class="qs-section-head">
          <h3 class="qs-section-title">요청 정보</h3>
        </div>
        <section class="qs-card">
          <div class="qs-meta-grid">
            <div v-for="i in 4" :key="i" class="qs-meta-cell">
              <div class="qs-skel" style="width:60px;height:11px" />
              <div class="qs-skel" style="width:70%;height:14px;margin-top:6px" />
            </div>
          </div>
        </section>
      </template>

      <!-- Error 404 -->
      <template v-else-if="errorCode === 404">
        <section class="qs-card">
          <div class="qs-error">
            <div class="qs-error-illu">
              <svg width="44" height="44" viewBox="0 0 64 64" fill="none" aria-hidden="true">
                <circle cx="32" cy="32" r="28" stroke="var(--text-tertiary)" stroke-width="2"
                  fill="var(--surface-elevated)"/>
                <path d="M22 22l20 20M42 22L22 42" stroke="var(--text-tertiary)"
                  stroke-width="2.2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="qs-error-code">ERROR · 404</div>
            <h2 class="qs-error-title">요청을 찾을 수 없습니다</h2>
            <p class="qs-error-desc">
              삭제되었거나 존재하지 않는 서명 요청이에요.<br>URL을 다시 확인해 주세요.
            </p>
            <RouterLink class="qs-btn qs-btn-primary qs-btn-md" to="/documents">
              내 문서 목록으로
            </RouterLink>
          </div>
        </section>
      </template>

      <!-- Error 403 -->
      <template v-else-if="errorCode === 403">
        <section class="qs-card">
          <div class="qs-error">
            <div class="qs-error-illu">
              <svg width="44" height="44" viewBox="0 0 64 64" fill="none" aria-hidden="true">
                <rect x="16" y="28" width="32" height="22" rx="4"
                  stroke="var(--text-tertiary)" stroke-width="2" fill="var(--surface-elevated)"/>
                <path d="M22 28v-6a10 10 0 0 1 20 0v6"
                  stroke="var(--text-tertiary)" stroke-width="2" fill="none"/>
                <circle cx="32" cy="38" r="2.4" fill="var(--text-tertiary)"/>
                <path d="M32 40v5" stroke="var(--text-tertiary)"
                  stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="qs-error-code">ERROR · 403</div>
            <h2 class="qs-error-title">조회 권한이 없습니다</h2>
            <p class="qs-error-desc">
              이 서명 요청은 다른 사용자가 만든 것이에요.<br>본인 계정의 요청만 조회할 수 있어요.
            </p>
            <RouterLink class="qs-btn qs-btn-primary qs-btn-md" to="/documents">
              내 문서 목록으로
            </RouterLink>
          </div>
        </section>
      </template>

      <!-- Loaded -->
      <template v-else-if="detail">
        <!-- Document card -->
        <section class="qs-card qs-doc-card">
          <div class="qs-doc-icon-lg">
            <svg width="56" height="68" viewBox="0 0 56 68" fill="none" aria-hidden="true">
              <path d="M4 4h32l16 16v44H4z" fill="var(--color-error-bg)"
                stroke="var(--color-error)" stroke-width="1.4" stroke-linejoin="round"/>
              <path d="M36 4v16h16" stroke="var(--color-error)"
                stroke-width="1.4" stroke-linejoin="round" fill="none"/>
              <text x="28" y="50" text-anchor="middle" font-size="14" font-weight="700"
                fill="var(--color-error)" font-family="monospace">PDF</text>
            </svg>
          </div>
          <div class="qs-doc-main">
            <h2 class="qs-detail-doc-name" :title="detail.documentName">{{ detail.documentName }}</h2>
            <div class="qs-doc-uploaded">{{ formatDate(detail.uploadedAt) }}에 업로드</div>
            <div class="qs-hash">
              <span class="qs-hash-label">SHA3-256</span>
              <span class="qs-hash-value">
                {{ hashOpen ? detail.hashSha3256 : detail.hashSha3256.slice(0, 16) + '…' }}
              </span>
              <button class="qs-hash-toggle" @click="hashOpen = !hashOpen">
                {{ hashOpen ? '접기' : '전체 보기' }}
              </button>
            </div>
          </div>
          <div class="qs-doc-status">
            <span :class="['qs-badge', overallBadge.cls]">
              <span class="qs-badge-dot" :style="{ background: overallBadge.dot }" aria-hidden="true" />
              {{ overallBadge.label }}
            </span>
          </div>
        </section>

        <!-- Signers section -->
        <div class="qs-section-head">
          <h3 class="qs-section-title">서명자 현황</h3>
          <span class="qs-section-meta">
            <strong>{{ signedCount }}</strong> / {{ detail.signers.length }}명 서명 완료
          </span>
        </div>
        <section class="qs-card">
          <div class="qs-signers">
            <div
              v-for="(signer, i) in detail.signers"
              :key="i"
              :class="['qs-signer-row', { 'is-expired': signer.status === 'EXPIRED' }]"
            >
              <div :class="['qs-avatar', avatarClass(signer.status)]" aria-hidden="true">
                <template v-if="signer.status === 'SIGNED'">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                    <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor"
                      stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </template>
                <template v-else>{{ avatarInitials(signer.email) }}</template>
              </div>
              <div class="qs-signer-main">
                <div class="qs-signer-email" :title="signer.email">{{ signer.email }}</div>
                <div class="qs-signer-meta">
                  <span :class="['qs-badge', 'qs-badge-sm', signerBadge(signer.status).cls]">
                    <span class="qs-badge-dot"
                      :style="{ background: signerBadge(signer.status).dot }"
                      aria-hidden="true" />
                    {{ signerBadge(signer.status).label }}
                  </span>
                  <span class="qs-dot-sep" aria-hidden="true">·</span>
                  <span v-if="signer.status === 'SIGNED'" class="qs-signer-time">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/>
                      <path d="M12 7v5l3 2" stroke="currentColor"
                        stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    {{ formatDate(signer.signedAt) }}
                  </span>
                  <span v-else class="qs-signer-time is-empty">—</span>
                </div>
              </div>
              <div class="qs-signer-actions">
                <button
                  v-if="signer.status === 'SIGNED'"
                  class="qs-btn qs-btn-secondary qs-btn-sm"
                  @click="downloadSigned(signer)"
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <path d="M12 4v12M6 10l6 6 6-6" stroke="currentColor"
                      stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M4 20h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                  서명된 PDF
                </button>
                <button
                  v-if="signer.status === 'PENDING'"
                  :class="['qs-btn', 'qs-btn-ghost', 'qs-btn-sm',
                    { 'is-success': copiedToken === signer.signatureToken }]"
                  @click="copyLink(signer)"
                >
                  <template v-if="copiedToken === signer.signatureToken">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor"
                        stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    복사됨
                  </template>
                  <template v-else>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <path d="M10 14a4 4 0 0 0 5.66 0l3-3a4 4 0 1 0-5.66-5.66l-1 1"
                        stroke="currentColor" stroke-width="1.7"
                        stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M14 10a4 4 0 0 0-5.66 0l-3 3a4 4 0 1 0 5.66 5.66l1-1"
                        stroke="currentColor" stroke-width="1.7"
                        stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    서명 링크 복사
                  </template>
                </button>
                <span v-if="signer.status === 'EXPIRED'" class="qs-signer-disabled">서명 불가</span>
              </div>
            </div>
          </div>
        </section>

        <!-- Request metadata -->
        <div class="qs-section-head">
          <h3 class="qs-section-title">요청 정보</h3>
        </div>
        <section class="qs-card">
          <div class="qs-meta-grid">
            <div class="qs-meta-cell">
              <span class="qs-meta-k">요청자</span>
              <span class="qs-meta-v qs-mono">{{ detail.requesterEmail }}</span>
            </div>
            <div class="qs-meta-cell">
              <span class="qs-meta-k">알고리즘</span>
              <span class="qs-meta-v qs-mono">{{ detail.algorithm }}</span>
            </div>
            <div class="qs-meta-cell">
              <span class="qs-meta-k">요청 일시</span>
              <span class="qs-meta-v">{{ formatDate(detail.requestedAt) }}</span>
            </div>
            <div class="qs-meta-cell">
              <span class="qs-meta-k">만료 일시</span>
              <template v-if="expiryFlag === 'expired'">
                <span class="qs-meta-v is-expired">{{ formatDate(detail.expiresAt) }}</span>
                <span class="qs-meta-tag is-expired">만료됨</span>
              </template>
              <template v-else-if="expiryFlag === 'soon'">
                <span class="qs-meta-v is-warning">{{ formatDate(detail.expiresAt) }}</span>
                <span class="qs-meta-tag">곧 만료</span>
              </template>
              <template v-else>
                <span class="qs-meta-v">{{ formatDate(detail.expiresAt) }}</span>
              </template>
            </div>
          </div>
        </section>
      </template>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/lib/api'
import AppTopbar from '@/components/layout/AppTopbar.vue'

interface SignerDetail {
  email: string
  status: 'SIGNED' | 'PENDING' | 'EXPIRED'
  signedAt: string | null
  signatureToken: string | null
}

interface SignatureRequestDetail {
  id: number
  documentName: string
  hashSha3256: string
  uploadedAt: string
  requesterEmail: string
  algorithm: string
  requestedAt: string
  expiresAt: string
  signers: SignerDetail[]
}

const SIGNER_BADGE = {
  SIGNED:  { cls: 'qs-badge-success', dot: 'var(--color-success)', label: '서명 완료' },
  PENDING: { cls: 'qs-badge-warning', dot: 'var(--color-warning)', label: '서명 대기' },
  EXPIRED: { cls: 'qs-badge-pending', dot: 'var(--text-tertiary)',  label: '만료' },
} as const

const OVERALL_BADGE = {
  completed: { cls: 'qs-badge-success', dot: 'var(--color-success)', label: '서명 완료' },
  progress:  { cls: 'qs-badge-warning', dot: 'var(--color-warning)', label: '진행 중' },
  expired:   { cls: 'qs-badge-pending', dot: 'var(--text-tertiary)', label: '만료됨' },
} as const

const route = useRoute()
const isLoading = ref(true)
const errorCode = ref<number | null>(null)
const detail = ref<SignatureRequestDetail | null>(null)
const hashOpen = ref(false)
const copiedToken = ref<string | null>(null)

onMounted(async () => {
  try {
    const res = await api.get<{ data: SignatureRequestDetail }>(`/api/signature-requests/${route.params.id}`)
    detail.value = res.data.data
  } catch (err: any) {
    errorCode.value = err?.response?.status ?? 500
  } finally {
    isLoading.value = false
  }
})

const signedCount = computed(() =>
  detail.value?.signers.filter(s => s.status === 'SIGNED').length ?? 0
)

const overallStatus = computed(() => {
  if (!detail.value) return 'progress'
  const { signers } = detail.value
  if (signers.every(s => s.status === 'SIGNED')) return 'completed'
  if (signers.some(s => s.status === 'EXPIRED')) return 'expired'
  return 'progress'
})

const overallBadge = computed(() => OVERALL_BADGE[overallStatus.value])

const expiryFlag = computed(() => {
  if (!detail.value) return 'default'
  const now = Date.now()
  const expires = new Date(detail.value.expiresAt).getTime()
  if (now > expires) return 'expired'
  if (expires - now < 24 * 60 * 60 * 1000) return 'soon'
  return 'default'
})

function signerBadge(status: SignerDetail['status']) {
  return SIGNER_BADGE[status]
}

function avatarClass(status: SignerDetail['status']) {
  return { SIGNED: 'qs-avatar-signed', PENDING: 'qs-avatar-pending', EXPIRED: 'qs-avatar-expired' }[status]
}

function avatarInitials(email: string) {
  const local = email.split('@')[0]
  const parts = local.split(/[._-]/)
  return (parts[0][0] + (parts[1]?.[0] ?? parts[0][1] ?? '')).toUpperCase()
}

function formatDate(d: string | null) {
  if (!d) return '—'
  const dt = new Date(d)
  if (isNaN(dt.getTime())) return d
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}.${pad(dt.getMonth() + 1)}.${pad(dt.getDate())} ${pad(dt.getHours())}:${pad(dt.getMinutes())}`
}

async function downloadSigned(signer: SignerDetail) {
  if (!detail.value) return
  try {
    const res = await api.get(
      `/api/signature-requests/${detail.value.id}/download?signerEmail=${encodeURIComponent(signer.email)}`,
      { responseType: 'blob' }
    )
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = detail.value.documentName.replace(/\.pdf$/i, '') + '_qusigned.pdf'
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    alert('다운로드에 실패했어요.')
  }
}

function copyLink(signer: SignerDetail) {
  const token = signer.signatureToken ?? ''
  const url = `${window.location.origin}/sign/${token}`
  try { navigator.clipboard.writeText(url) } catch {}
  copiedToken.value = token
  setTimeout(() => { copiedToken.value = null }, 1800)
}
</script>
