<template>
  <div class="qs-page">
    <AppTopbar />

    <main class="qs-main">
      <div class="qs-page-head">
        <div>
          <h1 class="qs-page-title">문서 현황</h1>
          <p class="qs-page-sub">내가 보낸 요청과 받은 요청을 한눈에</p>
        </div>
        <div class="qs-page-actions">
          <RouterLink to="/request" class="qs-btn qs-btn-primary qs-btn-md">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
            </svg>
            PDF 업로드
          </RouterLink>
        </div>
      </div>

      <!-- 보낸 서명 요청 섹션 -->
      <div class="udb-section">
        <div class="udb-section-head">
          <div class="udb-section-title-group">
            <span class="udb-section-icon udb-icon-sent">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M22 2L11 13M22 2L15 22l-4-9-9-4 20-7z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
            <h2 class="udb-section-title">보낸 서명 요청</h2>
            <span v-if="!sentLoading" class="udb-section-count">{{ sentItems.length }}건</span>
          </div>
          <RouterLink to="/documents/sent" class="udb-view-all">
            전체 보기
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M9 18l6-6-6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </RouterLink>
        </div>

        <div class="qs-list-card udb-card">
          <!-- Loading -->
          <template v-if="sentLoading">
            <div class="qs-list">
              <div v-for="i in 3" :key="i" class="qs-doc qs-doc-skel">
                <div class="qs-doc-icon"><div class="qs-skel qs-skel-icon"></div></div>
                <div class="qs-doc-main">
                  <div class="qs-skel qs-skel-line" style="width:55%;margin-bottom:8px"></div>
                  <div class="qs-skel qs-skel-line-sm" style="width:30%"></div>
                </div>
                <div class="qs-doc-status"><div class="qs-skel qs-skel-pill"></div></div>
                <div class="qs-doc-actions"><div class="qs-skel qs-skel-btn"></div></div>
              </div>
            </div>
          </template>

          <!-- Empty -->
          <template v-else-if="sentItems.length === 0">
            <div class="udb-empty">
              <svg width="36" height="36" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <rect x="3" y="3" width="18" height="18" rx="3" stroke="var(--text-tertiary)" stroke-width="1.5"/>
                <path d="M8 12h8M8 8h5" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              <p>아직 보낸 서명 요청이 없어요</p>
              <RouterLink to="/request" class="qs-btn qs-btn-sm qs-btn-primary">PDF 업로드하기</RouterLink>
            </div>
          </template>

          <!-- List -->
          <template v-else>
            <div class="qs-list">
              <div
                v-for="doc in sentPreview"
                :key="doc.id"
                class="qs-doc"
                style="cursor:pointer"
                @click="router.push(doc.detailPath)"
              >
                <div class="qs-doc-icon" style="position:relative">
                  <svg width="28" height="36" viewBox="0 0 28 36" fill="none" aria-hidden="true">
                    <rect width="28" height="36" rx="4" fill="var(--color-error-bg)"/>
                    <path d="M6 11h16M6 16h10M6 21h12" stroke="var(--color-error)" stroke-width="1.5" stroke-linecap="round" opacity="0.7"/>
                    <text x="5" y="30" font-size="6" font-weight="700" fill="var(--color-error)" font-family="monospace">PDF</text>
                  </svg>
                  <span v-if="doc.bundleDocCount > 1"
                    style="position:absolute;top:-4px;right:-6px;background:var(--color-primary-500);color:#fff;border-radius:99px;font-size:10px;font-weight:700;padding:1px 5px;line-height:1.4">
                    {{ doc.bundleDocCount }}
                  </span>
                </div>
                <div class="qs-doc-main">
                  <div class="qs-doc-name">{{ doc.displayName }}</div>
                  <div class="udb-doc-meta">{{ formatDate(doc.createdAt) }}</div>
                </div>
                <div class="qs-doc-status">
                  <span :class="['qs-badge', sentBadgeClass(doc.signatureStatus)]">
                    <span class="qs-badge-dot" :style="{ background: sentDotColor(doc.signatureStatus) }"></span>
                    {{ sentStatusLabel(doc.signatureStatus) }}
                  </span>
                </div>
                <div class="qs-doc-actions">
                  <button class="qs-btn qs-btn-sm qs-btn-primary" @click.stop="router.push(doc.detailPath)">
                    상세보기
                  </button>
                </div>
              </div>
            </div>
            <div v-if="sentItems.length > PREVIEW_SIZE" class="udb-more-row">
              <RouterLink to="/documents/sent" class="qs-btn qs-btn-ghost qs-btn-sm">
                {{ sentItems.length - PREVIEW_SIZE }}건 더 보기
              </RouterLink>
            </div>
          </template>
        </div>
      </div>

      <hr class="udb-divider" />

      <!-- 받은 서명 요청 섹션 -->
      <div class="udb-section">
        <div class="udb-section-head">
          <div class="udb-section-title-group">
            <span class="udb-section-icon udb-icon-received">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M20 12V22H4V12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M22 7H2v5h20V7z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 22V7" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M12 7H7.5a2.5 2.5 0 010-5C11 2 12 7 12 7z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 7h4.5a2.5 2.5 0 000-5C13 2 12 7 12 7z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
            <h2 class="udb-section-title">받은 서명 요청</h2>
            <span v-if="!receivedLoading" class="udb-section-count">{{ receivedItems.length }}건</span>
            <span v-if="pendingCount > 0" class="udb-pending-badge">{{ pendingCount }}개 대기 중</span>
          </div>
          <RouterLink to="/received" class="udb-view-all">
            전체 보기
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M9 18l6-6-6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </RouterLink>
        </div>

        <div class="qs-list-card udb-card">
          <!-- Loading -->
          <template v-if="receivedLoading">
            <div class="qs-list">
              <div v-for="i in 3" :key="i" class="qs-doc qs-doc-skel">
                <div class="qs-doc-icon"><div class="qs-skel qs-skel-icon"></div></div>
                <div class="qs-doc-main">
                  <div class="qs-skel qs-skel-line" style="width:55%;margin-bottom:8px"></div>
                  <div class="qs-skel qs-skel-line-sm" style="width:40%"></div>
                </div>
                <div class="qs-doc-status"><div class="qs-skel qs-skel-pill"></div></div>
                <div class="qs-doc-actions">
                  <div class="qs-skel qs-skel-btn"></div>
                  <div class="qs-skel qs-skel-btn"></div>
                </div>
              </div>
            </div>
          </template>

          <!-- Empty -->
          <template v-else-if="receivedItems.length === 0">
            <div class="udb-empty">
              <svg width="36" height="36" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M20 12V22H4V12" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M22 7H2v5h20V7z" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 22V7" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              <p>받은 서명 요청이 없어요</p>
            </div>
          </template>

          <!-- List -->
          <template v-else>
            <div class="qs-list">
              <div
                v-for="item in receivedPreview"
                :key="item.requestId"
                class="qs-doc"
                style="cursor:pointer"
                @click="goToDetail(item)"
              >
                <div class="qs-doc-icon" style="position:relative">
                  <svg width="28" height="36" viewBox="0 0 28 36" fill="none" aria-hidden="true">
                    <rect width="28" height="36" rx="4" fill="var(--color-error-bg)"/>
                    <path d="M6 11h16M6 16h10M6 21h12" stroke="var(--color-error)" stroke-width="1.5" stroke-linecap="round" opacity="0.7"/>
                    <text x="5" y="30" font-size="6" font-weight="700" fill="var(--color-error)" font-family="monospace">PDF</text>
                  </svg>
                  <span v-if="item.bundleDocCount > 1"
                    style="position:absolute;top:-4px;right:-6px;background:var(--color-primary-500);color:#fff;border-radius:99px;font-size:10px;font-weight:700;padding:1px 5px;line-height:1.4">
                    {{ item.bundleDocCount }}
                  </span>
                </div>
                <div class="qs-doc-main">
                  <div class="qs-doc-name">{{ receivedDisplayName(item) }}</div>
                  <div class="udb-doc-meta">
                    <span>{{ item.requesterEmail }}</span>
                    <span class="qs-dot-sep">·</span>
                    <span>{{ formatDate(item.createdAt) }}</span>
                    <span v-if="item.status === 'PENDING'" class="qs-dot-sep">·</span>
                    <span v-if="item.status === 'PENDING'" style="color:var(--color-error-text)">{{ formatExpiry(item.expiresAt) }}</span>
                  </div>
                </div>
                <div class="qs-doc-status">
                  <span :class="['qs-badge', receivedBadgeClass(item.status)]">
                    <span class="qs-badge-dot" :style="{ background: receivedDotColor(item.status) }"></span>
                    {{ receivedStatusLabel(item.status) }}
                  </span>
                </div>
                <div class="qs-doc-actions" @click.stop>
                  <button v-if="item.status === 'PENDING'"
                    class="qs-btn qs-btn-sm qs-btn-primary"
                    @click="router.push('/sign/' + (item.bundleToken ?? item.token))"
                  >
                    서명하기
                  </button>
                  <button v-if="item.status === 'SIGNED'"
                    class="qs-btn qs-btn-sm qs-btn-secondary"
                    @click="handleDownload(item)"
                  >
                    다운로드
                  </button>
                  <button class="qs-btn qs-btn-sm qs-btn-ghost" @click="goToDetail(item)">
                    상세보기
                  </button>
                </div>
              </div>
            </div>
            <div v-if="receivedItems.length > PREVIEW_SIZE" class="udb-more-row">
              <RouterLink to="/received" class="qs-btn qs-btn-ghost qs-btn-sm">
                {{ receivedItems.length - PREVIEW_SIZE }}건 더 보기
              </RouterLink>
            </div>
          </template>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/lib/api'
