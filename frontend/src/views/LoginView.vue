<template>
  <div class="qs-page qs-layout-centered">
    <PublicTopbar show-support-link />

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
              <RouterLink to="/signup">
                회원가입
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2"
                    stroke-linecap="round" stroke-linejoin="round" />
                </svg>
              </RouterLink>
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
          <a href="#" @click.prevent="showTerms = true">이용약관</a>과
          <a href="#" @click.prevent="showPrivacy = true">개인정보처리방침</a>에
          동의한 것으로 간주됩니다.
        </p>

        <TermsModal v-model="showTerms" type="terms" />
        <TermsModal v-model="showPrivacy" type="privacy" />
      </section>
    </main>

    <Toast :show="toast.show" :email="toast.email" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import QuSignMark from '@/components/ui/QuSignMark.vue'
import PqcBadge from '@/components/ui/PqcBadge.vue'
import TrustStrip from '@/components/ui/TrustStrip.vue'
import Toast from '@/components/ui/Toast.vue'
import LoginForm from '@/components/LoginForm.vue'
import PublicTopbar from '@/components/layout/PublicTopbar.vue'
import TermsModal from '@/components/ui/TermsModal.vue'

const toast = ref({ show: false, email: '' })
const showTerms = ref(false)
const showPrivacy = ref(false)

function handleLogin(email: string) {
  toast.value = { show: true, email }
  setTimeout(() => { toast.value = { show: false, email: '' } }, 3200)
}
</script>
