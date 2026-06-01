<template>
  <div class="qs-page qs-rq-page">
    <AppTopbar />

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

      <!-- ===== Step 1: Multi-file upload ===== -->
      <div v-if="step === 1" class="qs-step-card">
        <div class="qs-step-body">
          <h2 class="qs-step-title">PDF 업로드</h2>
          <p class="qs-step-desc">서명 요청할 PDF 파일을 업로드하세요. 최대 5개, 각 50MB까지 지원합니다.</p>

          <!-- Drop zone: full → compact → hidden (max reached) -->
          <div v-if="files.length < MAX_FILES"
            class="qs-drop"
            :class="{ 'is-drag': isDrag, 'is-compact': files.length > 0 }"
            @dragover.prevent="isDrag = true"
            @dragleave.prevent="isDrag = false"
            @drop.prevent="handleDrop"
            @click="fileInputEl?.click()">
            <input ref="fileInputEl" type="file" accept=".pdf" multiple style="display:none"
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

            <template v-if="files.length === 0">
              <p class="qs-drop-title">파일을 여기에 드래그하거나</p>
              <p class="qs-drop-sub">클릭하여 파일 선택 · 최대 5개, 각 50MB</p>
              <span class="qs-drop-pill">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                  <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5"
                    stroke-linecap="round"/>
                </svg>
                파일 선택
              </span>
            </template>
            <div v-else class="qs-drop-compact-text">
              <p class="qs-drop-title">파일 추가</p>
              <p class="qs-drop-sub">{{ files.length }}/{{ MAX_FILES }}개 · 클릭 또는 드래그</p>
            </div>
          </div>

          <div v-else class="qs-drop-max">
            <p class="qs-drop-max-title">최대 파일 수 도달</p>
            <p class="qs-drop-max-sub">최대 {{ MAX_FILES }}개까지 업로드할 수 있습니다</p>
          </div>

          <!-- File rows -->
          <div v-if="files.length > 0" class="qs-mfile-list">
            <div v-for="f in files" :key="f.id" class="qs-mfile"
              :class="{ 'is-error': f.status === 'error' }">
              <div class="qs-mfile-icon">
                <svg width="24" height="30" viewBox="0 0 28 36" fill="none">
                  <rect width="28" height="36" rx="4" fill="var(--color-error-bg)"/>
                  <text x="4" y="22" font-size="7" font-weight="800" fill="var(--color-error)"
                    font-family="monospace">PDF</text>
                </svg>
              </div>
              <div class="qs-mfile-main">
                <div class="qs-mfile-name">{{ f.name }}</div>
                <div class="qs-mfile-meta">
                  <span>{{ formatSize(f.size) }}</span>
                  <span class="qs-dot-sep">·</span>
                  <span class="qs-mfile-status" :class="fileStatusClass(f)">
                    <svg v-if="f.status === 'uploading'" width="11" height="11"
                      viewBox="0 0 24 24" fill="none"
                      style="animation:qs-spin 0.8s linear infinite">
                      <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"
                        stroke-dasharray="20 40"/>
                    </svg>
                    <svg v-else-if="f.status === 'done'" width="11" height="11"
                      viewBox="0 0 24 24" fill="none">
                      <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2.5"
                        stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    {{ fileStatusText(f) }}
                  </span>
                </div>
                <div v-if="f.status === 'uploading'" class="qs-mfile-prog">
                  <div class="qs-mfile-prog-bar" :style="{ width: f.progress + '%' }"></div>
                </div>
              </div>
              <div class="qs-mfile-actions">
                <button v-if="f.status === 'error'"
                  class="qs-btn qs-btn-sm qs-btn-ghost"
                  @click="retryFile(f)">재시도</button>
                <button class="qs-btn qs-btn-sm qs-btn-ghost" @click="removeFile(f.id)">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                    <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2"
                      stroke-linecap="round"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="qs-step-foot">
          <button class="qs-btn qs-btn-sm qs-btn-ghost" @click="router.push('/documents')">취소</button>
          <button class="qs-btn qs-btn-md qs-btn-primary" :disabled="!canGoNext" @click="step = 2">
            다음 단계
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2"
                stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- ===== Step 2: Signer config ===== -->
      <div v-if="step === 2" class="qs-step-card">
        <div class="qs-step-body">
          <h2 class="qs-step-title">서명 요청 설정</h2>

          <!-- File summary with pill list -->
          <div class="qs-file qs-file-summary">
            <div class="qs-file-icon">
              <svg width="28" height="36" viewBox="0 0 28 36" fill="none">
                <rect width="28" height="36" rx="4" fill="var(--color-error-bg)"/>
                <text x="4" y="22" font-size="7" font-weight="800" fill="var(--color-error)"
                  font-family="monospace">PDF</text>
              </svg>
            </div>
            <div class="qs-file-main">
              <div class="qs-file-name">{{ files.length }}개 파일 업로드됨</div>
              <div class="qs-file-pills">
                <span v-for="f in files" :key="f.id" class="qs-file-pill">{{ f.name }}</span>
              </div>
            </div>
          </div>

          <!-- Signer pills -->
          <div class="qs-section">
            <div class="qs-section-head">
              <h3 class="qs-section-title">서명자</h3>
              <span class="qs-section-meta">{{ signerPills.length }}/5명</span>
            </div>

            <div class="qs-signer-pills" @click="signerInputEl?.focus()">
              <span v-for="(pill, i) in signerPills" :key="i" class="qs-signer-pill">
                <span class="qs-signer-pill-label">{{ pill.email }}</span>
                <button class="qs-signer-pill-remove" @click.stop="signerPills.splice(i, 1)">
                  <svg width="10" height="10" viewBox="0 0 24 24" fill="none">
                    <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2.5"
                      stroke-linecap="round"/>
                  </svg>
                </button>
              </span>
              <input v-if="signerPills.length < 5"
                ref="signerInputEl"
                v-model="signerInputVal"
                type="email"
                placeholder="이메일 입력 후 Enter"
                @keydown="handleSignerKeydown">
            </div>
            <p v-if="signerError" class="qs-help is-error">{{ signerError }}</p>
            <p class="qs-pill-hint">Enter 또는 쉼표로 서명자 추가 · 최대 5명</p>
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
            :disabled="signerPills.length === 0 || isSending" @click="handleSubmit">
            <svg v-if="isSending" width="16" height="16" viewBox="0 0 24 24" fill="none"
              style="animation:qs-spin 0.8s linear infinite">
              <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"
                stroke-dasharray="20 40"/>
            </svg>
            {{ isSending ? '전송 중...' : '서명 요청 보내기' }}
          </button>
        </div>
      </div>

      <!-- ===== Step 3: Done ===== -->
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
            {{ fileResults.length }}개 파일 · {{ signerPills.length }}명 서명자에게<br>
            서명 요청 이메일이 발송되었습니다.
          </p>

          <!-- Per-file link list -->
          <div v-for="(fr, fi) in fileResults" :key="fi" class="qs-link-list">
            <div class="qs-link-file-head">
              <svg width="14" height="18" viewBox="0 0 28 36" fill="none">
                <rect width="28" height="36" rx="4" fill="var(--color-error-bg)"/>
                <text x="4" y="22" font-size="7" font-weight="800" fill="var(--color-error)"
                  font-family="monospace">PDF</text>
              </svg>
              <span class="qs-link-file-name">{{ fr.fileName }}</span>
            </div>
            <div class="qs-link-entries">
              <div v-for="(sr, si) in fr.signers" :key="si" class="qs-link-entry">
                <span class="qs-link-entry-email">{{ sr.email }}</span>
                <span class="qs-link-url">{{ sr.link }}</span>
                <button
                  class="qs-btn qs-btn-sm qs-btn-secondary"
                  :class="{ 'is-success': copiedKey === `${fi}-${si}` }"
                  @click="copyOne(sr.link, `${fi}-${si}`)">
                  {{ copiedKey === `${fi}-${si}` ? '복사됨' : '복사' }}
                </button>
              </div>
            </div>
          </div>

          <button class="qs-btn qs-btn-sm qs-btn-secondary qs-copy-all-btn" @click="copyAll">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <rect x="8" y="8" width="13" height="13" rx="2" stroke="currentColor" stroke-width="2"/>
              <path d="M4 16H3a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h11a2 2 0 0 1 2 2v1"
                stroke="currentColor" stroke-width="2"/>
            </svg>
            {{ copiedAll ? '복사됨!' : '전체 링크 복사' }}
          </button>

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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/lib/api'
import AppTopbar from '@/components/layout/AppTopbar.vue'

