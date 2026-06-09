<template>
  <div class="qs-page">
    <AppTopbar />

    <main class="qs-main">
      <div class="qs-page-head">
        <div>
          <h1 class="qs-page-title">보낸 서명 요청</h1>
          <p class="qs-page-sub">{{ isLoading ? '불러오는 중...' : `${filteredDocs.length}개의 문서` }}</p>
        </div>
        <div class="qs-page-actions">
          <RouterLink to="/request" class="qs-btn qs-btn-primary qs-btn-md">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5"
                stroke-linecap="round"/>
            </svg>
            PDF 업로드
          </RouterLink>
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
              <option value="name-asc">파일명 A-Z</option>
              <option value="name-desc">파일명 Z-A</option>
            </select>
            <label class="qs-search">
              <span class="qs-search-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
                  <path d="m21 21-4.35-4.35" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </span>
              <input v-model="search" type="search" placeholder="파일명 검색..." aria-label="문서 검색">
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
                <div class="qs-skel qs-skel-btn"></div>
              </div>
            </div>
          </div>
        </template>

        <!-- Empty state -->
        <template v-else-if="docs.length === 0">
          <div class="qs-empty">
            <div class="qs-empty-illu">
              <svg width="44" height="44" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <rect x="3" y="3" width="18" height="18" rx="3" stroke="var(--text-tertiary)"
                  stroke-width="1.5"/>
                <path d="M8 12h8M8 8h5" stroke="var(--text-tertiary)" stroke-width="1.5"
                  stroke-linecap="round"/>
              </svg>
            </div>
            <h3 class="qs-empty-title">아직 문서가 없어요</h3>
            <p class="qs-empty-desc">PDF를 업로드하고<br>ML-DSA 전자서명 요청을 보내보세요.</p>
            <RouterLink to="/request" class="qs-btn qs-btn-primary qs-btn-md">
              PDF 업로드하기
            </RouterLink>
          </div>
        </template>

        <!-- No search results -->
        <template v-else-if="filteredDocs.length === 0">
          <div class="qs-noresults">
            <p>"{{ search }}"에 대한 검색 결과가 없습니다.</p>
            <button class="qs-link-quiet" @click="search = ''">검색 초기화</button>
          </div>
        </template>

        <!-- Doc list -->
        <template v-else>
          <div class="qs-list">
            <div
              v-for="doc in pagedDocs"
              :key="doc.id"
              class="qs-doc"
              style="cursor:pointer"
              @click="router.push(doc.detailPath)"
            >
              <div class="qs-doc-icon" style="position:relative">
                <svg width="28" height="36" viewBox="0 0 28 36" fill="none" aria-hidden="true">
                  <rect width="28" height="36" rx="4" fill="var(--color-error-bg)"/>
                  <path d="M6 11h16M6 16h10M6 21h12" stroke="var(--color-error)"
                    stroke-width="1.5" stroke-linecap="round" opacity="0.7"/>
                  <text x="5" y="30" font-size="6" font-weight="700" fill="var(--color-error)"
                    font-family="monospace">PDF</text>
                </svg>
                <!-- 번들 뱃지 -->
                <span v-if="doc.bundleDocCount > 1"
                  style="position:absolute;top:-4px;right:-6px;background:var(--color-primary-500);color:#fff;border-radius:99px;font-size:10px;font-weight:700;padding:1px 5px;line-height:1.4">
                  {{ doc.bundleDocCount }}
                </span>
              </div>
              <div class="qs-doc-main">
                <div class="qs-doc-name">{{ doc.displayName }}</div>
              </div>
              <div class="qs-doc-status">
                <span class="qs-badge qs-badge-pending">
                  <span class="qs-badge-dot" style="background:var(--color-gray-400)"></span>
                  업로드됨
                </span>
              </div>
              <div class="qs-doc-actions">
                <button class="qs-btn qs-btn-sm qs-btn-primary" @click.stop="router.push(doc.detailPath)">
                  상세보기
                </button>
                <button v-if="!doc.bundleId" class="qs-btn qs-btn-sm qs-btn-secondary" @click.stop="handleDownload(doc)">
                  다운로드
                </button>
              </div>
            </div>
          </div>
        </template>

        <div v-if="!isLoading && filteredDocs.length > 0" class="qs-list-foot">
          <span class="qs-foot-info">
            {{ (currentPage - 1) * PAGE_SIZE + 1 }}–{{ Math.min(currentPage * PAGE_SIZE, filteredDocs.length) }} / 총 {{ filteredDocs.length }}개
          </span>
          <div class="qs-pagination">
            <button
              class="qs-btn qs-btn-ghost qs-btn-sm"
              :disabled="currentPage === 1"
              @click="currentPage--"
            >이전</button>
            <button
              v-for="p in totalPages"
              :key="p"
              :class="['qs-btn', 'qs-btn-sm', p === currentPage ? 'qs-btn-primary' : 'qs-btn-ghost']"
              @click="currentPage = p"
            >{{ p }}</button>
            <button
              class="qs-btn qs-btn-ghost qs-btn-sm"
              :disabled="currentPage === totalPages"
              @click="currentPage++"
            >다음</button>
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

