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

      <!-- Step 1: OTP verification -->
      <div v-if="step === 1" class="sg-card">
        <h2 class="sg-card-title">본인 확인</h2>
        <p class="sg-card-desc">
          서명 요청을 받은 이메일 주소를 입력하고 인증 코드를 받아 주세요.
        </p>

        <div class="qs-field">
          <label class="qs-label" for="sg-email">이메일</label>
          <div class="qs-input" :class="{ 'is-error': emailError }">
            <span class="qs-input-icon">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <rect x="2" y="4" width="20" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="m2 7 10 7 10-7" stroke="currentColor" stroke-width="2"/>
              </svg>
            </span>
            <input id="sg-email" v-model="email" type="email"
              placeholder="서명 요청을 받은 이메일"
              :disabled="otpSent"
              @blur="validateEmail">
          </div>
          <span v-if="emailError" class="qs-help is-error">올바른 이메일 형식이 아닙니다</span>
        </div>

        <button v-if="!otpSent"
          class="qs-btn qs-btn-full qs-btn-primary"
          :disabled="!email || !!emailError"
          @click="sendOtp">
          인증 코드 받기
        </button>

        <template v-if="otpSent">
          <p class="qs-label" style="margin-bottom:10px">
            <strong>{{ email }}</strong>으로 6자리 코드가 발송되었습니다
          </p>

          <div class="sg-otp-row">
            <input
              v-for="(_, i) in 6"
              :key="i"
              :ref="el => { if (el) otpRefs[i] = el as HTMLInputElement }"
              class="sg-otp-box"
              :class="{ 'is-filled': otp[i] }"
              type="text"
              inputmode="numeric"
              maxlength="1"
              :value="otp[i]"
              @input="handleOtpInput($event, i)"
              @keydown="handleOtpKeydown($event, i)"
              @paste="handleOtpPaste">
          </div>

          <div class="sg-timer-row">
            <span class="sg-timer" :class="{ 'is-urgent': timerSeconds <= 30 }">
              <svg class="sg-timer-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
                <path d="M12 7v5l3 3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              {{ formatTimer(timerSeconds) }}
            </span>
            <button class="sg-resend" :disabled="timerSeconds > 0" @click="resendOtp">
              재전송
            </button>
          </div>

          <button class="qs-btn qs-btn-full qs-btn-primary"
            :disabled="otp.some(d => !d)"
            @click="verifyOtp">
            확인
          </button>
        </template>
      </div>

      <!-- Step 2: Document review -->
      <div v-if="step === 2" class="sg-card">
        <h2 class="sg-card-title">문서 검토 및 서명</h2>
        <p class="sg-card-desc">문서 내용을 확인하고 서명에 동의해 주세요.</p>

        <!-- Doc meta -->
        <div class="sg-doc-meta">
          <div class="sg-doc-meta-name">
            <svg width="24" height="30" viewBox="0 0 24 30" fill="none" aria-hidden="true">
              <rect width="24" height="30" rx="3" fill="var(--color-error-bg)"/>
              <text x="3" y="19" font-size="6" font-weight="800" fill="var(--color-error)"
                font-family="monospace">PDF</text>
            </svg>
            <span class="sg-doc-name">{{ docInfo.name }}</span>
          </div>
          <div class="sg-meta-rows">
            <div class="sg-meta-row">
              <span class="sg-meta-k">요청자</span>
              <span class="sg-meta-v">{{ docInfo.requester }}</span>
            </div>
            <div class="sg-meta-row">
              <span class="sg-meta-k">요청 일시</span>
              <span class="sg-meta-v">{{ docInfo.requestedAt }}</span>
            </div>
            <div class="sg-meta-row">
              <span class="sg-meta-k">만료 일시</span>
              <span class="sg-meta-v">{{ docInfo.expiresAt }}</span>
            </div>
            <div class="sg-meta-row">
              <span class="sg-meta-k">SHA3-256</span>
              <div class="sg-meta-v">
                <span class="sg-meta-v is-mono">
                  {{ hashVisible ? docInfo.hash : docInfo.hash.slice(0, 20) + '...' }}
                </span>
                <button class="sg-hash-toggle" @click="hashVisible = !hashVisible">
                  {{ hashVisible ? '접기' : '전체 보기' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- PDF preview -->
        <div class="sg-pdf-preview">
          <div class="sg-pdf-toolbar">
            <span class="sg-pdf-label">문서 미리보기</span>
            <button class="qs-btn qs-btn-sm qs-btn-secondary" style="height:28px;font-size:12px;border-radius:8px">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                <path d="M8 3H5a2 2 0 0 0-2 2v3M21 8V5a2 2 0 0 0-2-2h-3M3 16v3a2 2 0 0 0 2 2h3M16 21h3a2 2 0 0 0 2-2v-3"
                  stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              전체화면
            </button>
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

        <!-- Consent -->
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

        <button class="qs-btn qs-btn-full qs-btn-primary"
          :disabled="!consent1 || !consent2 || isSigning"
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
              <span class="sg-success-v">{{ email }}</span>
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

          <button class="qs-btn qs-btn-full qs-btn-primary" style="margin-bottom:10px">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"
                stroke="currentColor" stroke-width="2" stroke-linecap="round"
                stroke-linejoin="round"/>
            </svg>
            서명된 PDF 다운로드
          </button>
        </div>
      </div>
    </main>

    <!-- Signing overlay -->
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
import { ref, watch, onUnmounted } from 'vue'
import QuSignMark from '@/components/ui/QuSignMark.vue'
import ThemeToggle from '@/components/ui/ThemeToggle.vue'

const theme = ref<'light' | 'dark'>('light')
const step = ref(1)

// Step 1
const email = ref('')
const emailError = ref(false)
const otpSent = ref(false)
const otp = ref<string[]>(['', '', '', '', '', ''])
const otpRefs = ref<HTMLInputElement[]>([])
const timerSeconds = ref(0)
let timerInterval: ReturnType<typeof setInterval> | null = null

// Step 2
const hashVisible = ref(false)
const consent1 = ref(false)
const consent2 = ref(false)
const isSigning = ref(false)
const signedAt = ref('')

const docInfo = {
  name: '2026년_1분기_계약서.pdf',
  requester: 'admin@qusign.io',
  requestedAt: '2026-05-06 09:14',
  expiresAt: '2026-05-13 09:14',
  hash: 'a3f9e8c12d45b67890feabc1234567890abcdef1234567890abcdef1234567890',
}

watch(theme, (t) => document.documentElement.setAttribute('data-theme', t), { immediate: true })
function handleThemeToggle(t: 'light' | 'dark') { theme.value = t }

function validateEmail() {
  emailError.value = !!email.value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)
}

function sendOtp() {
  validateEmail()
  if (emailError.value || !email.value) return
  otpSent.value = true
  startTimer()
  setTimeout(() => otpRefs.value[0]?.focus(), 100)
}

function startTimer() {
  timerSeconds.value = 180
  if (timerInterval) clearInterval(timerInterval)
  timerInterval = setInterval(() => {
    if (timerSeconds.value > 0) timerSeconds.value--
    else { if (timerInterval) clearInterval(timerInterval) }
  }, 1000)
}

function resendOtp() {
  otp.value = ['', '', '', '', '', '']
  startTimer()
  setTimeout(() => otpRefs.value[0]?.focus(), 100)
}

function formatTimer(s: number) {
  return `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`
}

function handleOtpInput(e: Event, i: number) {
  const v = (e.target as HTMLInputElement).value.replace(/\D/g, '').slice(0, 1)
  otp.value[i] = v
  if (v && i < 5) setTimeout(() => otpRefs.value[i + 1]?.focus(), 0)
}

function handleOtpKeydown(e: KeyboardEvent, i: number) {
  if (e.key === 'Backspace' && !otp.value[i] && i > 0) {
    otp.value[i - 1] = ''
    otpRefs.value[i - 1]?.focus()
  }
}

function handleOtpPaste(e: ClipboardEvent) {
  e.preventDefault()
  const digits = (e.clipboardData?.getData('text') ?? '').replace(/\D/g, '').slice(0, 6)
  digits.split('').forEach((d, i) => { otp.value[i] = d })
  const lastFilled = Math.min(digits.length, 5)
  otpRefs.value[lastFilled]?.focus()
}

function verifyOtp() {
  if (timerInterval) clearInterval(timerInterval)
  step.value = 2
}

async function handleSign() {
  isSigning.value = true
  await new Promise(r => setTimeout(r, 2200))
  isSigning.value = false
  signedAt.value = new Date().toLocaleString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
  step.value = 3
}

onUnmounted(() => { if (timerInterval) clearInterval(timerInterval) })
</script>

<style scoped>
.sg-fade-enter-active, .sg-fade-leave-active { transition: opacity 0.2s; }
.sg-fade-enter-from, .sg-fade-leave-to { opacity: 0; }
@keyframes sg-spin { to { transform: rotate(360deg); } }
</style>
