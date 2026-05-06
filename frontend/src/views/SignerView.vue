<template>
  <div class="qs-page qs-sg-page">
    <header class="qs-topbar">
      <div class="qs-topbar-inner">
        <div class="qs-topbar-brand">
          <QuSignMark variant="badge" :size="24" />
          <span class="qs-topbar-label">전자서명 요청</span>
        </div>
        <div class="qs-topbar-right">
          <div class="sg-pqc-pill">
            <span class="sg-pqc-dot"></span>
            <span>ML-DSA-65</span>
          </div>
          <ThemeToggle :theme="theme" @change="handleThemeToggle" />
        </div>
      </div>
    </header>

    <main class="qs-main">
      <ol class="sg-steps" aria-label="서명 진행 단계">
        <li class="sg-step" :class="{ 'is-active': step === 1, 'is-done': step > 1 }">
          <span class="sg-step-dot">
            <svg v-if="step > 1" width="10" height="10" viewBox="0 0 24 24" fill="none">
              <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="3"
                stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <template v-else>1</template>
          </span>
          <span class="sg-step-label">본인 확인</span>
        </li>
        <span class="sg-step-line" :class="{ 'is-done': step > 1 }"></span>
        <li class="sg-step" :class="{ 'is-active': step === 2, 'is-done': step > 2 }">
          <span class="sg-step-dot">
            <svg v-if="step > 2" width="10" height="10" viewBox="0 0 24 24" fill="none">
              <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="3"
                stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <template v-else>2</template>
          </span>
          <span class="sg-step-label">문서 검토</span>
        </li>
        <span class="sg-step-line" :class="{ 'is-done': step > 2 }"></span>
        <li class="sg-step" :class="{ 'is-active': step === 3 }">
          <span class="sg-step-dot">3</span>
          <span class="sg-step-label">완료</span>
        </li>
      </ol>

      <!-- Step 1: Login -->
      <div v-if="step === 1" class="sg-card">
        <h2 class="sg-card-title">본인 확인</h2>
        <p class="sg-card-desc">
          QuSign 계정으로 로그인하여 서명 요청을 확인해 주세요.
        </p>

        <div class="qs-field">
          <label class="qs-label" for="sg-email">이메일</label>
          <div class="qs-input" :class="{ 'is-error': !!loginErr }">
            <span class="qs-input-icon">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <rect x="2" y="4" width="20" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="m2 7 10 7 10-7" stroke="currentColor" stroke-width="2"/>
              </svg>
            </span>
            <input id="sg-email" v-model="loginEmail" type="email"
              autocomplete="username"
              placeholder="name@company.com"
              :disabled="isLoginLoading" />
          </div>
        </div>

        <div class="qs-field">
          <label class="qs-label" for="sg-pw">비밀번호</label>
          <div class="qs-input" :class="{ 'is-error': !!loginErr }">
            <span class="qs-input-icon">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="9" width="18" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M7 9V7a5 5 0 0 1 10 0v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </span>
            <input id="sg-pw" v-model="loginPw"
              :type="showLoginPw ? 'text' : 'password'"
              autocomplete="current-password"
              placeholder="비밀번호 입력"
              :disabled="isLoginLoading"
              @keydown.enter="handleLogin" />
            <button type="button" class="qs-input-eye"
              :aria-label="showLoginPw ? '비밀번호 숨기기' : '비밀번호 표시'"
              @click="showLoginPw = !showLoginPw">
              <svg v-if="showLoginPw" width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M3 3l18 18" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
                <path d="M10.6 6.2A10 10 0 0 1 12 6c6.5 0 10 6 10 6a14.7 14.7 0 0 1-2.6 3.4M6.7 7.4A14.7 14.7 0 0 0 2 12s3.5 6 10 6c1.7 0 3.2-.4 4.5-1"
                  stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
              </svg>
              <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7z" stroke="currentColor" stroke-width="1.6" />
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.6" />
              </svg>
            </button>
          </div>
        </div>

        <div v-if="loginErr" class="qs-alert" role="alert">
          <span class="qs-alert-dot" aria-hidden="true" />
          {{ loginErr }}
        </div>

        <button class="qs-btn qs-btn-full qs-btn-primary"
          :disabled="isLoginLoading || !loginEmail || !loginPw"
          @click="handleLogin">
          <template v-if="isLoginLoading">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" style="animation: qs-spin 0.8s linear infinite">
              <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-opacity="0.25" stroke-width="2.4" />
              <path d="M21 12a9 9 0 0 0-9-9" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" />
            </svg>
            <span>로그인 중…</span>
          </template>
          <template v-else>
            <span>로그인</span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </template>
        </button>
      </div>

      <!-- Step 2: Document review -->
      <div v-if="step === 2" class="sg-card">
        <h2 class="sg-card-title">문서 검토 및 서명</h2>
        <p class="sg-card-desc">문서 내용을 확인하고 서명에 동의해 주세요.</p>

        <div class="sg-doc-meta">
          <div class="sg-doc-meta-name">
            <svg width="24" height="30" viewBox="0 0 24 30" fill="none" aria-hidden="true">
              <rect width="24" height="30" rx="3" fill="var(--color-error-bg)"/>
              <text x="3" y="19" font-size="6" font-weight="800" fill="var(--color-error)"
                font-family="monospace">PDF</text>
            </svg>
            <span class="sg-doc-name">서명 요청 문서</span>
          </div>
          <div class="sg-meta-rows">
            <div class="sg-meta-row">
              <span class="sg-meta-k">서명자</span>
              <span class="sg-meta-v">{{ auth.email }}</span>
            </div>
            <div class="sg-meta-row">
              <span class="sg-meta-k">서명 토큰</span>
              <span class="sg-meta-v is-mono">{{ signToken }}</span>
            </div>
          </div>
        </div>

        <div class="sg-pdf-preview">
          <div class="sg-pdf-toolbar">
            <span class="sg-pdf-label">문서 미리보기</span>
          </div>
          <div class="sg-pdf-placeholder">
            <svg class="sg-pdf-placeholder-icon" width="48" height="56" viewBox="0 0 48 56" fill="none">
              <rect width="48" height="56" rx="6" fill="var(--border-default)"/>
              <rect x="8" y="12" width="32" height="4" rx="2" fill="var(--text-tertiary)" opacity="0.4"/>
              <rect x="8" y="20" width="28" height="3" rx="1.5" fill="var(--text-tertiary)" opacity="0.25"/>
              <rect x="8" y="26" width="30" height="3" rx="1.5" fill="var(--text-tertiary)" opacity="0.25"/>
              <rect x="8" y="32" width="24" height="3" rx="1.5" fill="var(--text-tertiary)" opacity="0.25"/>
            </svg>
            <span>PDF 미리보기</span>
          </div>
        </div>

        <div class="sg-consent">
          <label class="sg-check-row" @click.prevent="consent1 = !consent1">
            <div class="sg-check-box" :class="{ 'is-checked': consent1 }">
              <svg v-if="consent1" width="12" height="12" viewBox="0 0 24 24" fill="none">
                <path d="M20 6L9 17l-5-5" stroke="#fff" stroke-width="3" stroke-linecap="round"
                  stroke-linejoin="round"/>
              </svg>
            </div>
            <span class="sg-check-label">
              문서 내용을 확인했으며 <strong>서명에 동의</strong>합니다.
            </span>
          </label>
          <label class="sg-check-row" @click.prevent="consent2 = !consent2">
            <div class="sg-check-box" :class="{ 'is-checked': consent2 }">
              <svg v-if="consent2" width="12" height="12" viewBox="0 0 24 24" fill="none">
                <path d="M20 6L9 17l-5-5" stroke="#fff" stroke-width="3" stroke-linecap="round"
                  stroke-linejoin="round"/>
              </svg>
            </div>
            <span class="sg-check-label">
              <strong>ML-DSA 전자서명의 법적 효력</strong>에 동의합니다.
            </span>
          </label>
        </div>

        <div class="qs-field">
          <label class="qs-label" for="sg-sign-pw">서명 비밀번호</label>
          <p class="qs-help" style="margin-bottom:8px">ML-DSA 개인키 복호화를 위해 QuSign 계정 비밀번호를 입력해 주세요.</p>
          <div class="qs-input" :class="{ 'is-error': !!signErr }">
            <span class="qs-input-icon">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="9" width="18" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M7 9V7a5 5 0 0 1 10 0v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </span>
            <input id="sg-sign-pw" v-model="signPassword"
              :type="showSignPw ? 'text' : 'password'"
              autocomplete="current-password"
              placeholder="계정 비밀번호 입력"
              :disabled="isSigning"
              @keydown.enter="handleSign" />
            <button type="button" class="qs-input-eye"
              :aria-label="showSignPw ? '비밀번호 숨기기' : '비밀번호 표시'"
              @click="showSignPw = !showSignPw">
              <svg v-if="showSignPw" width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M3 3l18 18" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
                <path d="M10.6 6.2A10 10 0 0 1 12 6c6.5 0 10 6 10 6a14.7 14.7 0 0 1-2.6 3.4M6.7 7.4A14.7 14.7 0 0 0 2 12s3.5 6 10 6c1.7 0 3.2-.4 4.5-1"
                  stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
              </svg>
              <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7z" stroke="currentColor" stroke-width="1.6" />
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.6" />
              </svg>
            </button>
          </div>
        </div>

        <div v-if="signErr" class="qs-alert" role="alert">
          <span class="qs-alert-dot" aria-hidden="true" />
          {{ signErr }}
        </div>

        <button class="qs-btn qs-btn-full qs-btn-primary"
          :disabled="!consent1 || !consent2 || !signPassword || isSigning"
          @click="handleSign">
          서명하기
        </button>
      </div>

      <!-- Step 3: Success -->
      <div v-if="step === 3" class="sg-card">
        <div class="sg-success">
          <div class="sg-success-icon">
            <div class="sg-success-check">
              <svg width="36" height="36" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M20 6L9 17l-5-5" stroke="var(--color-success)" stroke-width="3"
                  stroke-linecap="round" stroke-linejoin="round"
                  stroke-dasharray="30" stroke-dashoffset="30"
                  style="animation:qs-draw 0.5s 0.15s ease forwards"/>
              </svg>
            </div>
          </div>
          <h2 class="sg-success-title">서명이 완료되었습니다</h2>
          <p class="sg-success-desc">
            ML-DSA-65 알고리즘으로 서명되었습니다.<br>
            서명된 PDF를 다운로드해 보관하세요.
          </p>

          <div class="sg-success-meta">
            <div class="sg-success-row">
              <span class="sg-success-k">서명자</span>
              <span class="sg-success-v">{{ auth.email }}</span>
            </div>
            <div class="sg-success-row">
              <span class="sg-success-k">서명 일시</span>
              <span class="sg-success-v">{{ signedAt }}</span>
            </div>
            <div class="sg-success-row">
              <span class="sg-success-k">알고리즘</span>
              <span class="sg-success-v is-mono">ML-DSA-65</span>
            </div>
          </div>
        </div>
      </div>
    </main>

    <Transition name="sg-fade">
      <div v-if="isSigning" class="sg-signing-overlay">
        <div class="sg-signing-modal">
          <div class="sg-signing-spinner"></div>
          <p class="sg-signing-title">서명 생성 중...</p>
          <p class="sg-signing-sub">ML-DSA-65로 서명값을 생성하고 있습니다</p>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import QuSignMark from '@/components/ui/QuSignMark.vue'
