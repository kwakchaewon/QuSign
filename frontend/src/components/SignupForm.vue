<template>
  <form class="qs-form" @submit.prevent="handleSubmit" novalidate>
    <!-- 이메일 -->
    <div class="qs-field">
      <label class="qs-label" for="qs-su-email">이메일</label>
      <div :class="['qs-input', emailErr && 'is-error']">
        <span class="qs-input-icon" aria-hidden="true">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <rect x="3" y="5" width="18" height="14" rx="3" stroke="currentColor" stroke-width="1.6" />
            <path d="M4 7l8 6 8-6" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </span>
        <input
          id="qs-su-email"
          ref="emailInput"
          type="email"
          inputmode="email"
          autocomplete="email"
          placeholder="name@company.com"
          v-model="email"
          :disabled="isLoading || isDone"
          @blur="touched.email = true"
        />
        <span v-if="email && emailValid" class="qs-input-tick" aria-hidden="true">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </span>
      </div>
      <span v-if="emailErr" class="qs-help is-error">{{ emailErr }}</span>
    </div>

    <!-- 비밀번호 -->
    <div class="qs-field">
      <label class="qs-label" for="qs-su-pw">비밀번호</label>
      <div :class="['qs-input', pwErr && 'is-error']">
        <span class="qs-input-icon" aria-hidden="true">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <rect x="4" y="10" width="16" height="11" rx="3" stroke="currentColor" stroke-width="1.6" />
            <path d="M8 10V7a4 4 0 0 1 8 0v3" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
          </svg>
        </span>
        <input
          id="qs-su-pw"
          :type="showPw ? 'text' : 'password'"
          autocomplete="new-password"
          placeholder="8자 이상"
          v-model="pw"
          :disabled="isLoading || isDone"
          @blur="touched.pw = true"
        />
        <button
          type="button"
          class="qs-input-eye"
          :aria-label="showPw ? '비밀번호 숨기기' : '비밀번호 표시'"
          @click="showPw = !showPw"
        >
          <svg v-if="showPw" width="18" height="18" viewBox="0 0 24 24" fill="none">
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

      <!-- 강도 미터 -->
      <div v-if="pw" :class="['qs-strength', `qs-strength-${strength.tier}`]">
        <div class="qs-strength-bars">
          <span v-for="i in 3" :key="i" :class="['qs-strength-bar', i <= strength.fillCount ? 'is-on' : '']" />
        </div>
        <span class="qs-strength-label">{{ strength.label }}</span>
      </div>

      <span v-if="pwErr" class="qs-help is-error">{{ pwErr }}</span>
    </div>

    <!-- 비밀번호 확인 -->
    <div class="qs-field">
      <label class="qs-label" for="qs-su-pw2">비밀번호 확인</label>
      <div :class="['qs-input', pw2Err && 'is-error']">
        <span class="qs-input-icon" aria-hidden="true">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <rect x="4" y="10" width="16" height="11" rx="3" stroke="currentColor" stroke-width="1.6" />
            <path d="M8 10V7a4 4 0 0 1 8 0v3" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
          </svg>
        </span>
        <input
          id="qs-su-pw2"
          :type="showPw2 ? 'text' : 'password'"
          autocomplete="new-password"
          placeholder="비밀번호 한 번 더 입력"
          v-model="pw2"
          :disabled="isLoading || isDone"
          @blur="touched.pw2 = true"
        />
        <span v-if="pw2Match" class="qs-input-tick" aria-hidden="true">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </span>
        <button
          type="button"
          class="qs-input-eye"
          :aria-label="showPw2 ? '비밀번호 숨기기' : '비밀번호 표시'"
          @click="showPw2 = !showPw2"
        >
          <svg v-if="showPw2" width="18" height="18" viewBox="0 0 24 24" fill="none">
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
      <span v-if="pw2Err" class="qs-help is-error">{{ pw2Err }}</span>
    </div>

    <!-- 약관 동의 -->
    <label class="qs-check qs-check-block">
      <input type="checkbox" v-model="agree" :disabled="isLoading || isDone" />
      <span class="qs-check-box" aria-hidden="true">
        <svg width="10" height="10" viewBox="0 0 24 24" fill="none">
          <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.8" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
      </span>
      <span>
        <a href="#" @click.prevent>이용약관</a> 및
        <a href="#" @click.prevent>개인정보처리방침</a>에 동의해요
      </span>
    </label>

    <!-- 에러 알림 -->
    <div v-if="submitErr" class="qs-alert" role="alert">
      <span class="qs-alert-dot" aria-hidden="true" />
      {{ submitErr }}
    </div>

    <!-- 제출 버튼 -->
    <button
      type="submit"
      :class="['qs-btn', 'qs-btn-primary', 'qs-btn-lg', 'qs-btn-block', { 'is-success': isDone }]"
      :disabled="isLoading || isDone"
    >
      <template v-if="isLoading">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" style="animation: qs-spin 0.8s linear infinite">
          <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-opacity="0.25" stroke-width="2.4" />
          <path d="M21 12a9 9 0 0 0-9-9" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" />
        </svg>
        <span>ML-DSA 키쌍 생성 중…</span>
      </template>
      <template v-else-if="isDone">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
          <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        <span>키쌍 생성 완료</span>
      </template>
      <template v-else>
        <span>계정 만들기</span>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
          <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
      </template>
    </button>
  </form>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'

