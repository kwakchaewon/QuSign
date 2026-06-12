<template>
  <div class="qs-admin-wrap">
    <aside class="qs-admin-sidebar">
      <div class="qs-admin-sidebar-header">
        <span class="qs-admin-brand">QuSign</span>
        <span class="qs-admin-badge">관리자</span>
      </div>
      <nav class="qs-admin-nav">
        <RouterLink class="qs-admin-nav-item" activeClass="is-active" :to="{ name: 'admin-stats' }" exact-active-class="is-active">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <rect x="3" y="12" width="4" height="9" rx="1" stroke="currentColor" stroke-width="2"/>
            <rect x="10" y="7" width="4" height="14" rx="1" stroke="currentColor" stroke-width="2"/>
            <rect x="17" y="3" width="4" height="18" rx="1" stroke="currentColor" stroke-width="2"/>
          </svg>
          통계
        </RouterLink>
        <RouterLink class="qs-admin-nav-item" activeClass="is-active" :to="{ name: 'admin-users' }">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
            <path d="M3 21v-2a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75M21 21v-2a4 4 0 0 0-3-3.87" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          사용자 관리
        </RouterLink>
        <RouterLink class="qs-admin-nav-item" activeClass="is-active" :to="{ name: 'admin-audit' }">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M9 12h6M9 16h6M17 21H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l5 5v11a2 2 0 0 1-2 2z"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          감사 로그
        </RouterLink>
      </nav>
      <div class="qs-admin-sidebar-footer">
        <button class="qs-admin-logout" @click="handleLogout">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          로그아웃
        </button>
      </div>
    </aside>
    <main class="qs-admin-main">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>
