<template>
  <div class="qs-page">
    <AppTopbar />

    <main class="qs-main">
      <div class="qs-page-head">
        <div>
          <h1 class="qs-page-title">받은 문서</h1>
          <p class="qs-page-sub">{{ isLoading ? '불러오는 중...' : `${filteredItems.length}개의 요청` }}</p>
        </div>
      </div>

      <div class="qs-list-card">
        <div class="qs-list-toolbar">
          <div class="qs-tabs">
            <button
              v-for="tab in TABS" :key="tab.key"
              :class="['qs-tab', { 'is-active': activeTab === tab.key }]"
              @click="activeTab = tab.key; currentPage = 1"
            >
              {{ tab.label }}
              <span class="qs-tab-count">{{ tabCount(tab.key) }}</span>
            </button>
          </div>
          <div class="qs-toolbar-right">
            <select v-model="sortKey" class="qs-select" @change="currentPage = 1">
              <option value="date-desc">최신순</option>
              <option value="date-asc">오래된순</option>
            </select>
            <label class="qs-search">
              <span class="qs-search-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
                  <path d="m21 21-4.35-4.35" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </span>
              <input v-model="search" type="search" placeholder="파일명 또는 요청자 검색..." aria-label="문서 검색">
            </label>
          </div>
        </div>

        <!-- Loading skeleton -->
        <template v-if="isLoading">
          <div class="qs-list">
            <div v-for="i in 4" :key="i" class="qs-doc qs-doc-skel">
              <div class="qs-doc-icon"><div class="qs-skel qs-skel-icon"></div></div>
              <div class="qs-doc-main">
                <div class="qs-skel qs-skel-line" style="width:55%;margin-bottom:8px"></div>
                <div class="qs-skel qs-skel-line-sm" style="width:35%"></div>
              </div>
              <div class="qs-doc-status"><div class="qs-skel qs-skel-pill"></div></div>
              <div class="qs-doc-actions">
                <div class="qs-skel qs-skel-btn"></div>
              </div>
            </div>
          </div>
        </template>

        <!-- Empty state -->
        <template v-else-if="items.length === 0">
          <div class="qs-empty">
            <div class="qs-empty-illu">
              <svg width="44" height="44" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M20 12V22H4V12" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M22 7H2v5h20V7z" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 22V7" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round"/>
                <path d="M12 7H7.5a2.5 2.5 0 010-5C11 2 12 7 12 7z" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 7h4.5a2.5 2.5 0 000-5C13 2 12 7 12 7z" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <h3 class="qs-empty-title">받은 서명 요청이 없어요</h3>
            <p class="qs-empty-desc">다른 사람이 서명을 요청하면<br>여기에서 확인할 수 있어요.</p>
          </div>
        </template>

        <!-- No search results -->
        <template v-else-if="filteredItems.length === 0">
          <div class="qs-noresults">
            <p>"{{ search }}"에 대한 검색 결과가 없습니다.</p>
            <button class="qs-link-quiet" @click="search = ''">검색 초기화</button>
          </div>
        </template>

        <!-- List -->
        <template v-else>
          <div class="qs-list">
            <div
              v-for="item in pagedItems"
              :key="item.requestId"
              class="qs-doc"
              style="cursor:pointer"
              @click="goToDetail(item)"
            >
              <div class="qs-doc-icon" style="position:relative">
                <svg width="28" height="36" viewBox="0 0 28 36" fill="none" aria-hidden="true">
                  <rect width="28" height="36" rx="4" fill="var(--color-error-bg)"/>
                  <path d="M6 11h16M6 16h10M6 21h12" stroke="var(--color-error)"
                    stroke-width="1.5" stroke-linecap="round" opacity="0.7"/>
                  <text x="5" y="30" font-size="6" font-weight="700" fill="var(--color-error)"
                    font-family="monospace">PDF</text>
                </svg>
                <span v-if="item.bundleDocCount > 1"
                  style="position:absolute;top:-4px;right:-6px;background:var(--color-primary-500);color:#fff;border-radius:99px;font-size:10px;font-weight:700;padding:1px 5px;line-height:1.4">
                  {{ item.bundleDocCount }}
                </span>
              </div>
              <div class="qs-doc-main">
                <div class="qs-doc-name">{{ displayName(item) }}</div>
                <div class="qs-received-meta">
                  <span class="qs-received-requester">{{ item.requesterEmail }}</span>
                  <span class="qs-dot-sep">·</span>
                  <span>{{ formatDate(item.createdAt) }}</span>
                  <span v-if="item.status === 'PENDING'" class="qs-dot-sep">·</span>
                  <span v-if="item.status === 'PENDING'" style="color:var(--color-error-text)">{{ formatExpiry(item.expiresAt) }}</span>
                </div>
              </div>
              <div class="qs-doc-status">
                <span :class="['qs-badge', statusBadgeClass(item.status)]">
                  <span class="qs-badge-dot" :style="{ background: statusDotColor(item.status) }"></span>
                  {{ statusLabel(item.status) }}
                </span>
              </div>
              <div class="qs-doc-actions" @click.stop>
                <button v-if="item.status === 'PENDING'"
                  class="qs-btn qs-btn-sm qs-btn-primary"
                  @click="goToSign(item)"
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
        </template>

        <div v-if="!isLoading && filteredItems.length > 0" class="qs-list-foot">
          <span class="qs-foot-info">
            {{ (currentPage - 1) * PAGE_SIZE + 1 }}–{{ Math.min(currentPage * PAGE_SIZE, filteredItems.length) }} / 총 {{ filteredItems.length }}개
          </span>
          <div class="qs-pagination">
            <button class="qs-btn qs-btn-ghost qs-btn-sm" :disabled="currentPage === 1" @click="currentPage--">이전</button>
            <button
              v-for="p in totalPages" :key="p"
              :class="['qs-btn', 'qs-btn-sm', p === currentPage ? 'qs-btn-primary' : 'qs-btn-ghost']"
              @click="currentPage = p"
            >{{ p }}</button>
            <button class="qs-btn qs-btn-ghost qs-btn-sm" :disabled="currentPage === totalPages" @click="currentPage++">다음</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/lib/api'