import AppTopbar from '@/components/layout/AppTopbar.vue'

interface Doc {
  id: number
  originalFilename: string
  hashSha3256: string
  createdAt: string | null
  signatureStatus: 'NONE' | 'PENDING' | 'SIGNED'
  bundleId: number | null
  bundlePrimary: boolean
  bundleDocCount: number
}

interface DisplayDoc extends Doc {
  displayName: string
  detailPath: string
}

interface ReceivedItem {
  requestId: number
  documentName: string
  requesterEmail: string
  status: 'PENDING' | 'SIGNED' | 'CANCELLED' | 'EXPIRED'
  token: string
  bundleToken: string | null
  bundleDocCount: number
  expiresAt: string
  createdAt: string
  message: string | null
  isBundle: boolean
}

const PREVIEW_SIZE = 5

const router = useRouter()
const sentLoading = ref(true)
const receivedLoading = ref(true)
const rawDocs = ref<Doc[]>([])
const receivedItems = ref<ReceivedItem[]>([])

onMounted(async () => {
  const [sent, received] = await Promise.all([
    api.get<{ data: Doc[] }>('/api/documents').then(r => r.data.data ?? []).catch(() => [] as Doc[]),
    api.get<{ data: { received: ReceivedItem[] } }>('/api/signature-requests/received').then(r => r.data.data.received ?? []).catch(() => [] as ReceivedItem[]),
  ])
  rawDocs.value = sent
  receivedItems.value = received
  sentLoading.value = false
  receivedLoading.value = false
})

