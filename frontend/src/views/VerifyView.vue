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

          <div class="vf-tabs">
            <button class="vf-tab" :class="{ 'is-active': mode === 'upload' }" @click="mode = 'upload'">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12"
                  stroke="currentColor" stroke-width="2" stroke-linecap="round"
                  stroke-linejoin="round"/>
              </svg>
              PDF 업로드
            </button>
            <button class="vf-tab" :class="{ 'is-active': mode === 'token' }" @click="mode = 'token'">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"
                  stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"
                  stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              서명 토큰
            </button>
          </div>
        </div>

        <div class="vf-card-body">
          <!-- Upload mode -->
          <div v-if="mode === 'upload'">
            <div class="vf-drop"
              :class="{ 'is-drag': isDrag }"
              @dragover.prevent="isDrag = true"
              @dragleave.prevent="isDrag = false"
              @drop.prevent="handleDrop"
              @click="fileInput?.click()">
              <input ref="fileInput" type="file" accept=".pdf" style="display:none"
                @change="handleFileChange">
              <div class="vf-drop-icon">
                <svg width="40" height="40" viewBox="0 0 40 40" fill="none" aria-hidden="true">
                  <rect width="40" height="40" rx="10" fill="var(--color-error-bg)"/>
                  <path d="M12 18h16M12 24h10" stroke="var(--color-error)"
                    stroke-width="1.5" stroke-linecap="round"/>
                  <text x="8" y="35" font-size="6" font-weight="800" fill="var(--color-error)"
                    font-family="monospace">PDF</text>
                </svg>
              </div>
              <p class="vf-drop-title">PDF를 여기에 드래그하거나</p>
              <p class="vf-drop-sub">클릭하여 파일 선택 · PDF 형식만 지원</p>
              <span class="vf-drop-pill">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12"
                    stroke="currentColor" stroke-width="2" stroke-linecap="round"
                    stroke-linejoin="round"/>
                </svg>
                파일 선택
              </span>
            </div>
          </div>

          <!-- Token mode -->
          <div v-else class="vf-token-section">
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
                  placeholder="예: abc123de">
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
          <div class="vf-steps-list">
            <div class="vf-step-item" :class="loadStep >= 1 ? (loadStep > 1 ? 'is-done' : 'is-active') : ''">
              <span class="vf-step-dot"></span>
              SHA3-256 해시 추출
            </div>
            <div class="vf-step-item" :class="loadStep >= 2 ? (loadStep > 2 ? 'is-done' : 'is-active') : ''">
              <span class="vf-step-dot"></span>
              서명값 확인
            </div>
            <div class="vf-step-item" :class="loadStep >= 3 ? (loadStep > 3 ? 'is-done' : 'is-active') : ''">
              <span class="vf-step-dot"></span>
              공개키 검증
            </div>
          </div>
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
                <span class="vf-detail-v">signer@example.com</span>
              </div>
              <div class="vf-detail-row">
                <span class="vf-detail-k">서명 일시</span>
                <span class="vf-detail-v">2026-05-06 14:32:11</span>
              </div>
              <div class="vf-detail-row">
                <span class="vf-detail-k">알고리즘</span>
                <span class="vf-detail-v">
                  <span class="vf-detail-tag">ML-DSA-65</span>
                </span>
              </div>
              <div class="vf-detail-row">
                <span class="vf-detail-k">문서 해시</span>
                <span class="vf-detail-v is-mono">a3f9e8c12d45b67890feabc1234567890abcdef1234567890abcdef1234567890</span>
              </div>
              <div class="vf-detail-row">
                <span class="vf-detail-k">서명값</span>
                <span class="vf-detail-v is-mono">4d2f9c3a8e1b7f06...</span>
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

type Status = 'idle' | 'loading' | 'success' | 'fail'

const theme = ref<'light' | 'dark'>('light')
const status = ref<Status>('idle')
const mode = ref<'upload' | 'token'>('upload')
const isDrag = ref(false)
const token = ref('')
const detailsOpen = ref(false)
const loadStep = ref(0)
const fileInput = ref<HTMLInputElement | null>(null)
const failReason = ref('서명값 불일치')

watch(theme, (t) => document.documentElement.setAttribute('data-theme', t), { immediate: true })
function handleThemeToggle(t: 'light' | 'dark') { theme.value = t }

function handleDrop(e: DragEvent) {
  isDrag.value = false
  const f = e.dataTransfer?.files[0]
  if (f?.type === 'application/pdf') startVerify()
}
function handleFileChange(e: Event) {
  if ((e.target as HTMLInputElement).files?.[0]) startVerify()
}

async function startVerify() {
  status.value = 'loading'
  loadStep.value = 0

  await tick(700);  loadStep.value = 1
  await tick(700);  loadStep.value = 2
  await tick(700);  loadStep.value = 3
  await tick(400)

  // Simulate: 80% success
  status.value = Math.random() > 0.2 ? 'success' : 'fail'
  if (status.value === 'fail') {
    const reasons = ['서명값 불일치', '해시 불일치', 'QuSign 서명 아님', '만료된 서명']
    failReason.value = reasons[Math.floor(Math.random() * reasons.length)]
  }
}

function tick(ms: number) { return new Promise(r => setTimeout(r, ms)) }

function reset() {
  status.value = 'idle'
  token.value = ''
  loadStep.value = 0
  detailsOpen.value = false
  if (fileInput.value) fileInput.value.value = ''
}
</script>
