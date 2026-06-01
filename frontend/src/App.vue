<template>
  <div>
    <RouterView />

    <div :class="['qs-toast-wrap', { 'is-show': message !== null }]" aria-live="assertive">
      <div class="qs-toast">
        <div class="qs-toast-ic is-error" aria-hidden="true">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
            <path d="M12 8v5M12 16h.01" stroke="currentColor" stroke-width="2.2"
              stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div>
          <p class="qs-toast-t">오류</p>
          <p class="qs-toast-d">{{ message }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'

const { message } = useToast()
const auth = useAuthStore()
const notificationStore = useNotificationStore()

// 페이지 새로고침 시 기존 토큰으로 알림 초기화
onMounted(async () => {
  if (auth.isLoggedIn && auth.token) {
    await notificationStore.fetchNotifications()
    notificationStore.connectSSE(auth.token)
  }
})
</script>