// 화면에 표시할 항목 (번들의 경우 primary 문서만 표시)
interface DisplayItem extends Doc {
  displayName: string   // "파일명.pdf 외 N건" 형식
  detailPath: string    // 클릭 시 이동 경로
}

const PAGE_SIZE = 10
const TABS = [
  { key: 'ALL',     label: '전체' },
  { key: 'PENDING', label: '서명 대기' },
  { key: 'SIGNED',  label: '서명 완료' },
  { key: 'NONE',    label: '서명 미요청' },
] as const
type TabKey = typeof TABS[number]['key']

const router = useRouter()
const search = ref('')
const isLoading = ref(true)
const fetchError = ref('')
const currentPage = ref(1)
const activeTab = ref<TabKey>('ALL')
const sortKey = ref<'date-desc' | 'date-asc' | 'name-asc' | 'name-desc'>('date-desc')

const docs = ref<Doc[]>([])

onMounted(async () => {
  try {
    const res = await api.get<{ data: Doc[] }>('/api/documents')
    docs.value = res.data.data ?? []
  } catch {
    fetchError.value = '문서 목록을 불러오지 못했어요.'
  } finally {
    isLoading.value = false
  }
})

// 번들 비주 문서 제외, 번들 primary만 표시하도록 변환
const displayItems = computed<DisplayItem[]>(() =>
  docs.value
    .filter(d => !d.bundleId || d.bundlePrimary)
    .map(d => ({
      ...d,
      displayName: d.bundleId && d.bundleDocCount > 1
        ? `${d.originalFilename} 외 ${d.bundleDocCount - 1}건`
        : d.originalFilename,
      detailPath: d.bundleId ? `/bundles/${d.bundleId}` : `/documents/${d.id}`,
    }))
)

function tabCount(key: TabKey) {
  if (key === 'ALL') return displayItems.value.length
  return displayItems.value.filter(d => d.signatureStatus === key).length
}

const filteredDocs = computed(() => {
  let list = activeTab.value === 'ALL'
    ? displayItems.value
    : displayItems.value.filter(d => d.signatureStatus === activeTab.value)

  const q = search.value.trim().toLowerCase()
  if (q) list = list.filter(d => d.displayName.toLowerCase().includes(q))

  return [...list].sort((a, b) => {
    switch (sortKey.value) {
      case 'date-asc':  return (a.createdAt ?? '').localeCompare(b.createdAt ?? '')
      case 'name-asc':  return a.displayName.localeCompare(b.displayName)
      case 'name-desc': return b.displayName.localeCompare(a.displayName)
      default:          return (b.createdAt ?? '').localeCompare(a.createdAt ?? '')
    }
  })
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredDocs.value.length / PAGE_SIZE)))

const pagedDocs = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return filteredDocs.value.slice(start, start + PAGE_SIZE)
})

watch([search, sortKey], () => { currentPage.value = 1 })

function formatDate(d: string | null) {
  if (!d) return '-'
  return d.slice(0, 10).replace(/-/g, '.')
}
async function handleDownload(doc: DisplayItem) {
  try {
    const res = await api.get(`/api/documents/${doc.id}/download`, { responseType: 'blob' })
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = doc.originalFilename
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    alert('다운로드에 실패했어요.')
  }
}
</script>
