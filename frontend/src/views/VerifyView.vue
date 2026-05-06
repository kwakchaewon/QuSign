<template>
  <div class="vf-page">
    <header class="vf-header">
      <div class="vf-header-inner">
        <div class="vf-logo">
          <QuSignMark variant="badge" :size="24" />
          <span class="vf-logo-name">QuSign</span>
        </div>
        <div class="vf-header-center">
          <span class="vf-header-title">무결성 검증</span>
        </div>
        <div class="vf-header-right">
          <div class="vf-pqc">
            <span class="vf-pqc-dot"></span>
            ML-DSA-65
          </div>
          <ThemeToggle :theme="theme" @change="handleThemeToggle" />
        </div>
      </div>
    </header>

    <main class="vf-main">
      <!-- Idle state -->
      <div v-if="status === 'idle'" class="vf-card">
        <div class="vf-card-head">
          <h1 class="vf-card-title">서명된 PDF를 검증해 보세요</h1>
          <p class="vf-card-desc">
            QuSign으로 서명된 문서의 진위 여부와 변조 여부를 확인합니다.
          </p>
        </div>

        <div class="vf-card-body">
          <div class="vf-token-section">
            <div class="vf-field">
              <label class="vf-label" for="vf-token">서명 토큰</label>
              <div class="vf-input">
                <span class="vf-input-icon">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                    <path d="M15 7h3a5 5 0 0 1 5 5 5 5 0 0 1-5 5h-3m-6 0H6a5 5 0 0 1-5-5 5 5 0 0 1 5-5h3"
                      stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    <line x1="8" y1="12" x2="16" y2="12" stroke="currentColor" stroke-width="2"
                      stroke-linecap="round"/>
                  </svg>
                </span>
                <input id="vf-token" v-model="token" type="text"
                  placeholder="서명 토큰 입력"
                  @keydown.enter="startVerify">
              </div>
            </div>
            <button class="vf-btn vf-btn-primary"
              :disabled="!token.trim()"
              @click="startVerify">
              검증하기
            </button>
          </div>
        </div>
      </div>

      <!-- Loading state -->
      <div v-else-if="status === 'loading'" class="vf-card">
        <div class="vf-loading">
          <div class="vf-spinner"></div>
          <p class="vf-loading-title">검증 중...</p>
          <p class="vf-loading-sub">ML-DSA-65 서명값을 검증하고 있습니다</p>
        </div>
      </div>

      <!-- Success state -->
      <div v-else-if="status === 'success'" class="vf-card is-success">
        <div class="vf-result">
          <div class="vf-result-icon">
            <div class="vf-result-check">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M20 6L9 17l-5-5" stroke="var(--color-success)" stroke-width="3"
                  stroke-linecap="round" stroke-linejoin="round"
                  stroke-dasharray="30" stroke-dashoffset="30"
                  style="animation:qs-draw 0.5s 0.15s ease forwards"/>
              </svg>
            </div>
          </div>
          <h2 class="vf-result-title is-success">검증 완료</h2>
          <p class="vf-result-desc">이 문서는 변조되지 않았습니다.</p>

          <div class="vf-details">
            <button class="vf-details-toggle" @click="detailsOpen = !detailsOpen">
              세부 정보
              <svg class="vf-details-toggle-icon" :class="{ 'is-open': detailsOpen }"
                width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2"
                  stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
            <div v-if="detailsOpen" class="vf-details-body">
              <div class="vf-detail-row">
                <span class="vf-detail-k">서명자</span>
                <span class="vf-detail-v">{{ verifyResult.signerId }}</span>
              </div>
              <div class="vf-detail-row">
                <span class="vf-detail-k">서명 일시</span>
                <span class="vf-detail-v">{{ verifyResult.signedAt }}</span>
              </div>
              <div class="vf-detail-row">
                <span class="vf-detail-k">알고리즘</span>
                <span class="vf-detail-v">
                  <span class="vf-detail-tag">ML-DSA-65</span>
                </span>
              </div>
              <div class="vf-detail-row">
                <span class="vf-detail-k">문서 해시</span>
                <span class="vf-detail-v is-mono">{{ verifyResult.documentHash }}</span>
              </div>
            </div>
          </div>

          <div class="vf-result-actions">
            <button class="vf-btn vf-btn-secondary" @click="reset">다시 검증하기</button>
          </div>
        </div>
      </div>

      <!-- Fail state -->
      <div v-else-if="status === 'fail'" class="vf-card is-fail">
        <div class="vf-result">
          <div class="vf-result-icon">
            <div class="vf-result-x">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M18 6L6 18M6 6l12 12" stroke="var(--color-error)" stroke-width="3"
                  stroke-linecap="round"/>
              </svg>
            </div>
          </div>
          <h2 class="vf-result-title is-fail">검증 실패</h2>
          <p class="vf-result-desc">이 문서가 변조되었거나 QuSign 서명이 아닙니다.</p>
          <div class="vf-fail-reason">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2"
                stroke-linecap="round"/>
            </svg>
            {{ failReason }}
          </div>
          <div class="vf-result-actions">
            <button class="vf-btn vf-btn-secondary" @click="reset">다시 검증하기</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import QuSignMark from '@/components/ui/QuSignMark.vue'
import ThemeToggle from '@/components/ui/ThemeToggle.vue'
import api from '@/lib/api'

type Status = 'idle' | 'loading' | 'success' | 'fail'

interface VerifyResult {
  valid: boolean
  signerId: string
  signedAt: string
  documentHash: string
}

const theme = ref<'light' | 'dark'>('light')
const status = ref<Status>('idle')
const token = ref('')
const detailsOpen = ref(false)
const failReason = ref('')
const verifyResult = ref<VerifyResult>({ valid: false, signerId: '', signedAt: '', documentHash: '' })

watch(theme, (t) => document.documentElement.setAttribute('data-theme', t), { immediate: true })
function handleThemeToggle(t: 'light' | 'dark') { theme.value = t }

async function startVerify() {
  if (!token.value.trim()) return
  status.value = 'loading'
  try {
    const res = await api.post<{ data: VerifyResult }>('/api/verify', { token: token.value.trim() })
    const result = res.data.data
    if (result.valid) {
      verifyResult.value = result
      status.value = 'success'
    } else {
      failReason.value = '서명값 불일치'
      status.value = 'fail'
    }
  } catch (err: any) {
    failReason.value = err.response?.data?.message ?? '검증에 실패했어요. 토큰을 확인해 주세요.'
    status.value = 'fail'
  }
}

function reset() {
  status.value = 'idle'
  token.value = ''
  detailsOpen.value = false
  verifyResult.value = { valid: false, signerId: '', signedAt: '', documentHash: '' }
  failReason.value = ''
}
</script>
