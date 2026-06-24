<template>
  <div class="vf-page">
    <AppTopbar v-if="auth.isLoggedIn" />
    <PublicTopbar v-else subtitle="무결성 검증" logo-to="/verify" show-pqc-badge />

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
          <div class="vf-drop"
              :class="{ 'is-drag': isDragging }"
              @click="fileInput?.click()"
              @dragover.prevent="isDragging = true"
              @dragleave.prevent="isDragging = false"
              @drop.prevent="handleDrop">
              <div class="vf-drop-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"
                    stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                  <polyline points="14 2 14 8 20 8" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                </svg>
              </div>
              <template v-if="selectedFile">
                <p class="vf-drop-title">{{ selectedFile.name }}</p>
                <p class="vf-drop-sub">다른 파일을 선택하려면 클릭하세요</p>
              </template>
              <template v-else>
                <p class="vf-drop-title">서명된 PDF를 여기에 드래그하세요</p>
                <p class="vf-drop-sub">또는 클릭하여 파일 선택 · PDF만 가능</p>
                <span class="vf-drop-pill">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    <polyline points="17 8 12 3 7 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <line x1="12" y1="3" x2="12" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                  파일 선택
                </span>
              </template>
            </div>
            <input ref="fileInput" type="file" accept=".pdf" style="display:none" @change="handleFileChange">
            <button v-if="selectedFile"
              class="vf-btn vf-btn-primary"
              style="margin-top:14px"
              @click="startVerifyFile">
              검증하기
            </button>
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
            <button class="vf-btn vf-btn-primary" @click="reset">다시 검증하기</button>
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
            <button class="vf-btn vf-btn-primary" @click="reset">다시 검증하기</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import api from '@/lib/api'
import PublicTopbar from '@/components/layout/PublicTopbar.vue'
import AppTopbar from '@/components/layout/AppTopbar.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

type Status = 'idle' | 'loading' | 'success' | 'fail'

interface VerifyResult {
  valid: boolean
  signerId: string
  signedAt: string
  documentHash: string
}

const status = ref<Status>('idle')
const selectedFile = ref<File | null>(null)
const isDragging = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const detailsOpen = ref(false)
const failReason = ref('')
const verifyResult = ref<VerifyResult>({ valid: false, signerId: '', signedAt: '', documentHash: '' })

function handleDrop(e: DragEvent) {
  isDragging.value = false
  const file = e.dataTransfer?.files[0]
  if (file?.type === 'application/pdf') selectedFile.value = file
}

function handleFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (file) selectedFile.value = file
}

async function startVerifyFile() {
  if (!selectedFile.value) return
  status.value = 'loading'
  try {
    const form = new FormData()
    form.append('file', selectedFile.value)
    const res = await api.post<{ data: VerifyResult }>('/api/verify/file', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    handleResult(res.data.data, 'PDF를 확인해 주세요.')
  } catch (err: any) {
    failReason.value = err.response?.data?.message ?? '검증에 실패했어요. PDF를 확인해 주세요.'
    status.value = 'fail'
  }
}

function handleResult(result: VerifyResult, fallbackMsg: string) {
  if (result.valid) {
    verifyResult.value = result
    status.value = 'success'
  } else {
    failReason.value = `서명값 불일치 — ${fallbackMsg}`
    status.value = 'fail'
  }
}

function reset() {
  status.value = 'idle'
  selectedFile.value = null
  isDragging.value = false
  detailsOpen.value = false
  verifyResult.value = { valid: false, signerId: '', signedAt: '', documentHash: '' }
  failReason.value = ''
}
</script>
