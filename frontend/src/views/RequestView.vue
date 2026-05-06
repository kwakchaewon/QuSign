<template>
  <div class="qs-page qs-rq-page">
    <header class="qs-topbar">
      <div class="qs-topbar-inner">
        <div class="qs-topbar-brand">
          <QuSignMark variant="badge" :size="28" />
          <span class="qs-topbar-name">서명 요청</span>
        </div>
        <div class="qs-topbar-right">
          <span class="qs-user-email">{{ userEmail }}</span>
          <ThemeToggle :theme="theme" @change="handleThemeToggle" />
          <button class="qs-icon-btn" title="문서 목록" @click="router.push('/documents')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M19 12H5M12 5l-7 7 7 7" stroke="currentColor" stroke-width="2"
                stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>
    </header>

    <main class="qs-main">
      <ol class="qs-steps" aria-label="진행 단계">
        <li class="qs-step" :class="{ 'is-active': step === 1, 'is-done': step > 1 }">
          <span class="qs-step-bullet">
            <svg v-if="step > 1" width="12" height="12" viewBox="0 0 24 24" fill="none">
              <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="3"
                stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <template v-else>1</template>
          </span>
          <span class="qs-step-label">PDF 업로드</span>
        </li>
        <span class="qs-step-line" :class="{ 'is-done': step > 1 }"></span>
        <li class="qs-step" :class="{ 'is-active': step === 2, 'is-done': step > 2 }">
          <span class="qs-step-bullet">
            <svg v-if="step > 2" width="12" height="12" viewBox="0 0 24 24" fill="none">
              <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="3"
                stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <template v-else>2</template>
          </span>
          <span class="qs-step-label">서명 요청 설정</span>
        </li>
        <span class="qs-step-line" :class="{ 'is-done': step > 2 }"></span>
        <li class="qs-step" :class="{ 'is-active': step === 3 }">
          <span class="qs-step-bullet">3</span>
          <span class="qs-step-label">완료</span>
        </li>
      </ol>

      <!-- Step 1: Upload -->
      <div v-if="step === 1" class="qs-step-card">
        <div class="qs-step-body">
          <h2 class="qs-step-title">PDF 업로드</h2>
          <p class="qs-step-desc">서명 요청할 PDF 파일을 업로드하세요. 최대 50MB까지 지원합니다.</p>

          <div v-if="!file"
            class="qs-drop"
            :class="{ 'is-drag': isDrag }"
            @dragover.prevent="isDrag = true"
            @dragleave.prevent="isDrag = false"
            @drop.prevent="handleDrop"
            @click="fileInput?.click()">
            <input ref="fileInput" type="file" accept=".pdf" style="display:none"
              @change="handleFileChange">
            <div class="qs-drop-icon">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none" aria-hidden="true">
                <rect width="48" height="48" rx="12" fill="var(--color-error-bg)"/>
                <path d="M16 20h16M16 26h10M16 32h12" stroke="var(--color-error)"
                  stroke-width="2" stroke-linecap="round"/>
                <text x="12" y="42" font-size="8" font-weight="800" fill="var(--color-error)"
                  font-family="monospace">PDF</text>
              </svg>
            </div>
            <p class="qs-drop-title">PDF를 여기에 드래그하거나</p>
            <p class="qs-drop-sub">클릭하여 파일 선택 · 최대 50MB</p>
            <span class="qs-drop-pill">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5"
                  stroke-linecap="round"/>
              </svg>
              파일 선택
            </span>
          </div>

          <div v-else class="qs-file">
            <div class="qs-file-icon">
              <svg width="28" height="36" viewBox="0 0 28 36" fill="none">
                <rect width="28" height="36" rx="4" fill="var(--color-error-bg)"/>
                <text x="4" y="22" font-size="7" font-weight="800" fill="var(--color-error)"
                  font-family="monospace">PDF</text>
              </svg>
            </div>
            <div class="qs-file-main">
              <div class="qs-file-name">{{ file.name }}</div>
              <div class="qs-file-meta">
                <span>{{ formatSize(file.size) }}</span>
                <span class="qs-dot-sep">·</span>
                <span class="qs-file-hash-state" :class="hashDone ? 'is-done' : 'is-progress'">
                  <svg v-if="!hashDone" width="12" height="12" viewBox="0 0 24 24" fill="none"
                    style="animation:sg-spin 0.8s linear infinite">
                    <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"
                      stroke-dasharray="20 40"/>
                  </svg>
                  <svg v-else width="12" height="12" viewBox="0 0 24 24" fill="none">
                    <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2.5"
                      stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  {{ hashDone ? 'SHA3-256 완료' : '해시 계산 중...' }}
                </span>
              </div>
              <div v-if="hashDone" class="qs-file-hash">
                <span class="qs-hash-label">SHA3-256</span>
                <span class="qs-hash-value">{{ hash }}</span>
              </div>
            </div>
            <button class="qs-btn qs-btn-sm qs-btn-ghost" @click="file = null; hash = ''; hashDone = false">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2"
                  stroke-linecap="round"/>
              </svg>
            </button>
          </div>
        </div>
        <div class="qs-step-foot">
          <button class="qs-btn qs-btn-sm qs-btn-ghost" @click="router.push('/documents')">
            취소
          </button>
          <button class="qs-btn qs-btn-md qs-btn-primary"
            :disabled="!file || !hashDone" @click="step = 2">
            다음 단계
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2"
                stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- Step 2: Signer config -->
      <div v-if="step === 2" class="qs-step-card">
        <div class="qs-step-body">
          <h2 class="qs-step-title">서명 요청 설정</h2>

          <!-- File summary -->
          <div class="qs-file qs-file-summary">
            <div class="qs-file-icon">
              <svg width="28" height="36" viewBox="0 0 28 36" fill="none">
                <rect width="28" height="36" rx="4" fill="var(--color-error-bg)"/>
                <text x="4" y="22" font-size="7" font-weight="800" fill="var(--color-error)"
                  font-family="monospace">PDF</text>
              </svg>
            </div>
            <div class="qs-file-main">
              <div class="qs-file-name">{{ file?.name }}</div>
              <div class="qs-file-meta">
                <span>{{ formatSize(file?.size ?? 0) }}</span>
                <span class="qs-dot-sep">·</span>
                <span class="qs-badge qs-badge-success">
                  <span class="qs-badge-dot" style="background:var(--color-success)"></span>
                  SHA3-256 완료
                </span>
              </div>
            </div>
          </div>

          <!-- Signers -->
          <div class="qs-section">
            <div class="qs-section-head">
              <h3 class="qs-section-title">서명자</h3>
              <span class="qs-section-meta">{{ signers.length }}/5명</span>
            </div>

            <div v-for="(signer, i) in signers" :key="i" class="qs-signer"
              :class="{ 'is-error': signer.error }">
              <div class="qs-signer-num">{{ i + 1 }}</div>
              <div class="qs-signer-fields">
                <div class="qs-field">
                  <label class="qs-label">이메일</label>
                  <div class="qs-input" :class="{ 'is-error': signer.error }">
                    <span class="qs-input-icon">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                        <rect x="2" y="4" width="20" height="16" rx="2" stroke="currentColor"
                          stroke-width="2"/>
                        <path d="m2 7 10 7 10-7" stroke="currentColor" stroke-width="2"/>
                      </svg>
                    </span>
                    <input v-model="signer.email" type="email"
                      placeholder="signer@example.com"
                      @blur="validateSigner(i)">
                  </div>
                  <span v-if="signer.error" class="qs-help is-error">올바른 이메일 형식이 아닙니다</span>
                </div>
                <div class="qs-field">
                  <label class="qs-label">이름 <span class="qs-label-opt">(선택)</span></label>
                  <div class="qs-input">
                    <input v-model="signer.name" type="text" placeholder="홍길동">
                  </div>
                </div>
              </div>
              <button v-if="signers.length > 1" class="qs-btn qs-btn-sm qs-btn-ghost qs-signer-remove"
                @click="signers.splice(i, 1)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                  <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2"
                    stroke-linecap="round"/>
                </svg>
              </button>
            </div>

            <button v-if="signers.length < 5" class="qs-add-btn" @click="addSigner">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5"
                  stroke-linecap="round"/>
              </svg>
              서명자 추가
              <span class="qs-add-hint">최대 5명</span>
            </button>
          </div>

          <!-- Message -->
          <div class="qs-section">
            <h3 class="qs-section-title">
              메시지 <span class="qs-label-opt">선택</span>
            </h3>
            <textarea class="qs-textarea" v-model="message"
              placeholder="서명자에게 전달할 메시지를 입력하세요..."></textarea>
          </div>
        </div>

        <div class="qs-step-foot">
          <button class="qs-btn qs-btn-sm qs-btn-secondary" @click="step = 1">이전</button>
          <button class="qs-btn qs-btn-md qs-btn-primary"
            :disabled="!canSubmit || isSending" @click="handleSubmit">
            <svg v-if="isSending" width="16" height="16" viewBox="0 0 24 24" fill="none"
              style="animation:sg-spin 0.8s linear infinite">
              <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"
                stroke-dasharray="20 40"/>
            </svg>
            {{ isSending ? '전송 중...' : '서명 요청 보내기' }}
          </button>
        </div>
      </div>

      <!-- Step 3: Done -->
      <div v-if="step === 3" class="qs-step-card">
        <div class="qs-step-body qs-done">
          <div class="qs-done-icon">
            <svg width="72" height="72" viewBox="0 0 72 72" fill="none" aria-hidden="true">
              <circle cx="36" cy="36" r="36" fill="var(--color-success-bg)"/>
              <path d="M22 36l10 10 18-20" stroke="var(--color-success)" stroke-width="4"
                stroke-linecap="round" stroke-linejoin="round"
                stroke-dasharray="30" stroke-dashoffset="30"
                style="animation:qs-draw 0.5s 0.2s ease forwards"/>
            </svg>
          </div>
          <h2 class="qs-done-title">서명 요청이 전송되었습니다</h2>
          <p class="qs-done-desc">
            {{ signers.map(s => s.email).join(', ') }}에게<br>
            서명 요청 이메일이 발송되었습니다.
          </p>

          <div class="qs-done-meta">
            <div class="qs-done-row">
              <span class="qs-done-k">파일명</span>
              <span class="qs-done-v">{{ file?.name }}</span>
            </div>
            <div class="qs-done-row">
              <span class="qs-done-k">서명자</span>
              <span class="qs-done-v">{{ signers.length }}명</span>
            </div>
            <div class="qs-done-row">
              <span class="qs-done-k">알고리즘</span>
              <span class="qs-done-v qs-mono">ML-DSA-65</span>
            </div>
          </div>

          <div class="qs-link-box">
            <p class="qs-link-label">서명 링크</p>
            <div class="qs-link-row">
              <span class="qs-link-url">{{ signingLink }}</span>
              <button class="qs-btn qs-btn-sm qs-btn-secondary"
                :class="{ 'is-success': copied }" @click="copyLink">
                <svg v-if="!copied" width="14" height="14" viewBox="0 0 24 24" fill="none">
                  <rect x="8" y="8" width="13" height="13" rx="2" stroke="currentColor" stroke-width="2"/>
                  <path d="M4 16H3a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h11a2 2 0 0 1 2 2v1"
                    stroke="currentColor" stroke-width="2"/>
                </svg>
                <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none">
                  <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2.5"
                    stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                {{ copied ? '복사됨' : '복사' }}
              </button>
            </div>
          </div>

          <div class="qs-done-actions">
            <RouterLink to="/documents" class="qs-btn qs-btn-md qs-btn-secondary">
              문서 목록으로
            </RouterLink>
            <button class="qs-btn qs-btn-md qs-btn-primary" @click="resetForm">
              새 문서 요청
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import QuSignMark from '@/components/ui/QuSignMark.vue'
import ThemeToggle from '@/components/ui/ThemeToggle.vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/lib/api'

