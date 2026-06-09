<template>
  <div class="qs-page">
    <AppTopbar />

    <main class="qs-main">
      <div class="qs-page-head">
        <div>
          <h1 class="qs-page-title">문서 현황</h1>
          <p class="qs-page-sub">서명 현황을 한눈에</p>
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

      <!-- 요약 카드 -->
      <div class="udb-cards">
        <div class="udb-card-item udb-card-urgent">
          <div class="udb-card-value">
            <template v-if="summaryLoading"><span class="qs-skel udb-card-skel"></span></template>
            <template v-else>{{ summary?.receivedUnsigned ?? 0 }}</template>
          </div>
          <div class="udb-card-label">내가 서명할 것</div>
        </div>
        <div class="udb-card-item udb-card-waiting">
          <div class="udb-card-value">
            <template v-if="summaryLoading"><span class="qs-skel udb-card-skel"></span></template>
            <template v-else>{{ summary?.sentPending ?? 0 }}</template>
          </div>
          <div class="udb-card-label">상대방 대기중</div>
        </div>
        <div class="udb-card-item udb-card-done">
          <div class="udb-card-value">
            <template v-if="summaryLoading"><span class="qs-skel udb-card-skel"></span></template>
            <template v-else>{{ summary?.sentSigned ?? 0 }}</template>
          </div>
          <div class="udb-card-label">완료</div>
        </div>
      </div>

      <!-- 지금 할 일 -->
      <div class="qs-list-card">
        <div class="udb-feed-head">
          <h2 class="udb-feed-title">지금 할 일</h2>
          <span v-if="!actionLoading && actionItems.length > 0" class="udb-section-count">
            {{ actionItems.length }}건
          </span>
        </div>

        <!-- Loading -->
        <template v-if="actionLoading">
          <div class="qs-list">
            <div v-for="i in 3" :key="i" class="qs-doc qs-doc-skel">
              <div class="qs-doc-icon"><div class="qs-skel qs-skel-icon"></div></div>
              <div class="qs-doc-main">
                <div class="qs-skel qs-skel-line" style="width:55%;margin-bottom:8px"></div>
                <div class="qs-skel qs-skel-line-sm" style="width:35%"></div>
              </div>
              <div class="qs-doc-status"><div class="qs-skel qs-skel-pill"></div></div>
              <div class="qs-doc-actions"><div class="qs-skel qs-skel-btn"></div></div>
            </div>
          </div>
        </template>

        <!-- Empty -->
        <template v-else-if="actionItems.length === 0">
          <div class="udb-empty">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M9 12l2 2 4-4" stroke="var(--badge-success-text)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="12" r="9" stroke="var(--badge-success-text)" stroke-width="1.5"/>
            </svg>
            <p>모든 서명이 완료됐어요</p>
            <p class="udb-empty-sub">새 서명 요청을 보내거나 전체 목록을 확인하세요.</p>
          </div>
        </template>

        <!-- Action feed -->
        <template v-else>
          <div class="qs-list">
            <div
              v-for="item in actionItems"
              :key="item.signPath"
              class="qs-doc"
              style="cursor:pointer"
              @click="router.push(item.signPath)"
            >
              <!-- 방향 아이콘 -->
              <div class="qs-doc-icon">
                <!-- RECEIVED: 받은 편지함 -->
                <template v-if="item.direction === 'RECEIVED'">
                  <span class="udb-dir-icon udb-dir-received">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <path d="M20 12V22H4V12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M22 7H2v5h20V7z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M12 22V7" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                  </span>
                </template>
                <!-- SENT: 보내기 -->
                <template v-else>
                  <span class="udb-dir-icon udb-dir-sent">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <path d="M22 2L11 13M22 2L15 22l-4-9-9-4 20-7z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </span>
                </template>
              </div>

              <div class="qs-doc-main">
                <div class="qs-doc-name">{{ item.docName }}</div>
                <div class="udb-doc-meta">
                  <span v-if="item.direction === 'RECEIVED'">{{ item.counterpart }} 요청</span>
                  <span v-else>{{ item.counterpart }} 서명 대기</span>
                  <span class="qs-dot-sep">·</span>
                  <span>{{ formatDate(item.createdAt) }}</span>
                </div>
              </div>

              <div class="qs-doc-status">
                <span v-if="item.direction === 'RECEIVED'" class="qs-badge qs-badge-error">
                  <span class="qs-badge-dot" style="background:var(--badge-error-text)"></span>
                  서명 필요
                </span>
                <span v-else class="qs-badge qs-badge-pending">
                  <span class="qs-badge-dot" style="background:var(--color-warning)"></span>
                  대기중
                </span>
              </div>

              <div class="qs-doc-actions" @click.stop>
                <button
                  v-if="item.direction === 'RECEIVED'"
                  class="qs-btn qs-btn-sm qs-btn-primary"
                  @click="router.push(item.signPath)"
                >
                  서명하기
                </button>
                <button
                  class="qs-btn qs-btn-sm qs-btn-ghost"
                  @click="router.push(item.signPath)"
                >
                  상세보기
                </button>
              </div>
            </div>
          </div>
        </template>
      </div>

      <!-- 전체 목록 바로가기 -->
      <div class="udb-quicklinks">
        <RouterLink to="/documents/sent" class="udb-quicklink">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M22 2L11 13M22 2L15 22l-4-9-9-4 20-7z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          보낸 서명 요청 전체보기
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M9 18l6-6-6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </RouterLink>
        <RouterLink to="/received" class="udb-quicklink">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M20 12V22H4V12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M22 7H2v5h20V7z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 22V7" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          받은 서명 요청 전체보기
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M9 18l6-6-6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </RouterLink>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
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
const summaryLoading = ref(true)
const actionLoading = ref(true)
const summary = ref<Summary | null>(null)
const actionItems = ref<ActionItem[]>([])

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

