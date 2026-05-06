<template>
  <div class="qs-page">
    <header class="qs-topbar">
      <div class="qs-topbar-inner">
        <div class="qs-topbar-brand">
          <QuSignMark variant="badge" :size="28" />
          <span class="qs-topbar-name">QuSign</span>
        </div>
        <nav class="qs-topbar-nav">
          <RouterLink class="qs-nav-link is-active" to="/documents">문서</RouterLink>
          <a class="qs-nav-link" href="#" @click.prevent>설정</a>
        </nav>
        <div class="qs-topbar-right">
          <ThemeToggle :theme="theme" @change="handleThemeToggle" />
          <div class="qs-user">
            <div class="qs-user-avatar">{{ userInitial }}</div>
            <span class="qs-user-email">{{ userEmail }}</span>
          </div>
          <button class="qs-icon-btn" title="로그아웃" @click="handleLogout">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>
    </header>

    <main class="qs-main">
      <div class="qs-page-head">
        <div>
          <h1 class="qs-page-title">내 문서</h1>
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
            <button class="qs-tab is-active">
              전체
              <span class="qs-tab-count">{{ docs.length }}</span>
            </button>
          </div>
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
            <div v-for="doc in filteredDocs" :key="doc.id" class="qs-doc">
              <div class="qs-doc-icon">
                <svg width="28" height="36" viewBox="0 0 28 36" fill="none" aria-hidden="true">
                  <rect width="28" height="36" rx="4" fill="var(--color-error-bg)"/>
                  <path d="M6 11h16M6 16h10M6 21h12" stroke="var(--color-error)"
                    stroke-width="1.5" stroke-linecap="round" opacity="0.7"/>
                  <text x="5" y="30" font-size="6" font-weight="700" fill="var(--color-error)"
                    font-family="monospace">PDF</text>
                </svg>
              </div>
              <div class="qs-doc-main">
                <div class="qs-doc-name">{{ doc.originalFilename }}</div>
                <div class="qs-doc-meta">
                  <span>{{ formatDate(doc.createdAt) }}</span>
                  <span class="qs-dot-sep">·</span>
                  <span class="qs-doc-signer">{{ doc.hashSha3256.slice(0, 12) }}…</span>
                </div>
              </div>
              <div class="qs-doc-status">
                <span class="qs-badge qs-badge-pending">
                  <span class="qs-badge-dot" style="background:var(--color-gray-400)"></span>
                  업로드됨
                </span>
              </div>
              <div class="qs-doc-actions">
                <RouterLink to="/request" class="qs-btn qs-btn-sm qs-btn-primary">
                  서명 요청
                </RouterLink>
                <button class="qs-btn qs-btn-sm qs-btn-secondary" @click="handleDownload(doc)">
                  다운로드
                </button>
              </div>
            </div>
          </div>
        </template>

        <div v-if="!isLoading && filteredDocs.length > 0" class="qs-list-foot">
          총 <strong>{{ filteredDocs.length }}</strong>개 문서
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import QuSignMark from '@/components/ui/QuSignMark.vue'
import ThemeToggle from '@/components/ui/ThemeToggle.vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/lib/api'

interface Doc {
  id: number
  originalFilename: string
  hashSha3256: string
  createdAt: string | null
}

const router = useRouter()
const auth = useAuthStore()
const theme = ref<'light' | 'dark'>('light')
const search = ref('')
const isLoading = ref(true)
const fetchError = ref('')

const userEmail = computed(() => auth.email ?? '')
const userInitial = computed(() => userEmail.value.charAt(0).toUpperCase())

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

watch(theme, (t) => {
  document.documentElement.setAttribute('data-theme', t)
}, { immediate: true })

const filteredDocs = computed(() => {
  if (!search.value.trim()) return docs.value
  const q = search.value.toLowerCase()
  return docs.value.filter(d => d.originalFilename.toLowerCase().includes(q))
})

function formatDate(d: string | null) {
  if (!d) return '-'
  return d.slice(0, 10).replace(/-/g, '.')
}
function handleThemeToggle(t: 'light' | 'dark') {
  theme.value = t
}
function handleLogout() {
  auth.logout()
  router.push('/login')
}
async function handleDownload(doc: Doc) {
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
