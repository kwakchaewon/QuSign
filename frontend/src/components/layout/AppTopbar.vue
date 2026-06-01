<template>
  <header class="qs-topbar">
    <div class="qs-topbar-inner">
      <RouterLink class="qs-topbar-brand" to="/home">
        <QuSignMark variant="badge" :size="28" />
        <span class="qs-topbar-name">QuSign</span>
      </RouterLink>
      <nav class="qs-topbar-nav">
        <RouterLink class="qs-nav-link" activeClass="is-active" to="/home">홈</RouterLink>
        <RouterLink class="qs-nav-link" activeClass="is-active" to="/documents">내 문서</RouterLink>
        <RouterLink class="qs-nav-link" activeClass="is-active" to="/request">서명 요청</RouterLink>
        <RouterLink class="qs-nav-link" activeClass="is-active" to="/verify">검증</RouterLink>
      </nav>
      <div class="qs-topbar-right">
        <ThemeToggle :theme="theme" @change="setTheme" />
        <NotificationDropdown />
        <div class="qs-user">
          <div class="qs-user-avatar" aria-hidden="true">{{ userInitial }}</div>
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
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import QuSignMark from '@/components/ui/QuSignMark.vue'
import ThemeToggle from '@/components/ui/ThemeToggle.vue'
import NotificationDropdown from '@/components/layout/NotificationDropdown.vue'
import { useAuthStore } from '@/stores/auth'
import { useTheme } from '@/composables/useTheme'

const auth = useAuthStore()
const router = useRouter()
const { theme } = useTheme()

const userEmail = computed(() => auth.email ?? '')
const userInitial = computed(() => userEmail.value.charAt(0).toUpperCase())

function setTheme(t: 'light' | 'dark') { theme.value = t }

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>