function formatDate(d: string | null) {
  if (!d) return '-'
  return d.slice(0, 10).replace(/-/g, '.')
}
</script>

<style scoped>
/* 요약 카드 */
.udb-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.udb-card-item {
  padding: 1.25rem 1.5rem;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  background: var(--surface-card);
}

.udb-card-value {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1.1;
  margin-bottom: 0.375rem;
  min-height: 2.2rem;
  display: flex;
  align-items: center;
}

.udb-card-skel {
  display: inline-block;
  width: 2.5rem;
  height: 1.75rem;
  border-radius: 6px;
}

.udb-card-label {
  font-size: 0.8125rem;
  color: var(--text-tertiary);
  font-weight: 500;
}

.udb-card-urgent .udb-card-value { color: var(--badge-error-text, #dc2626); }
.udb-card-urgent { border-color: color-mix(in srgb, var(--badge-error-text, #dc2626) 20%, transparent); }

.udb-card-waiting .udb-card-value { color: var(--color-warning-text, #b45309); }

.udb-card-done .udb-card-value { color: var(--badge-success-text, #16a34a); }

/* 피드 헤더 */
.udb-feed-head {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 1.25rem 0.75rem;
  border-bottom: 1px solid var(--border-subtle, var(--border-color));
}

.udb-feed-title {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.udb-section-count {
  font-size: 0.8125rem;
  color: var(--text-tertiary);
  font-weight: 500;
}

/* 방향 아이콘 */
.udb-dir-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  flex-shrink: 0;
}

.udb-dir-received {
  background: color-mix(in srgb, var(--badge-success-text, #16a34a) 12%, transparent);
  color: var(--badge-success-text, #16a34a);
}

.udb-dir-sent {
  background: color-mix(in srgb, var(--color-primary-500) 12%, transparent);
  color: var(--color-primary-500);
}

/* Empty state */
.udb-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 3rem 1rem;
  color: var(--text-tertiary);
  font-size: 0.9375rem;
  font-weight: 500;
}

.udb-empty-sub {
  font-size: 0.8125rem;
  font-weight: 400;
  color: var(--text-tertiary);
  margin: 0;
}

/* 메타 */
.udb-doc-meta {
  font-size: 0.8125rem;
  color: var(--text-tertiary);
  margin-top: 2px;
}

/* 바로가기 */
.udb-quicklinks {
  display: flex;
  gap: 0.75rem;
  margin-top: 1rem;
  flex-wrap: wrap;
}

.udb-quicklink {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.8125rem;
  color: var(--color-primary-500);
  text-decoration: none;
  font-weight: 500;
  padding: 0.5rem 0.875rem;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--surface-card);
  transition: border-color 0.15s, background 0.15s;
}

.udb-quicklink:hover {
  border-color: var(--color-primary-500);
  background: color-mix(in srgb, var(--color-primary-500) 6%, transparent);
}
</style>
