<template>
  <div class="qs-page qs-page-detail">
    <AppTopbar />

    <main class="qs-main">
      <div class="qs-page-head">
        <h1 class="qs-page-title">서명 요청 상세</h1>
        <span v-if="detail" class="qs-page-sub">#bundle_{{ detail.bundleId }}</span>
      </div>

      <!-- Loading skeleton -->
      <template v-if="isLoading">
        <section class="qs-card qs-doc-card">
          <div class="qs-skel" style="width:56px;height:68px;border-radius:8px" />
          <div class="qs-doc-main">
            <div class="qs-skel" style="width:70%;height:18px;margin-bottom:10px" />
            <div class="qs-skel" style="width:40%;height:12px" />
          </div>
        </section>
      </template>

      <!-- Error -->
      <template v-else-if="errorCode !== null">
        <section class="qs-card">
          <div class="qs-error">
            <div class="qs-error-code">ERROR · {{ errorCode }}</div>
            <h2 class="qs-error-title">{{ errorCode === 404 ? '요청을 찾을 수 없습니다' : '불러오기에 실패했습니다' }}</h2>
            <RouterLink class="qs-btn qs-btn-primary qs-btn-md" to="/documents">내 문서 목록으로</RouterLink>
          </div>
        </section>
      </template>

      <!-- Loaded -->
      <template v-else-if="detail">
        <!-- Document bundle card -->
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
            <h2 class="qs-detail-doc-name" :title="bundleDisplayName">{{ bundleDisplayName }}</h2>
            <div class="qs-doc-uploaded">{{ formatDate(detail.requestedAt) }}에 요청됨</div>
            <!-- 번들 내 파일 목록 펼치기 -->
            <div class="qs-bundle-files">
              <button class="qs-link-quiet" @click="filesOpen = !filesOpen">
                {{ filesOpen ? '파일 목록 접기' : `${detail.docCount}개 파일 목록 보기` }}
              </button>
              <div v-if="filesOpen" class="qs-bundle-file-list">
                <div v-for="doc in detail.documents" :key="doc.index" class="qs-bundle-file-item">
                  <svg width="14" height="18" viewBox="0 0 28 36" fill="none" aria-hidden="true">
                    <rect width="28" height="36" rx="4" fill="var(--color-error-bg)"/>
                    <text x="4" y="22" font-size="7" font-weight="800" fill="var(--color-error)"
                      font-family="monospace">PDF</text>
                  </svg>
                  <span>{{ doc.filename }}</span>
                </div>
              </div>
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
              v-for="signer in detail.signers"
              :key="signer.email"
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
                    {{ formatDate(signer.signedAt) }}
                  </span>
                  <span v-else class="qs-signer-time is-empty">—</span>
                </div>
              </div>
              <div class="qs-signer-actions">
                <!-- 서명 완료: 각 파일별 다운로드 -->
                <template v-if="signer.status === 'SIGNED'">
                  <div v-if="signer.signedDocuments.length === 1">
                    <button
                      class="qs-btn qs-btn-secondary qs-btn-sm"
                      @click="downloadBundleDoc(signer, 0)"
                    >
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <path d="M12 4v12M6 10l6 6 6-6" stroke="currentColor"
                          stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M4 20h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                      </svg>
                      서명된 PDF
                    </button>
                  </div>
                  <div v-else class="qs-bundle-dl-group">
                    <button
                      v-for="doc in signer.signedDocuments"
                      :key="doc.index"
                      class="qs-btn qs-btn-secondary qs-btn-sm"
                      :title="doc.filename"
                      @click="downloadBundleDoc(signer, doc.index)"
                    >
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <path d="M12 4v12M6 10l6 6 6-6" stroke="currentColor"
                          stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M4 20h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                      </svg>
                      {{ truncateName(doc.filename) }}
                    </button>
                  </div>
                </template>

                <!-- 대기: 링크 복사 + 취소 -->
                <template v-else-if="signer.status === 'PENDING'">
                  <button
                    v-if="signer.bundleToken"
                    :class="['qs-btn', 'qs-btn-ghost', 'qs-btn-sm',
                      { 'is-success': copiedToken === signer.bundleToken }]"
                    @click="copyLink(signer)"
                  >
                    <template v-if="copiedToken === signer.bundleToken">
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
                  <button
                    class="qs-btn qs-btn-danger-ghost qs-btn-sm"
                    @click="openCancelModal(signer)"
                  >
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/>
                      <path d="M5.6 5.6l12.8 12.8" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                    </svg>
                    요청 취소
                  </button>
                </template>

                <span v-else-if="signer.status === 'EXPIRED'" class="qs-signer-disabled">서명 불가</span>
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

    <!-- Cancel confirm modal -->
    <Teleport to="body">
      <div
        v-if="showCancelModal && cancelTarget"
        class="qs-modal-backdrop"
        role="dialog"
        aria-modal="true"
        @click.self="closeCancelModal"
      >
        <div class="qs-modal">
          <div class="qs-modal-icon-warn" aria-hidden="true">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
              <path d="M12 3l10 18H2L12 3z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
              <path d="M12 10v5M12 18v.5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h3 class="qs-modal-title">서명 요청을 취소하시겠어요?</h3>
          <p class="qs-modal-desc">
            <strong>{{ cancelTarget.email }}</strong>에게 보낸 번들 서명 요청이 취소됩니다.<br />
            취소 후에는 되돌릴 수 없으며, 서명 링크가 즉시 만료됩니다.
          </p>
          <div class="qs-modal-actions">
            <button type="button" class="qs-btn qs-btn-secondary qs-btn-md"
              :disabled="cancelSubmitting" @click="closeCancelModal">닫기</button>
            <button type="button" class="qs-btn qs-btn-danger qs-btn-md"
              :disabled="cancelSubmitting" @click="confirmCancel">
              {{ cancelSubmitting ? '취소 처리 중…' : '취소하기' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Toast -->
    <Teleport to="body">
      <div :class="['qs-toast-wrap', { 'is-show': toastVisible }]" aria-live="polite">
        <div v-if="toastVisible" class="qs-toast">
          <span class="qs-toast-dot" aria-hidden="true" />
          <span>{{ toastText }}</span>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/lib/api'
import AppTopbar from '@/components/layout/AppTopbar.vue'

interface BundleDocumentInfo { index: number; filename: string; hashSha3256: string }
interface BundleSignedDoc { index: number; filename: string }

interface BundleSignerDetail {
  email: string
  status: 'SIGNED' | 'PENDING' | 'CANCELLED' | 'EXPIRED'
  signedAt: string | null
  bundleToken: string | null
  signedDocuments: BundleSignedDoc[]
}

interface BundleDetail {
  bundleId: number
  primaryDocName: string
  docCount: number
  documents: BundleDocumentInfo[]
  requesterEmail: string
  algorithm: string
  requestedAt: string
  expiresAt: string
  signers: BundleSignerDetail[]
}

const SIGNER_BADGE = {
  SIGNED:    { cls: 'qs-badge-success', dot: 'var(--color-success)', label: '서명 완료' },
  PENDING:   { cls: 'qs-badge-warning', dot: 'var(--color-warning)', label: '서명 대기' },
  EXPIRED:   { cls: 'qs-badge-pending', dot: 'var(--text-tertiary)',  label: '만료' },
  CANCELLED: { cls: 'qs-badge-pending', dot: 'var(--text-tertiary)',  label: '취소됨' },
} as const

const OVERALL_BADGE = {
  completed: { cls: 'qs-badge-success', dot: 'var(--color-success)', label: '서명 완료' },
  progress:  { cls: 'qs-badge-warning', dot: 'var(--color-warning)', label: '진행 중' },
  expired:   { cls: 'qs-badge-pending', dot: 'var(--text-tertiary)', label: '만료됨' },
} as const

const route = useRoute()
const isLoading = ref(true)
const errorCode = ref<number | null>(null)
const detail = ref<BundleDetail | null>(null)
const filesOpen = ref(false)
const copiedToken = ref<string | null>(null)
const showCancelModal = ref(false)
const cancelTarget = ref<BundleSignerDetail | null>(null)
const cancelSubmitting = ref(false)
const toastVisible = ref(false)
const toastText = ref('')
let toastTimer: ReturnType<typeof setTimeout> | null = null

onMounted(async () => {
  try {
    const res = await api.get<{ data: BundleDetail }>(`/api/bundles/${route.params.id}`)
    detail.value = res.data.data
  } catch (err: any) {
    errorCode.value = err?.response?.status ?? 500
  } finally {
    isLoading.value = false
  }
})

const bundleDisplayName = computed(() => {
  if (!detail.value) return ''
  if (detail.value.docCount <= 1) return detail.value.primaryDocName
  return `${detail.value.primaryDocName} 외 ${detail.value.docCount - 1}건`
})

const signedCount = computed(() =>
  detail.value?.signers.filter(s => s.status === 'SIGNED').length ?? 0
)

const overallStatus = computed(() => {
  if (!detail.value) return 'progress'
  const { signers } = detail.value
  if (signers.every(s => s.status === 'SIGNED')) return 'completed'
  if (signers.some(s => s.status === 'EXPIRED' || s.status === 'CANCELLED')) return 'expired'
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

function signerBadge(status: BundleSignerDetail['status']) {
  return SIGNER_BADGE[status] ?? SIGNER_BADGE.EXPIRED
}

function avatarClass(status: string) {
  return { SIGNED: 'qs-avatar-signed', PENDING: 'qs-avatar-pending', EXPIRED: 'qs-avatar-expired', CANCELLED: 'qs-avatar-expired' }[status]
}

function avatarInitials(email: string) {
  const local = email.split('@')[0] ?? ''
  const parts = local.split(/[._-]/)
  return ((parts[0]?.[0] ?? '') + (parts[1]?.[0] ?? parts[0]?.[1] ?? '')).toUpperCase()
}

function formatDate(d: string | null | undefined) {
  if (!d) return '—'
  const dt = new Date(d)
  if (isNaN(dt.getTime())) return d
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}.${pad(dt.getMonth() + 1)}.${pad(dt.getDate())} ${pad(dt.getHours())}:${pad(dt.getMinutes())}`
}

function truncateName(name: string, max = 20) {
  if (name.length <= max) return name
  const ext = name.lastIndexOf('.') > 0 ? name.slice(name.lastIndexOf('.')) : ''
  return name.slice(0, max - 3 - ext.length) + '…' + ext
}

function copyLink(signer: BundleSignerDetail) {
  const token = signer.bundleToken ?? ''
  const url = `${window.location.origin}/sign/${token}`
  try { navigator.clipboard.writeText(url) } catch {}
  copiedToken.value = token
  setTimeout(() => { copiedToken.value = null }, 1800)
}

async function downloadBundleDoc(signer: BundleSignerDetail, docIndex: number) {
  if (!detail.value) return
  try {
    const res = await api.get(
      `/api/bundles/${detail.value.bundleId}/signers/${encodeURIComponent(signer.email)}/signed-documents/${docIndex}`,
      { responseType: 'blob' }
    )
    const doc = detail.value.documents[docIndex]
    const filename = (doc?.filename ?? 'document.pdf').replace(/\.pdf$/i, '') + '_qusigned.pdf'
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url; a.download = filename; a.click()
    URL.revokeObjectURL(url)
  } catch {
    alert('다운로드에 실패했어요.')
  }
}

function openCancelModal(signer: BundleSignerDetail) {
  cancelTarget.value = signer
  showCancelModal.value = true
}

function closeCancelModal() {
  if (cancelSubmitting.value) return
  showCancelModal.value = false
  cancelTarget.value = null
}

async function confirmCancel() {
  if (!detail.value || !cancelTarget.value) return
  cancelSubmitting.value = true
  try {
    await api.post(`/api/bundles/${detail.value.bundleId}/signers/${encodeURIComponent(cancelTarget.value.email)}/cancel`)
    const target = cancelTarget.value.email
    const signer = detail.value.signers.find(s => s.email === target)
    if (signer) signer.status = 'CANCELLED'
    showToastMsg(`${target}에게 보낸 요청이 취소되었어요`)
    closeCancelModal()
  } catch {
    closeCancelModal()
  } finally {
    cancelSubmitting.value = false
  }
}

function showToastMsg(text: string) {
  toastText.value = text
  toastVisible.value = true
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { toastVisible.value = false }, 2800)
}

function onEscKey(e: KeyboardEvent) {
  if (e.key === 'Escape' && showCancelModal.value && !cancelSubmitting.value) closeCancelModal()
}
watch(showCancelModal, open => {
  if (open) document.addEventListener('keydown', onEscKey)
  else document.removeEventListener('keydown', onEscKey)
})
onBeforeUnmount(() => {
  document.removeEventListener('keydown', onEscKey)
  if (toastTimer) clearTimeout(toastTimer)
})
</script>

<style scoped>
.qs-bundle-files { margin-top: 8px }
.qs-bundle-file-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 8px;
  padding: 8px 12px;
  background: var(--surface-elevated);
  border-radius: 8px;
  border: 1px solid var(--border-subtle);
}
.qs-bundle-file-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}
.qs-bundle-dl-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
</style>
