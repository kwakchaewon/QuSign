<template>
  <div class="qs-notifs-page">
    <div class="qs-notifs-inner">
      <div class="qs-notifs-head">
        <h1 class="qs-notifs-title">알림</h1>
        <button
          v-if="hasUnread"
          class="qs-btn qs-btn-ghost qs-btn-sm"
          @click="handleMarkAllAsRead"
        >
          전체 읽음
        </button>
      </div>

      <!-- 로딩 -->
      <div v-if="loading && items.length === 0" class="qs-notifs-loading">불러오는 중...</div>

      <!-- 알림 목록 -->
      <ul v-else-if="items.length > 0" class="qs-notifs-list">
        <li
          v-for="n in items"
          :key="n.id"
          :class="['qs-notifs-item', { 'is-unread': !n.isRead }]"
          @click="handleItemClick(n)"
        >
          <span class="qs-notifs-icon" :data-type="n.type" aria-hidden="true">
            {{ typeIcon(n.type) }}
          </span>
          <div class="qs-notifs-body">
            <p class="qs-notifs-item-title">{{ n.title }}</p>
            <p class="qs-notifs-msg">{{ n.message }}</p>
            <time class="qs-notifs-time">{{ formatTime(n.createdAt) }}</time>
          </div>
          <span v-if="!n.isRead" class="qs-notifs-dot" aria-label="읽지 않음" />
        </li>
      </ul>

      <!-- 빈 상태 -->
      <div v-else class="qs-notifs-empty">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"
            stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M13.73 21a2 2 0 0 1-3.46 0"
            stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <p>알림이 없습니다</p>
      </div>

      <!-- 더 보기 -->
      <div v-if="hasMore" class="qs-notifs-more">
        <button class="qs-btn qs-btn-ghost" :disabled="loading" @click="loadMore">
          {{ loading ? '불러오는 중...' : '더 보기' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore, type Notification } from '@/stores/notification'

const PAGE_SIZE = 50

const store = useNotificationStore()
const router = useRouter()

const items = ref<Notification[]>([])
const page = ref(0)
const loading = ref(false)
const hasMore = ref(true)

const hasUnread = computed(() => items.value.some((n) => !n.isRead))

async function load(p: number) {
  loading.value = true
  try {
    const fetched = await store.fetchPage(p)
    if (fetched.length < PAGE_SIZE) hasMore.value = false
    if (p === 0) {
      items.value = fetched
    } else {
      items.value.push(...fetched)
    }
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  page.value++
  await load(page.value)
}

async function handleItemClick(n: Notification) {
  if (!n.isRead) {
    await store.markAsRead(n.id)
    const found = items.value.find((i) => i.id === n.id)
    if (found) found.isRead = true
  }
  if (n.referenceId) {
    router.push(`/documents/${n.referenceId}`)
  }
}

async function handleMarkAllAsRead() {
  await store.markAllAsRead()
  items.value.forEach((n) => (n.isRead = true))
}

function typeIcon(type: string): string {
  const map: Record<string, string> = {
    SIGN_DONE: '✅',
    SIGN_REQUEST: '📝',
    SIGN_CANCELLED: '❌',
    SIGN_EXPIRING_SOON: '⏰',
    SIGN_EXPIRED: '🔔',
  }
  return map[type] ?? '🔔'
}

function formatTime(isoStr: string): string {
  if (!isoStr) return ''
  const d = new Date(isoStr.replace('T', ' '))
  if (isNaN(d.getTime())) return ''
  return d.toLocaleString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}

onMounted(() => load(0))
</script>

<style scoped>
.qs-notifs-page {
  min-height: 100vh;
  background: var(--qs-bg, #f9fafb);
  padding: 40px 20px;
}

.qs-notifs-inner {
  max-width: 680px;
  margin: 0 auto;
}

.qs-notifs-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.qs-notifs-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--qs-text, #111827);
  margin: 0;
}

.qs-notifs-loading {
  text-align: center;
  padding: 40px;
  color: var(--qs-text-muted, #6b7280);
  font-size: 14px;
}

.qs-notifs-list {
  list-style: none;
  margin: 0;
  padding: 0;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--qs-border, #e5e7eb);
  background: var(--qs-surface, #fff);
}

.qs-notifs-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px 20px;
  cursor: pointer;
  border-bottom: 1px solid var(--qs-border-subtle, #f3f4f6);
  transition: background 0.15s;
}
.qs-notifs-item:last-child {
  border-bottom: none;
}
.qs-notifs-item:hover {
  background: var(--qs-hover, #f9fafb);
}
.qs-notifs-item.is-unread {
  background: var(--qs-unread-bg, #f0f0ff);
}
.qs-notifs-item.is-unread:hover {
  background: var(--qs-unread-hover, #e8e8ff);
}

.qs-notifs-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  font-size: 18px;
  flex-shrink: 0;
  background: var(--qs-hover, #f3f4f6);
}
.qs-notifs-icon[data-type="SIGN_DONE"]          { background: #dcfce7; }
.qs-notifs-icon[data-type="SIGN_REQUEST"]       { background: #ede9fe; }
.qs-notifs-icon[data-type="SIGN_CANCELLED"]     { background: #fee2e2; }
.qs-notifs-icon[data-type="SIGN_EXPIRING_SOON"] { background: #fef3c7; }
.qs-notifs-icon[data-type="SIGN_EXPIRED"]       { background: #f3f4f6; }

.qs-notifs-body {
  flex: 1;
  min-width: 0;
}

.qs-notifs-item-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--qs-text, #111827);
  margin: 0 0 3px;
}

.qs-notifs-msg {
  font-size: 13px;
  color: var(--qs-text-muted, #6b7280);
  margin: 0 0 5px;
  line-height: 1.5;
  word-break: break-word;
}

.qs-notifs-time {
  font-size: 12px;
  color: var(--qs-text-faint, #9ca3af);
}

.qs-notifs-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--qs-primary, #6366f1);
  flex-shrink: 0;
  margin-top: 6px;
}

.qs-notifs-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 60px 20px;
  color: var(--qs-text-muted, #6b7280);
  font-size: 14px;
}

.qs-notifs-more {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.qs-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: background 0.15s, color 0.15s;
}
.qs-btn-ghost {
  background: transparent;
  color: var(--qs-text-muted, #6b7280);
  border: 1px solid var(--qs-border, #e5e7eb);
}
.qs-btn-ghost:hover:not(:disabled) {
  background: var(--qs-hover, #f3f4f6);
  color: var(--qs-text, #111827);
}
.qs-btn-ghost:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.qs-btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}
</style>