interface Signer { email: string; name: string; error: boolean }

const router = useRouter()
const auth = useAuthStore()
const theme = ref<'light' | 'dark'>('light')
const step = ref(1)
const userEmail = computed(() => auth.email ?? '')

const fileInput = ref<HTMLInputElement | null>(null)
const file = ref<File | null>(null)
const isDrag = ref(false)
const isUploading = ref(false)
const hashDone = ref(false)
const hash = ref('')
const uploadedDocId = ref<number | null>(null)

const signers = ref<Signer[]>([{ email: '', name: '', error: false }])
const message = ref('')
const isSending = ref(false)
const signingLinks = ref<string[]>([])
const copied = ref(false)
const submitError = ref('')

watch(theme, (t) => document.documentElement.setAttribute('data-theme', t), { immediate: true })

function handleThemeToggle(t: 'light' | 'dark') { theme.value = t }

function handleDrop(e: DragEvent) {
  isDrag.value = false
  const f = e.dataTransfer?.files[0]
  if (f?.type === 'application/pdf') setFile(f)
}
function handleFileChange(e: Event) {
  const f = (e.target as HTMLInputElement).files?.[0]
  if (f) setFile(f)
}
async function setFile(f: File) {
  file.value = f
  hashDone.value = false
  hash.value = ''
  uploadedDocId.value = null
  isUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', f)
    const res = await api.post<{ data: { id: number; hashSha3256: string } }>('/api/documents', formData)
    uploadedDocId.value = res.data.data.id
    hash.value = res.data.data.hashSha3256
    hashDone.value = true
  } catch (err: any) {
    file.value = null
    alert(err.response?.data?.message ?? '파일 업로드에 실패했어요.')
  } finally {
    isUploading.value = false
  }
}
function formatSize(bytes: number) {
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

function addSigner() { signers.value.push({ email: '', name: '', error: false }) }
function validateSigner(i: number) {
  const s = signers.value[i]
  s.error = !!s.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(s.email)
}

const canSubmit = computed(() =>
  signers.value.length > 0 &&
  signers.value.every(s => s.email && !s.error) &&
  !signers.value.some(s => s.error)
)

async function handleSubmit() {
  signers.value.forEach((_, i) => validateSigner(i))
  if (!canSubmit.value || !uploadedDocId.value) return
  isSending.value = true
  submitError.value = ''
  try {
    const results = await Promise.all(
      signers.value.map(s =>
        api.post<{ data: { token: string } }>('/api/signature-requests', {
          documentId: uploadedDocId.value,
          signerEmail: s.email,
          expirationHours: 72,
        })
      )
    )
    signingLinks.value = results.map(r => `${window.location.origin}/sign/${r.data.data.token}`)
    step.value = 3
  } catch (err: any) {
    submitError.value = err.response?.data?.message ?? '서명 요청 전송에 실패했어요.'
  } finally {
    isSending.value = false
  }
}

async function copyLink() {
  await navigator.clipboard.writeText(signingLinks.value[0] ?? '').catch(() => {})
  copied.value = true
  setTimeout(() => { copied.value = false }, 2000)
}

function resetForm() {
  step.value = 1
  file.value = null
  hash.value = ''
  hashDone.value = false
  uploadedDocId.value = null
  signers.value = [{ email: '', name: '', error: false }]
  message.value = ''
  signingLinks.value = []
  submitError.value = ''
}
</script>
