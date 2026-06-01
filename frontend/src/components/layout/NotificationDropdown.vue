<template>
  <div class="qs-notif-wrap" ref="wrapRef">
    <!-- 벨 버튼 -->
    <button class="qs-icon-btn qs-notif-btn" title="알림" @click="toggleOpen">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        <path d="M13.73 21a2 2 0 0 1-3.46 0"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <span v-if="unreadCount > 0" class="qs-notif-badge">
        {{ unreadCount > 99 ? '99+' : unreadCount }}
      </span>
    </button>

    <!-- 드롭다운 패널 -->
    <div v-if="isOpen" class="qs-notif-panel">
      <div class="qs-notif-header">
        <span class="qs-notif-header-title">알림</span>
        <div class="qs-notif-header-actions">
          <button
            v-if="unreadCount > 0"
            class="qs-notif-read-all"
            @click="handleMarkAllAsRead"
          >전체 읽음</button>
          <!-- TODO: Claude Design 목업 완성 후 복원
          <RouterLink class="qs-notif-view-all" to="/notifications" @click="isOpen = false">
            전체 보기
          </RouterLink>
          -->
        </div>
      </div>

      <ul v-if="notifications.length > 0" class="qs-notif-list">
        <li
          v-for="n in notifications"
          :key="n.id"
          :class="['qs-notif-item', { 'is-unread': !n.isRead }]"
          @click="handleItemClick(n)"
        >
          <span class="qs-notif-icon" :data-type="n.type" aria-hidden="true">
            {{ typeIcon(n.type) }}
          </span>
          <div class="qs-notif-body">
            <p class="qs-notif-title">{{ n.title }}</p>
            <p class="qs-notif-msg">{{ n.message }}</p>
            <time class="qs-notif-time">{{ formatTime(n.createdAt) }}</time>
          </div>
          <span v-if="!n.isRead" class="qs-notif-dot" aria-label="읽지 않음" />
        </li>
      </ul>

      <div v-else class="qs-notif-empty">새로운 알림이 없습니다</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore, type Notification } from '@/stores/notification'

const store = useNotificationStore()
const router = useRouter()

const isOpen = ref(false)
const wrapRef = ref<HTMLElement | null>(null)

const notifications = computed(() => store.notifications)
const unreadCount = computed(() => store.unreadCount)

function toggleOpen() {
  isOpen.value = !isOpen.value
}

async function handleItemClick(n: Notification) {
  if (!n.isRead) await store.markAsRead(n.id)
  isOpen.value = false
  if (n.referenceId) {
    router.push(`/documents/${n.referenceId}`)
  }
}

async function handleMarkAllAsRead() {
  await store.markAllAsRead()
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
  const now = new Date()
  const diffMs = now.getTime() - d.getTime()
  const diffMin = Math.floor(diffMs / 60000)
  if (diffMin < 1) return '방금 전'
  if (diffMin < 60) return `${diffMin}분 전`
  const diffH = Math.floor(diffMin / 60)
  if (diffH < 24) return `${diffH}시간 전`
  return `${Math.floor(diffH / 24)}일 전`
}

function onClickOutside(e: MouseEvent) {
  if (wrapRef.value && !wrapRef.value.contains(e.target as Node)) {
    isOpen.value = false
  }
}

onMounted(() => document.addEventListener('mousedown', onClickOutside))
onUnmounted(() => document.removeEventListener('mousedown', onClickOutside))
</script>

<style scoped>
/* 벨 버튼 래퍼 — static으로 두어 패널이 qs-topbar-inner 기준으로 위치 */
.qs-notif-wrap {
  position: static;
}

/* 배지 기준점은 버튼 자체 */
.qs-notif-btn {
  position: relative;
}

/* 미읽음 배지 */
.qs-notif-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 16px;
  height: 16px;
  padding: 0 3px;
  border-radius: 8px;
  background: var(--color-error);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  line-height: 16px;
  text-align: center;
  pointer-events: none;
}

/* 패널 — qs-topbar-inner 기준 absolute, 우측 패딩 끝과 정렬 */
.qs-notif-panel {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 4px;
  width: 340px;
  max-height: 480px;
  overflow-y: auto;
  border-radius: var(--radius-lg, 14px);
  background: var(--surface-elevated);
  border: 1px solid var(--border-default);
  box-shadow: var(--shadow-lg);
  z-index: var(--z-dropdown, 100);
}

/* 헤더 */
.qs-notif-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 10px;
  border-bottom: 1px solid var(--border-default);
  position: sticky;
  top: 0;
  background: var(--surface-elevated);
  z-index: 1;
}

.qs-notif-header-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.qs-notif-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.qs-notif-read-all {
  font-size: 12px;
  color: var(--color-primary-500);
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px 4px;
}
.qs-notif-read-all:hover { text-decoration: underline; }

.qs-notif-view-all {
  font-size: 12px;
  color: var(--text-secondary);
  text-decoration: none;
  padding: 2px 4px;
}
.qs-notif-view-all:hover {
  text-decoration: underline;
  color: var(--text-primary);
}

/* 목록 */
.qs-notif-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.qs-notif-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid var(--border-default);
  transition: background var(--duration-fast, 0.15s);
}
.qs-notif-item:last-child { border-bottom: none; }
.qs-notif-item:hover { background: var(--surface-muted); }
.qs-notif-item.is-unread { background: var(--color-info-bg); }
.qs-notif-item.is-unread:hover { background: var(--color-info-bg); filter: brightness(0.96); }

/* 아이콘 — 다크 대응 시맨틱 토큰 */
.qs-notif-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  font-size: 16px;
  flex-shrink: 0;
  background: var(--surface-muted);
}
.qs-notif-icon[data-type="SIGN_DONE"]          { background: var(--color-success-bg); }
.qs-notif-icon[data-type="SIGN_REQUEST"]       { background: var(--color-info-bg); }
.qs-notif-icon[data-type="SIGN_CANCELLED"]     { background: var(--color-error-bg); }
.qs-notif-icon[data-type="SIGN_EXPIRING_SOON"] { background: var(--color-warning-bg); }
.qs-notif-icon[data-type="SIGN_EXPIRED"]       { background: var(--surface-muted); }

/* 본문 */
.qs-notif-body { flex: 1; min-width: 0; }

.qs-notif-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 2px;
}

.qs-notif-msg {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0 0 4px;
  line-height: 1.4;
  word-break: break-word;
}

.qs-notif-time {
  font-size: 11px;
  color: var(--text-tertiary);
}

.qs-notif-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary-500);
  flex-shrink: 0;
  margin-top: 5px;
}

/* 빈 상태 */
.qs-notif-empty {
  padding: 32px 16px;
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