const emit = defineEmits<{ success: [email: string] }>()

const emailInput = ref<HTMLInputElement | null>(null)
const email = ref('')
const pw = ref('')
const pw2 = ref('')
const showPw = ref(false)
const showPw2 = ref(false)
const agree = ref(true)
const touched = ref({ email: false, pw: false, pw2: false })
const submitErr = ref<string | null>(null)
const phase = ref<'idle' | 'generating' | 'done'>('idle')

onMounted(() => { emailInput.value?.focus() })
watch([email, pw, pw2], () => { submitErr.value = null })

const isLoading = computed(() => phase.value === 'generating')
const isDone = computed(() => phase.value === 'done')

const emailValid = computed(() => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value.trim()))
const pwValid = computed(() => pw.value.length >= 8)
const pw2Match = computed(() => pw2.value.length > 0 && pw.value === pw2.value)

const strength = computed(() => {
  const p = pw.value
  if (!p) return { tier: 'none', label: '', fillCount: 0 }
  let score = 0
  if (p.length >= 8) score++
  if (p.length >= 12) score++
  if (/[a-z]/.test(p) && /[A-Z]/.test(p)) score++
  if (/\d/.test(p)) score++
  if (/[^A-Za-z0-9]/.test(p)) score++
  if (score >= 4) return { tier: 'strong', label: '강함', fillCount: 3 }
  if (score >= 2) return { tier: 'fair', label: '보통', fillCount: 2 }
  return { tier: 'weak', label: '약함', fillCount: 1 }
})

const emailErr = computed(() => {
  if (!touched.value.email || emailValid.value) return ''
  return email.value.length === 0 ? '이메일을 입력해 주세요' : '이메일 형식이 올바르지 않아요'
})
const pwErr = computed(() => {
  if (!touched.value.pw || pwValid.value) return ''
  return pw.value.length === 0 ? '비밀번호를 입력해 주세요' : '8자 이상 입력해 주세요'
})
const pw2Err = computed(() => {
  if (!touched.value.pw2 || pw2Match.value) return ''
  return pw2.value.length === 0 ? '비밀번호를 한 번 더 입력해 주세요' : '비밀번호가 일치하지 않아요'
})

function handleSubmit() {
  touched.value = { email: true, pw: true, pw2: true }
  submitErr.value = null
  if (!emailValid.value || !pwValid.value || !pw2Match.value) return
  if (!agree.value) { submitErr.value = '약관에 동의해 주세요'; return }
  phase.value = 'generating'
  setTimeout(() => {
    phase.value = 'done'
    emit('success', email.value.trim())
  }, 2000)
}
</script>
