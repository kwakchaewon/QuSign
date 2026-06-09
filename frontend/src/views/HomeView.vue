<template>
  <div class="qs-page">
    <AppTopbar />

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
          <template v-if="summaryLoading">
            <div v-for="i in 3" :key="i" class="qs-stat-card qs-stat-skel">
              <div class="qs-skel" style="width:36px;height:36px;border-radius:10px"></div>
              <div class="qs-skel qs-skel-line" style="width:40%;height:24px"></div>
              <div class="qs-skel qs-skel-line qs-skel-line-sm" style="width:60%"></div>
            </div>
          </template>
          <template v-else>
            <div class="qs-stat-card">
              <span class="qs-stat-icon is-error">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M20 12V22H4V12" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M22 7H2v5h20V7z" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M12 22V7" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
                </svg>
              </span>
              <div class="qs-stat-body">
                <span class="qs-stat-num">{{ summary?.receivedUnsigned ?? 0 }}</span>
              </div>
              <div class="qs-stat-label">내가 서명할 것</div>
            </div>

            <div class="qs-stat-card">
              <span class="qs-stat-icon is-warning">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.6"/>
                  <path d="M12 7v5l3.5 2" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
                </svg>
              </span>
              <div class="qs-stat-body">
                <span class="qs-stat-num">{{ summary?.sentPending ?? 0 }}</span>
              </div>
              <div class="qs-stat-label">상대방 대기중</div>
            </div>

            <div class="qs-stat-card">
              <span class="qs-stat-icon is-success">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M9 12.5l2.5 2.5L16 10" stroke="currentColor" stroke-width="1.7"
                    stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.6"/>
                </svg>
              </span>
              <div class="qs-stat-body">
                <span class="qs-stat-num">{{ summary?.sentSigned ?? 0 }}</span>
              </div>
              <div class="qs-stat-label">완료</div>
            </div>
          </template>
        </div>
      </section>

      <!-- 지금 할 일 -->
      <section class="qs-section">
        <div class="qs-section-head">
          <h2 class="qs-section-title">지금 할 일</h2>
          <RouterLink class="qs-section-link" to="/documents">
            <span>문서 현황</span>
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
              <path d="M3 6h6m-2-3l3 3-3 3" stroke="currentColor" stroke-width="1.6"
                stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </RouterLink>
        </div>
        <div class="qs-recent-card">
          <template v-if="actionLoading">
            <div v-for="i in 3" :key="i" class="qs-recent-row">
              <div class="qs-skel" style="width:32px;height:32px;border-radius:8px"></div>
              <div>
                <div class="qs-skel qs-skel-line" style="width:52%"></div>
                <div class="qs-skel qs-skel-line qs-skel-line-sm" style="width:36%;margin-top:8px"></div>
              </div>
              <div class="qs-skel qs-skel-line qs-skel-line-sm" style="width:56px"></div>
              <div class="qs-skel qs-skel-pill"></div>
              <div class="qs-skel" style="width:64px;height:18px;border-radius:6px"></div>
            </div>
          </template>
          <template v-else-if="actionItems.length === 0">
            <div class="qs-empty">
              <div class="qs-empty-illu" aria-hidden="true">
                <svg width="44" height="44" viewBox="0 0 24 24" fill="none">
                  <path d="M9 12l2 2 4-4" stroke="var(--badge-success-text, #16a34a)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="12" cy="12" r="9" stroke="var(--badge-success-text, #16a34a)" stroke-width="1.5"/>
                </svg>
              </div>
              <h3 class="qs-empty-title">지금 처리할 서명 요청이 없어요</h3>
              <p class="qs-empty-desc">새 서명 요청을 보내거나 받은 문서를 확인하세요.</p>
              <RouterLink class="qs-btn qs-btn-primary qs-btn-md" to="/request">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <rect x="4" y="3" width="13" height="18" rx="2" stroke="currentColor" stroke-width="1.6"
                    stroke-linejoin="round"/>
                  <path d="M14 12h7m-3-3l3 3-3 3" stroke="currentColor" stroke-width="1.7"
                    stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span>서명 요청 보내기</span>
              </RouterLink>
            </div>
          </template>
          <template v-else>
            <div
              v-for="item in actionItems"
              :key="item.signPath"
              class="qs-recent-row"
              style="cursor:pointer"
              @click="router.push(item.signPath)"
            >
              <!-- 방향 아이콘 -->
              <div class="qs-recent-icon">
                <span v-if="item.direction === 'RECEIVED'" class="qs-action-dir qs-action-dir-recv">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <path d="M20 12V22H4V12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M22 7H2v5h20V7z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M12 22V7" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                </span>
                <span v-else class="qs-action-dir qs-action-dir-sent">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <path d="M22 2L11 13M22 2L15 22l-4-9-9-4 20-7z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </span>
              </div>
              <div class="qs-recent-main">
                <div class="qs-recent-name">{{ item.docName }}</div>
                <div class="qs-recent-sub">
                  <span v-if="item.direction === 'RECEIVED'">{{ item.counterpart }} 요청</span>
                  <span v-else>{{ item.counterpart }} 서명 대기</span>
                </div>
              </div>
              <span class="qs-recent-time">{{ formatDate(item.createdAt) }}</span>
              <span v-if="item.direction === 'RECEIVED'" class="qs-badge qs-badge-error">
                <span class="qs-badge-dot" style="background:var(--badge-error-text, #dc2626)" aria-hidden="true"></span>
                서명 필요
              </span>
              <span v-else class="qs-badge qs-badge-warning">
                <span class="qs-badge-dot" style="background:var(--color-warning)" aria-hidden="true"></span>
                대기중
              </span>
              <button
                v-if="item.direction === 'RECEIVED'"
                class="qs-recent-action"
                @click.stop="router.push(item.signPath)"
              >서명하기</button>
              <button
                v-else
                class="qs-recent-action"
                @click.stop="router.push(item.signPath)"
              >상세 보기</button>
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
            <RouterLink class="qs-btn qs-btn-secondary qs-btn-md" to="/settings">
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/lib/api'
import AppTopbar from '@/components/layout/AppTopbar.vue'

interface Summary {
  sentPending: number
  sentSigned: number
  receivedUnsigned: number
}

interface ActionItem {
  direction: 'SENT' | 'RECEIVED'
  docName: string
  counterpart: string
  createdAt: string
  signPath: string
  token?: string
  bundleToken?: string
  bundleId?: number
}

const router = useRouter()
const auth = useAuthStore()
const summaryLoading = ref(true)
const actionLoading = ref(true)
const toast = ref<string | null>(null)
const summary = ref<Summary | null>(null)
const actionItems = ref<ActionItem[]>([])

const namePart = computed(() => auth.email?.split('@')[0] ?? '')
const todayString = computed(() => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dd}`
})

onMounted(async () => {
  const [summaryResult, actionResult] = await Promise.all([
    api.get<{ data: Summary }>('/api/dashboard/summary').catch(() => null),
    api.get<{ data: { items: ActionItem[] } }>('/api/dashboard/action-items').catch(() => null),
  ])
  summary.value = summaryResult?.data.data ?? null
  summaryLoading.value = false
  actionItems.value = actionResult?.data.data?.items ?? []
  actionLoading.value = false
})

function showToast(text: string) {
  toast.value = text
  setTimeout(() => { toast.value = null }, 2400)
}

function formatDate(d: string | null) {
  if (!d) return '-'
  return d.slice(0, 10).replace(/-/g, '.')
}

void showToast
</script>
