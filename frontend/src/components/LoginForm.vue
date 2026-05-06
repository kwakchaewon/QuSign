<template>
  <form class="qs-form" @submit.prevent="handleSubmit" novalidate>
    <!-- 이메일 -->
    <div class="qs-field">
      <label class="qs-label" for="qs-email">이메일</label>
      <div :class="['qs-input', emailErr && 'is-error']">
        <span class="qs-input-icon" aria-hidden="true">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <rect x="3" y="5" width="18" height="14" rx="3" stroke="currentColor" stroke-width="1.6" />
            <path d="M4 7l8 6 8-6" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </span>
        <input
          id="qs-email"
          ref="emailInput"
          type="email"
          inputmode="email"
          autocomplete="username"
          placeholder="name@company.com"
          v-model="email"
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
      <label class="qs-label" for="qs-pw">비밀번호</label>
      <div :class="['qs-input', pwErr && 'is-error']">
        <span class="qs-input-icon" aria-hidden="true">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <rect x="4" y="10" width="16" height="11" rx="3" stroke="currentColor" stroke-width="1.6" />
            <path d="M8 10V7a4 4 0 0 1 8 0v3" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
          </svg>
        </span>
        <input
          id="qs-pw"
          :type="showPw ? 'text' : 'password'"
          autocomplete="current-password"
          placeholder="비밀번호 입력"
          v-model="pw"
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
      <span v-if="pwErr" class="qs-help is-error">{{ pwErr }}</span>
    </div>

    <!-- 이메일 기억하기 -->
    <div class="qs-row-between">
      <label class="qs-check">
        <input type="checkbox" v-model="remember" />
        <span class="qs-check-box" aria-hidden="true">
          <svg width="10" height="10" viewBox="0 0 24 24" fill="none">
            <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.8" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </span>
        <span>이메일 기억하기</span>
      </label>
    </div>

    <!-- 에러 알림 -->
    <div v-if="submitErr" class="qs-alert" role="alert">
      <span class="qs-alert-dot" aria-hidden="true" />
      {{ submitErr }}
    </div>

    <!-- 로그인 버튼 -->
    <button
      type="submit"
      :class="['qs-btn', 'qs-btn-primary', 'qs-btn-lg', 'qs-btn-block', { 'is-success': success }]"
      :disabled="loading"
    >
      <template v-if="loading">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" style="animation: qs-spin 0.8s linear infinite">
          <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-opacity="0.25" stroke-width="2.4" />
          <path d="M21 12a9 9 0 0 0-9-9" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" />
        </svg>
        <span>안전하게 연결 중…</span>
      </template>
      <template v-else-if="success">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
          <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        <span>로그인 완료</span>
      </template>
      <template v-else>
        <span>로그인</span>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
          <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
      </template>
    </button>
  </form>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'

const emit = defineEmits<{ login: [email: string] }>()

const emailInput = ref<HTMLInputElement | null>(null)
const email = ref('')
const pw = ref('')
const showPw = ref(false)
const remember = ref(true)
const touched = ref({ email: false, pw: false })
const submitErr = ref<string | null>(null)
const loading = ref(false)
const success = ref(false)

onMounted(() => {
  try {
    const saved = localStorage.getItem('qusign:rememberedEmail')
    if (saved) { email.value = saved; remember.value = true }
  } catch {}
  emailInput.value?.focus()
})

watch([email, pw], () => { submitErr.value = null })

const emailValid = computed(() => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value.trim()))
const pwValid = computed(() => pw.value.length >= 6)

const emailErr = computed(() => {
  if (!touched.value.email || emailValid.value) return ''
  return email.value.length === 0 ? '이메일을 입력해 주세요' : '이메일 형식이 올바르지 않아요'
})

const pwErr = computed(() => {
  if (!touched.value.pw || pwValid.value) return ''
  return pw.value.length === 0 ? '비밀번호를 입력해 주세요' : '6자 이상 입력해 주세요'
})

function handleSubmit() {
  touched.value = { email: true, pw: true }
  submitErr.value = null
  if (!emailValid.value || !pwValid.value) return

  loading.value = true
  setTimeout(() => {
    if (pw.value === 'wrong!1') {
      loading.value = false
      submitErr.value = '이메일 또는 비밀번호가 올바르지 않아요'
      return
    }
    try {
      if (remember.value) localStorage.setItem('qusign:rememberedEmail', email.value.trim())
      else localStorage.removeItem('qusign:rememberedEmail')
    } catch {}
    loading.value = false
    success.value = true
    emit('login', email.value.trim())
    setTimeout(() => { success.value = false }, 2200)
  }, 1100)
}
</script>
