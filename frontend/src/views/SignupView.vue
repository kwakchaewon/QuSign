<template>
  <div class="qs-page qs-layout-centered">
    <PublicTopbar show-support-link />

    <main class="qs-main">
      <section class="qs-card-wrap qs-layout-centered-card">
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
            <h2 class="qs-card-title">회원가입</h2>

            <SignupForm @success="handleSuccess" />

            <div class="qs-signup">
              <span>이미 계정이 있으신가요?</span>
              <RouterLink to="/login">
                로그인
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
              </RouterLink>
            </div>
          </div>

        </div>
      </section>
    </main>

    <!-- 성공 오버레이 -->
    <div :class="['qs-success', success.show && 'is-show']" :aria-hidden="!success.show">
      <div class="qs-success-card">
        <div class="qs-success-orb">
          <svg viewBox="0 0 80 80" width="80" height="80" aria-hidden="true">
            <circle cx="40" cy="40" r="36" class="qs-success-ring" />
            <path d="M24 41 L36 53 L58 30" class="qs-success-tick"
              fill="none" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </div>
        <h3 class="qs-success-title">키쌍 생성 완료</h3>
        <p class="qs-success-desc">
          ML-DSA-65 공개키·개인키가 안전하게 저장되었어요.<br />
          이제 <strong>{{ success.email }}</strong> 계정으로 로그인할 수 있어요.
        </p>
        <div class="qs-success-keys">
          <div class="qs-success-key">
            <span class="qs-success-key-k">공개키</span>
            <code class="qs-success-key-v">pk_a8f3…b4e7</code>
          </div>
          <div class="qs-success-key">
            <span class="qs-success-key-k">개인키</span>
            <code class="qs-success-key-v">
              sk_············
              <span class="qs-success-key-tag">암호화 보관</span>
            </code>
          </div>
        </div>
        <RouterLink to="/login" class="qs-btn qs-btn-primary qs-btn-lg qs-btn-block">
          <span>로그인 화면으로 이동</span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import QuSignMark from '@/components/ui/QuSignMark.vue'
import PqcBadge from '@/components/ui/PqcBadge.vue'
import SignupForm from '@/components/SignupForm.vue'
import PublicTopbar from '@/components/layout/PublicTopbar.vue'

const success = ref({ show: false, email: '' })

function handleSuccess(email: string) {
  success.value = { show: true, email }
}
</script>