const MAX_FILES = 5
const MAX_SIZE = 50 * 1024 * 1024
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

interface FileEntry {
  id: string
  file: File
  name: string
  size: number
  progress: number
  status: 'waiting' | 'uploading' | 'done' | 'error'
  hash: string
  docId: number | null
  errorMsg: string
}

interface SignerPill { email: string }
interface SignerResult { email: string; link: string }
interface FileResult { fileName: string; signers: SignerResult[] }

const router = useRouter()
const step = ref(1)

const fileInputEl = ref<HTMLInputElement | null>(null)
const isDrag = ref(false)
const files = ref<FileEntry[]>([])

const signerInputEl = ref<HTMLInputElement | null>(null)
const signerInputVal = ref('')
const signerPills = ref<SignerPill[]>([])
const signerError = ref('')
const message = ref('')
const isSending = ref(false)
const fileResults = ref<FileResult[]>([])
const copiedKey = ref('')
const copiedAll = ref(false)

function handleBeforeUnload(e: BeforeUnloadEvent) {
  const isActive = files.value.some(f => f.status === 'uploading') || isSending.value
  if (isActive) e.preventDefault()
}
onMounted(() => window.addEventListener('beforeunload', handleBeforeUnload))
onUnmounted(() => window.removeEventListener('beforeunload', handleBeforeUnload))

const canGoNext = computed(() =>
  files.value.length > 0 &&
  files.value.every(f => f.status === 'done')
)