const sentItems = computed<DisplayDoc[]>(() =>
  rawDocs.value
    .filter(d => !d.bundleId || d.bundlePrimary)
    .map(d => ({
      ...d,
      displayName: d.bundleId && d.bundleDocCount > 1
        ? `${d.originalFilename} 외 ${d.bundleDocCount - 1}건`
        : d.originalFilename,
      detailPath: d.bundleId ? `/bundles/${d.bundleId}` : `/documents/${d.id}`,
    }))
)

const sentPreview = computed(() => sentItems.value.slice(0, PREVIEW_SIZE))
const receivedPreview = computed(() => receivedItems.value.slice(0, PREVIEW_SIZE))
const pendingCount = computed(() => receivedItems.value.filter(i => i.status === 'PENDING').length)

function receivedDisplayName(item: ReceivedItem) {
  return item.isBundle && item.bundleDocCount > 1
    ? `${item.documentName} 외 ${item.bundleDocCount - 1}건`
    : item.documentName
}

function goToDetail(item: ReceivedItem) {
  router.push('/received/' + (item.bundleToken ?? item.token))
}

async function handleDownload(item: ReceivedItem) {
  if (item.isBundle) { goToDetail(item); return }
  try {
    const res = await api.get(`/api/signature-requests/${item.token}/signed-document`, { responseType: 'blob' })
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = item.documentName.replace(/\.pdf$/i, '_qusigned.pdf')
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    alert('다운로드에 실패했어요.')
  }
}