import AppTopbar from '@/components/layout/AppTopbar.vue'
import '@/assets/received.css'

interface ReceivedDocumentItem {
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

const PAGE_SIZE = 10
const TABS = [
  { key: 'ALL',      label: '전체' },
  { key: 'PENDING',  label: '서명 대기' },
  { key: 'SIGNED',   label: '서명 완료' },
  { key: 'INACTIVE', label: '취소·만료' },
] as const
type TabKey = typeof TABS[number]['key']

const router = useRouter()
const search = ref('')
const isLoading = ref(true)
const currentPage = ref(1)
const activeTab = ref<TabKey>('ALL')
const sortKey = ref<'date-desc' | 'date-asc'>('date-desc')
const items = ref<ReceivedDocumentItem[]>([])

onMounted(async () => {
  try {
    const res = await api.get<{ data: { received: ReceivedDocumentItem[] } }>('/api/signature-requests/received')
    items.value = res.data.data.received ?? []
  } catch {
    // useToast 없이 단순 처리
  } finally {
    isLoading.value = false
  }
})

function displayName(item: ReceivedDocumentItem) {
  return item.isBundle && item.bundleDocCount > 1
    ? `${item.documentName} 외 ${item.bundleDocCount - 1}건`
    : item.documentName
}

function tabCount(key: TabKey) {
  if (key === 'ALL') return items.value.length
  if (key === 'INACTIVE') return items.value.filter(i => i.status === 'CANCELLED' || i.status === 'EXPIRED').length
  return items.value.filter(i => i.status === key).length
}

const filteredItems = computed(() => {
  let list = items.value
  if (activeTab.value === 'INACTIVE') {
    list = list.filter(i => i.status === 'CANCELLED' || i.status === 'EXPIRED')
  } else if (activeTab.value !== 'ALL') {
    list = list.filter(i => i.status === activeTab.value)
  }

  const q = search.value.trim().toLowerCase()
  if (q) {
    list = list.filter(i =>
      i.documentName.toLowerCase().includes(q) ||
      i.requesterEmail.toLowerCase().includes(q)
    )
  }

  return [...list].sort((a, b) =>
    sortKey.value === 'date-asc'
      ? a.createdAt.localeCompare(b.createdAt)
      : b.createdAt.localeCompare(a.createdAt)
  )
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredItems.value.length / PAGE_SIZE)))
const pagedItems = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return filteredItems.value.slice(start, start + PAGE_SIZE)
})

watch([search, sortKey], () => { currentPage.value = 1 })

function goToSign(item: ReceivedDocumentItem) {
  router.push('/sign/' + (item.bundleToken ?? item.token))
}

function goToDetail(item: ReceivedDocumentItem) {
  router.push('/received/' + (item.bundleToken ?? item.token))
}

async function handleDownload(item: ReceivedDocumentItem) {
  if (item.isBundle) {
    goToDetail(item)
    return
  }
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

function formatDate(d: string) {
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

function statusLabel(s: string) {
  switch (s) {
    case 'PENDING':   return '서명 대기'
    case 'SIGNED':    return '서명 완료'
    case 'CANCELLED': return '취소됨'
    case 'EXPIRED':   return '만료됨'
    default:          return s
  }
}

function statusBadgeClass(s: string) {
  switch (s) {
    case 'PENDING':   return 'qs-badge-pending'
    case 'SIGNED':    return 'qs-badge-success'
    case 'CANCELLED': return 'qs-badge-error'
    case 'EXPIRED':   return 'qs-badge-pending'
    default:          return 'qs-badge-pending'
  }
}

function statusDotColor(s: string) {
  switch (s) {
    case 'PENDING':   return 'var(--color-warning)'
    case 'SIGNED':    return 'var(--badge-success-text)'
    case 'CANCELLED': return 'var(--badge-error-text)'
    case 'EXPIRED':   return 'var(--color-gray-400)'
    default:          return 'var(--color-gray-400)'
  }
}
</script>