function handleDrop(e: DragEvent) {
  isDrag.value = false
  const dropped = Array.from(e.dataTransfer?.files ?? []).filter(f => f.type === 'application/pdf')
  addFiles(dropped)
}

function handleFileChange(e: Event) {
  const selected = Array.from((e.target as HTMLInputElement).files ?? [])
  addFiles(selected)
  if (fileInputEl.value) fileInputEl.value.value = ''
}

function addFiles(newFiles: File[]) {
  const remaining = MAX_FILES - files.value.length
  const toAdd = newFiles.slice(0, remaining)
  for (const f of toAdd) {
    if (f.size > MAX_SIZE) {
      alert(`"${f.name}" 파일 크기가 50MB를 초과합니다.`)
      continue
    }
    const entry: FileEntry = {
      id: crypto.randomUUID(),
      file: f,
      name: f.name,
      size: f.size,
      progress: 0,
      status: 'waiting',
      hash: '',
      docId: null,
      errorMsg: '',
    }
    files.value.push(entry)
    uploadFile(files.value[files.value.length - 1])
  }
}

async function uploadFile(entry: FileEntry) {
  entry.status = 'uploading'
  entry.progress = 0
  entry.errorMsg = ''
  try {
    const formData = new FormData()
    formData.append('file', entry.file)
    const res = await api.post<{ data: { id: number; hashSha3256: string } }>(
      '/api/documents',
      formData,
      {
        onUploadProgress: (evt: { loaded: number; total?: number }) => {
          entry.progress = Math.round((evt.loaded * 100) / (evt.total ?? evt.loaded))
        },
      }
    )
    entry.status = 'done'
    entry.progress = 100
    entry.hash = res.data.data.hashSha3256
    entry.docId = res.data.data.id
  } catch (err: any) {
    entry.status = 'error'
    entry.errorMsg = err.response?.data?.message ?? '업로드 실패'
  }
}

function retryFile(entry: FileEntry) { uploadFile(entry) }

function removeFile(id: string) {
  const idx = files.value.findIndex(f => f.id === id)
  if (idx !== -1) files.value.splice(idx, 1)
}

function formatSize(bytes: number) {
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

function fileStatusClass(f: FileEntry) {
  if (f.status === 'error') return 'is-error'
  if (f.status === 'uploading') return 'is-uploading'
  if (f.status === 'done') return 'is-done'
  return ''
}

function fileStatusText(f: FileEntry) {
  if (f.status === 'error') return f.errorMsg || '업로드 실패'
  if (f.status === 'uploading') return `업로드 중 ${f.progress}%`
  if (f.status === 'done') return 'SHA3-256 완료'
  return '대기 중'
}

function handleSignerKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' || e.key === ',') {
    e.preventDefault()
    addSignerPill()
  }
}

function addSignerPill() {
  const email = signerInputVal.value.trim().replace(/,$/, '')
  if (!email) return
  if (!EMAIL_RE.test(email)) {
    signerError.value = '올바른 이메일 형식이 아닙니다'
    return
  }
  if (signerPills.value.some(p => p.email === email)) {
    signerError.value = '이미 추가된 이메일입니다'
    return
  }
  signerPills.value.push({ email })
  signerInputVal.value = ''
  signerError.value = ''
}

async function handleSubmit() {
  if (signerPills.value.length === 0) return
  isSending.value = true
  try {
    const results: FileResult[] = []
    for (const f of files.value) {
      if (!f.docId) continue
      const signerResults: SignerResult[] = []
      for (const s of signerPills.value) {
        const res = await api.post<{ data: { token: string } }>('/api/signature-requests', {
          documentId: f.docId,
          signerEmail: s.email,
          expirationHours: 72,
        })
        signerResults.push({
          email: s.email,
          link: `${window.location.origin}/sign/${res.data.data.token}`,
        })
      }
      results.push({ fileName: f.name, signers: signerResults })
    }
    fileResults.value = results
    step.value = 3
  } catch (err: any) {
    alert(err.response?.data?.message ?? '서명 요청 전송에 실패했어요.')
  } finally {
    isSending.value = false
  }
}

async function copyOne(link: string, key: string) {
  await navigator.clipboard.writeText(link).catch(() => {})
  copiedKey.value = key
  setTimeout(() => { copiedKey.value = '' }, 2000)
}

async function copyAll() {
  const lines: string[] = []
  for (const fr of fileResults.value) {
    for (const sr of fr.signers) {
      lines.push(`[${fr.fileName}] ${sr.email}: ${sr.link}`)
    }
  }
  const all = lines.join('\n')
  await navigator.clipboard.writeText(all).catch(() => {})
  copiedAll.value = true
  setTimeout(() => { copiedAll.value = false }, 2000)
}

function resetForm() {
  step.value = 1
  files.value = []
  signerPills.value = []
  signerInputVal.value = ''
  signerError.value = ''
  message.value = ''
  fileResults.value = []
  copiedKey.value = ''
  copiedAll.value = false
}
</script>