import ThemeToggle from '@/components/ui/ThemeToggle.vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/lib/api'

const theme = ref<'light' | 'dark'>('light')
const route = useRoute()
const auth = useAuthStore()

const signToken = computed(() => (route.params.token as string) ?? '')
const step = ref(1)

// Step 1
const loginEmail = ref('')
const loginPw = ref('')
const showLoginPw = ref(false)
const isLoginLoading = ref(false)
const loginErr = ref<string | null>(null)

// Step 2
const consent1 = ref(false)
const consent2 = ref(false)
const signPassword = ref('')
const showSignPw = ref(false)
const isSigning = ref(false)
const signErr = ref<string | null>(null)
const signedAt = ref('')

watch(theme, (t) => document.documentElement.setAttribute('data-theme', t), { immediate: true })
function handleThemeToggle(t: 'light' | 'dark') { theme.value = t }

onMounted(() => {
  if (auth.isLoggedIn) step.value = 2
})

async function handleLogin() {
  if (!loginEmail.value || !loginPw.value) return
  isLoginLoading.value = true
  loginErr.value = null
  try {
    await auth.login(loginEmail.value.trim(), loginPw.value)
    step.value = 2
  } catch (err: any) {
    loginErr.value = err.response?.data?.message ?? '이메일 또는 비밀번호가 올바르지 않아요'
  } finally {
    isLoginLoading.value = false
  }
}

async function handleSign() {
  isSigning.value = true
  signErr.value = null
  try {
    await api.post(`/api/signature-requests/${signToken.value}/sign`, { password: signPassword.value })
    signedAt.value = new Date().toLocaleString('ko-KR', {
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit',
    })
    step.value = 3
  } catch (err: any) {
    signErr.value = err.response?.data?.message ?? '서명에 실패했어요. 비밀번호를 확인해 주세요.'
  } finally {
    isSigning.value = false
  }
}
</script>

<style scoped>
.sg-fade-enter-active, .sg-fade-leave-active { transition: opacity 0.2s; }
.sg-fade-enter-from, .sg-fade-leave-to { opacity: 0; }
@keyframes sg-spin { to { transform: rotate(360deg); } }
</style>