function formatDate(d: string | null) {
  if (!d) return '-'
  return d.slice(0, 10).replace(/-/g, '.')
}

function formatExpiry(d: string) {
  if (!d) return ''
  const diff = Math.ceil((new Date(d).getTime() - Date.now()) / 86400000)
  if (diff <= 0) return '만료됨'
  if (diff === 1) return '오늘 만료'
  return `${diff}일 후 만료`
}

function sentStatusLabel(s: string) {
  switch (s) {
    case 'PENDING': return '서명 대기'
    case 'SIGNED':  return '서명 완료'
    default:        return '서명 미요청'
  }
}

function sentBadgeClass(s: string) {
  switch (s) {
    case 'PENDING': return 'qs-badge-pending'
    case 'SIGNED':  return 'qs-badge-success'
    default:        return 'qs-badge-pending'
  }
}

function sentDotColor(s: string) {
  switch (s) {
    case 'PENDING': return 'var(--color-warning)'
    case 'SIGNED':  return 'var(--badge-success-text)'
    default:        return 'var(--color-gray-400)'
  }
}

function receivedStatusLabel(s: string) {
  switch (s) {
    case 'PENDING':   return '서명 대기'
    case 'SIGNED':    return '서명 완료'
    case 'CANCELLED': return '취소됨'
    case 'EXPIRED':   return '만료됨'
    default:          return s
  }
}

function receivedBadgeClass(s: string) {
  switch (s) {
    case 'PENDING':   return 'qs-badge-pending'
    case 'SIGNED':    return 'qs-badge-success'
    case 'CANCELLED': return 'qs-badge-error'
    case 'EXPIRED':   return 'qs-badge-pending'
    default:          return 'qs-badge-pending'
  }
}

function receivedDotColor(s: string) {
  switch (s) {
    case 'PENDING':   return 'var(--color-warning)'
    case 'SIGNED':    return 'var(--badge-success-text)'
    case 'CANCELLED': return 'var(--badge-error-text)'
    case 'EXPIRED':   return 'var(--color-gray-400)'
    default:          return 'var(--color-gray-400)'
  }
}
</script>

<style scoped>
.udb-section {
  margin-bottom: 2rem;
}

.udb-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

.udb-section-title-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.udb-section-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  flex-shrink: 0;
}

.udb-icon-sent {
  background: color-mix(in srgb, var(--color-primary-500) 12%, transparent);
  color: var(--color-primary-500);
}

.udb-icon-received {
  background: color-mix(in srgb, var(--color-success, #22c55e) 12%, transparent);
  color: var(--badge-success-text, #16a34a);
}

.udb-section-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.udb-section-count {
  font-size: 0.8125rem;
  color: var(--text-tertiary);
  font-weight: 500;
}

.udb-pending-badge {
  font-size: 0.75rem;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 99px;
  background: color-mix(in srgb, var(--color-warning) 15%, transparent);
  color: var(--color-warning-text, #b45309);
}

.udb-view-all {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 0.8125rem;
  color: var(--color-primary-500);
  text-decoration: none;
  font-weight: 500;
}

.udb-view-all:hover {
  text-decoration: underline;
}

.udb-card {
  padding: 0;
}

.udb-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 2.5rem 1rem;
  color: var(--text-tertiary);
  font-size: 0.875rem;
}

.udb-doc-meta {
  font-size: 0.8125rem;
  color: var(--text-tertiary);
  margin-top: 2px;
}

.udb-more-row {
  display: flex;
  justify-content: center;
  padding: 0.75rem;
  border-top: 1px solid var(--border-subtle, var(--border-color));
}

.udb-divider {
  border: none;
  border-top: 1px solid var(--border-subtle, var(--border-color));
  margin: 0.5rem 0 1.5rem;
}
</style>
