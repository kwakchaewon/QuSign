<template>
  <div class="qs-page">
    <header class="qs-topbar">
      <div class="qs-topbar-inner">
        <RouterLink class="qs-topbar-brand" to="/">
          <QuSignMark variant="badge" :size="28" />
          <span class="qs-topbar-name">QuSign</span>
        </RouterLink>
        <nav class="qs-topbar-nav">
          <RouterLink class="qs-nav-link is-active" to="/home">홈</RouterLink>
          <RouterLink class="qs-nav-link" to="/documents">내 문서</RouterLink>
          <RouterLink class="qs-nav-link" to="/verify">검증</RouterLink>
        </nav>
        <div class="qs-topbar-right">
          <div class="qs-user">
            <div class="qs-user-avatar" aria-hidden="true">{{ userInitial }}</div>
            <span class="qs-user-email">{{ userEmail }}</span>
          </div>
          <RouterLink class="qs-btn qs-btn-ghost qs-btn-sm" to="/settings">계정 설정</RouterLink>
          <ThemeToggle :theme="theme" @change="handleThemeToggle" />
          <button class="qs-icon-btn" title="로그아웃" @click="handleLogout">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>
    </header>

    <main class="qs-home-main">
      <!-- 환영 메시지 -->
      <section class="qs-welcome">
        <h1 class="qs-welcome-title">
          안녕하세요, <strong>{{ namePart }}</strong>님
        </h1>
        <p class="qs-welcome-date">{{ todayString }}</p>
      </section>

      <!-- 통계 카드 -->
      <section>
        <div class="qs-stats">
          <template v-if="isLoading">
            <div v-for="i in 4" :key="i" class="qs-stat-card qs-stat-skel">
              <div class="qs-skel" style="width:36px;height:36px;border-radius:10px"></div>
              <div class="qs-skel qs-skel-line" style="width:40%;height:24px"></div>
              <div class="qs-skel qs-skel-line qs-skel-line-sm" style="width:60%"></div>
            </div>
          </template>
          <template v-else>
            <div class="qs-stat-card">
              <span class="qs-stat-icon is-primary">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M5 8h11a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V8z"
                    stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
                  <path d="M7 5h11a2 2 0 0 1 2 2" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
                  <path d="M9 13h6M9 17h4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
                </svg>
              </span>
              <div class="qs-stat-body">
                <span class="qs-stat-num">{{ stats.totalDocuments }}</span>
              </div>
              <div class="qs-stat-label">전체 문서</div>
            </div>

            <div class="qs-stat-card">
              <span class="qs-stat-icon is-success">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M12 3l2.4 1.6 2.9-.4 1.5 2.5 2.5 1.5-.4 2.9L22.5 13.5 21 16l.4 2.9-2.5 1.5-1.5 2.5-2.9-.4L12 24l-2.4-1.5-2.9.4-1.5-2.5-2.5-1.5.4-2.9L1.5 13.5 3 11l-.4-2.9 2.5-1.5 1.5-2.5 2.9.4L12 3z"
                    stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/>
                  <path d="M8.5 12.5l2.5 2.5L16 10" stroke="currentColor" stroke-width="1.7"
                    stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
              <div class="qs-stat-body">
                <span class="qs-stat-num">{{ stats.signedCount }}</span>
              </div>
              <div class="qs-stat-label">서명 완료</div>
            </div>

            <div class="qs-stat-card">
              <span class="qs-stat-icon is-warning">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.6"/>
                  <path d="M12 7v5l3.5 2" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
                </svg>
              </span>
              <div class="qs-stat-body">
                <span class="qs-stat-num">{{ stats.pendingCount }}</span>
              </div>
              <div class="qs-stat-label">서명 대기 중</div>
            </div>

            <div class="qs-stat-card">
              <span class="qs-stat-icon is-neutral">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.6"/>
                  <path d="M9 9l6 6M15 9l-6 6" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
                </svg>
              </span>
              <div class="qs-stat-body">
                <span class="qs-stat-num">{{ stats.expiredCount }}</span>
              </div>
              <div class="qs-stat-label">만료된 요청</div>
            </div>
          </template>
        </div>
      </section>

      <!-- 최근 요청 -->
      <section class="qs-section">
        <div class="qs-section-head">
          <h2 class="qs-section-title">최근 요청</h2>
          <RouterLink class="qs-section-link" to="/documents">
            <span>전체 보기</span>
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
              <path d="M3 6h6m-2-3l3 3-3 3" stroke="currentColor" stroke-width="1.6"
                stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </RouterLink>
        </div>
        <div class="qs-recent-card">
          <template v-if="isLoading">
            <div v-for="i in 3" :key="i" class="qs-recent-row">
              <div class="qs-skel" style="width:28px;height:32px;border-radius:6px"></div>
              <div>
                <div class="qs-skel qs-skel-line" style="width:52%"></div>
                <div class="qs-skel qs-skel-line qs-skel-line-sm" style="width:36%;margin-top:8px"></div>
              </div>
              <div class="qs-skel qs-skel-line qs-skel-line-sm" style="width:56px"></div>
              <div class="qs-skel qs-skel-pill"></div>
              <div class="qs-skel" style="width:64px;height:18px;border-radius:6px"></div>
            </div>
          </template>
          <template v-else-if="stats.recentRequests.length === 0">
            <div class="qs-empty">
              <div class="qs-empty-illu" aria-hidden="true">
                <svg width="44" height="44" viewBox="0 0 80 80" fill="none">
                  <rect x="14" y="22" width="52" height="40" rx="8"
                    fill="var(--surface-muted)" stroke="var(--border-default)" stroke-width="1"/>
                  <path d="M14 44h14l4 6h16l4-6h14" stroke="var(--border-strong)" stroke-width="1.4"
                    fill="none" stroke-linejoin="round"/>
                  <path d="M40 16v14m-5-5l5 5 5-5" stroke="var(--color-primary-500)" stroke-width="2"
                    stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <h3 class="qs-empty-title">아직 서명 요청이 없어요</h3>
              <p class="qs-empty-desc">PDF를 업로드해 첫 서명 요청을 보내보세요.</p>
              <RouterLink class="qs-btn qs-btn-primary qs-btn-md" to="/request">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <rect x="4" y="3" width="13" height="18" rx="2" stroke="currentColor" stroke-width="1.6"
                    stroke-linejoin="round"/>
                  <path d="M14 12h7m-3-3l3 3-3 3" stroke="currentColor" stroke-width="1.7"
                    stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span>첫 서명 요청 만들기</span>
              </RouterLink>
            </div>
          </template>
          <template v-else>
            <div
              v-for="item in stats.recentRequests"
              :key="item.id"
              class="qs-recent-row"
              @click="router.push(`/documents/${item.id}`)"
            >
              <div class="qs-recent-icon">
                <svg width="20" height="24" viewBox="0 0 22 26" fill="none" aria-hidden="true">
                  <path d="M2 2h11l7 7v15a0 0 0 0 1 0 0H2a0 0 0 0 1 0 0V2z"
                    fill="var(--color-error-bg)" stroke="var(--color-error)" stroke-width="1.2"/>
                  <path d="M13 2v7h7" stroke="var(--color-error)" stroke-width="1.2"
                    stroke-linejoin="round" fill="none"/>
                  <text x="11" y="20" text-anchor="middle" font-size="6" font-weight="700"
                    fill="var(--color-error)" font-family="var(--font-mono)">PDF</text>
                </svg>
              </div>
              <div class="qs-recent-main">
                <div class="qs-recent-name">{{ item.documentName }}</div>
                <div class="qs-recent-sub">{{ formatSigners(item.signers) }}</div>
              </div>
              <span class="qs-recent-time">{{ timeAgo(item.createdAt) }}</span>
              <span :class="['qs-badge', statusBadgeClass(item.status)]">
                <span class="qs-badge-dot" :style="{ background: statusDotColor(item.status) }" aria-hidden="true"></span>
                {{ statusLabel(item.status) }}
              </span>
              <button class="qs-recent-action" @click.stop="router.push(`/documents/${item.id}`)">
                상세 보기
              </button>
            </div>
          </template>
        </div>
      </section>

      <!-- 빠른 액션 -->
      <section class="qs-section">
        <div class="qs-section-head">
          <h2 class="qs-section-title">빠른 액션</h2>
        </div>
        <div class="qs-quick">
          <div class="qs-quick-card is-primary">
            <span class="qs-quick-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <rect x="4" y="3" width="13" height="18" rx="2" stroke="currentColor" stroke-width="1.6"
                  stroke-linejoin="round"/>
                <path d="M14 12h7m-3-3l3 3-3 3" stroke="currentColor" stroke-width="1.7"
                  stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M7 8h6M7 12h3" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
              </svg>
            </span>
            <div class="qs-quick-body">
              <h3 class="qs-quick-title">서명 요청 보내기</h3>
              <p class="qs-quick-desc">PDF를 업로드하고 서명자에게 요청 링크를 발송합니다.</p>
            </div>
            <RouterLink class="qs-btn qs-btn-primary qs-btn-md" to="/request">
              <span>시작하기</span>
              <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
                <path d="M3 6h6m-2-3l3 3-3 3" stroke="currentColor" stroke-width="1.6"
                  stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </RouterLink>
          </div>

          <div class="qs-quick-card">
            <span class="qs-quick-icon is-success">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M12 3l8 3v6c0 4.5-3.4 8.5-8 9-4.6-.5-8-4.5-8-9V6l8-3z"
                  stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
                <path d="M9 12.5l2 2 4-4.5" stroke="currentColor" stroke-width="1.7"
                  stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
            <div class="qs-quick-body">
              <h3 class="qs-quick-title">서명 검증하기</h3>
              <p class="qs-quick-desc">서명된 PDF의 위변조 여부를 즉시 확인합니다.</p>
            </div>
            <RouterLink class="qs-btn qs-btn-secondary qs-btn-md" to="/verify">
              <span>검증 페이지로</span>
              <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
                <path d="M3 6h6m-2-3l3 3-3 3" stroke="currentColor" stroke-width="1.6"
                  stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </RouterLink>
          </div>

          <div class="qs-quick-card">
            <span class="qs-quick-icon is-neutral">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <circle cx="9" cy="8" r="3.5" stroke="currentColor" stroke-width="1.6"/>
                <path d="M3 20c1-3.5 3.5-5 6-5s5 1.5 6 5" stroke="currentColor" stroke-width="1.6"
                  stroke-linecap="round"/>
                <circle cx="18" cy="14" r="2" stroke="currentColor" stroke-width="1.5"/>
                <path d="M18 11v1.2M18 15.8V17M14.8 14h1.2M20 14h1.2" stroke="currentColor"
                  stroke-width="1.4" stroke-linecap="round"/>
              </svg>
            </span>
            <div class="qs-quick-body">
              <h3 class="qs-quick-title">계정 관리</h3>
              <p class="qs-quick-desc">비밀번호 변경, 알림·보안 설정을 관리합니다.</p>
            </div>
            <RouterLink class="qs-btn qs-btn-ghost qs-btn-md" to="/settings">
              <span>설정으로</span>
              <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
                <path d="M3 6h6m-2-3l3 3-3 3" stroke="currentColor" stroke-width="1.6"
                  stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </RouterLink>
          </div>
        </div>
      </section>
    </main>

    <!-- 토스트 -->
    <div :class="['qs-toast-wrap', { 'is-show': toast }]" aria-live="polite">
      <div v-if="toast" class="qs-toast">
        <span class="qs-toast-dot" aria-hidden="true"></span>
        <span>{{ toast }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import QuSignMark from '@/components/ui/QuSignMark.vue'
import ThemeToggle from '@/components/ui/ThemeToggle.vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/lib/api'

interface RecentRequestItem {
  id: number
  documentName: string
  signers: string[]
  createdAt: string
  status: 'SIGNED' | 'PENDING' | 'EXPIRED'
}

interface DashboardData {
  totalDocuments: number
  signedCount: number
  pendingCount: number
  expiredCount: number
  recentRequests: RecentRequestItem[]
}

const router = useRouter()
const auth = useAuthStore()
const theme = ref<'light' | 'dark'>('light')
const isLoading = ref(true)
const toast = ref<string | null>(null)

const stats = ref<DashboardData>({
  totalDocuments: 0,
  signedCount: 0,
  pendingCount: 0,
  expiredCount: 0,
  recentRequests: [],
})

const userEmail = computed(() => auth.email ?? '')
const userInitial = computed(() => userEmail.value.charAt(0).toUpperCase())
const namePart = computed(() => userEmail.value.split('@')[0])
const todayString = computed(() => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dd}`
})

onMounted(async () => {
  try {
    const res = await api.get<{ data: DashboardData }>('/api/dashboard')
    stats.value = res.data.data
  } catch {
    // 오류 시 기본값 유지
  } finally {
    isLoading.value = false
  }
})

watch(theme, (t) => {
  document.documentElement.setAttribute('data-theme', t)
}, { immediate: true })

function handleThemeToggle(t: 'light' | 'dark') {
  theme.value = t
}

function handleLogout() {
  auth.logout()
  router.push('/login')
}

function showToast(text: string) {
  toast.value = text
  setTimeout(() => { toast.value = null }, 2400)
}

function formatSigners(signers: string[]): string {
  if (!signers || signers.length === 0) return ''
  if (signers.length === 1) return signers[0] ?? ''
  return `${signers[0] ?? ''} 외 ${signers.length - 1}명`
}

function timeAgo(isoString: string): string {
  const now = new Date()
  const past = new Date(isoString)
  const diffMs = now.getTime() - past.getTime()
  const diffMin = Math.floor(diffMs / 60000)
  const diffHour = Math.floor(diffMin / 60)
  const diffDay = Math.floor(diffHour / 24)
  const diffWeek = Math.floor(diffDay / 7)
  if (diffMin < 1) return '방금 전'
  if (diffMin < 60) return `${diffMin}분 전`
  if (diffHour < 24) return `${diffHour}시간 전`
  if (diffDay < 7) return `${diffDay}일 전`
  return `${diffWeek}주일 전`
}

const STATUS_MAP = {
  SIGNED:  { cls: 'qs-badge-success', dot: 'var(--color-success)', label: '서명 완료' },
  PENDING: { cls: 'qs-badge-warning', dot: 'var(--color-warning)', label: '대기 중' },
  EXPIRED: { cls: 'qs-badge-error',   dot: 'var(--color-error)',   label: '만료됨' },
} as const

function statusBadgeClass(status: string) {
  return STATUS_MAP[status as keyof typeof STATUS_MAP]?.cls ?? 'qs-badge-warning'
}
function statusDotColor(status: string) {
  return STATUS_MAP[status as keyof typeof STATUS_MAP]?.dot ?? 'var(--color-warning)'
}
function statusLabel(status: string) {
  return STATUS_MAP[status as keyof typeof STATUS_MAP]?.label ?? status
}

// showToast를 외부에서 사용할 경우를 위해 유지
void showToast
</script>
