<template>
  <div class="qs-page qs-layout-centered">
    <header class="qs-topbar">
      <div class="qs-topbar-brand">
        <QuSignMark variant="badge" :size="28" />
        <span class="qs-topbar-name">QuSign</span>
      </div>
      <div class="qs-topbar-right">
        <a class="qs-link-quiet qs-topbar-help" href="#" @click.prevent>고객지원</a>
        <ThemeToggle :theme="theme" @change="handleThemeToggle" />
      </div>
    </header>

    <main class="qs-main">
      <section class="qs-card-wrap">
        <div class="qs-card">
          <div class="qs-card-head">
            <div class="qs-brand">
              <QuSignMark variant="badge" :size="48" />
              <div class="qs-brand-text">
                <h1 class="qs-brand-name">QuSign</h1>
                <p class="qs-brand-sub">양자내성암호 기반 전자서명</p>
              </div>
            </div>
            <PqcBadge variant="soft" />
          </div>

          <div class="qs-card-divider" />

          <div class="qs-card-body">
            <h2 class="qs-card-title">로그인</h2>
            <p class="qs-card-desc">기업 계정으로 로그인하고 문서를 안전하게 서명하세요.</p>

            <LoginForm @login="handleLogin" />

            <div class="qs-signup">
              <span>아직 계정이 없으신가요?</span>
              <a href="#" @click.prevent>
                회원가입
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2"
                    stroke-linecap="round" stroke-linejoin="round" />
                </svg>
              </a>
            </div>
            <div class="qs-forgot">
              <a href="#" @click.prevent>비밀번호를 잊어버리셨나요?</a>
            </div>
          </div>

          <div class="qs-card-foot">
            <TrustStrip />
          </div>
        </div>

        <p class="qs-legal">
          로그인하면
          <a href="#" @click.prevent>이용약관</a>과
          <a href="#" @click.prevent>개인정보처리방침</a>에
          동의한 것으로 간주됩니다.
        </p>
      </section>
    </main>

    <Toast :show="toast.show" :email="toast.email" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import QuSignMark from '@/components/ui/QuSignMark.vue'
import PqcBadge from '@/components/ui/PqcBadge.vue'
import TrustStrip from '@/components/ui/TrustStrip.vue'
import ThemeToggle from '@/components/ui/ThemeToggle.vue'
import Toast from '@/components/ui/Toast.vue'
import LoginForm from '@/components/LoginForm.vue'

const theme = ref<'light' | 'dark'>('light')
const toast = ref({ show: false, email: '' })

watch(theme, (t) => {
  document.documentElement.setAttribute('data-theme', t)
}, { immediate: true })

function handleThemeToggle(t: 'light' | 'dark') {
  theme.value = t
}

function handleLogin(email: string) {
  toast.value = { show: true, email }
  setTimeout(() => { toast.value = { show: false, email: '' } }, 3200)
}
</script>
