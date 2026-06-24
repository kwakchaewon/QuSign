<template>
  <div class="pdf-viewer">
    <div ref="scrollRef" class="pdf-viewer-scroll">
      <div v-if="loading && totalPages === 0" class="pdf-viewer-loading">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true" class="pdf-viewer-spinner">
          <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-opacity="0.25" stroke-width="2.4"/>
          <path d="M21 12a9 9 0 0 0-9-9" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"/>
        </svg>
        <span>PDF 불러오는 중…</span>
      </div>

      <div v-if="!loading && loadError" class="pdf-viewer-error">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.7"/>
          <path d="M12 8v4M12 15.5v.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
        </svg>
        <span>PDF를 불러올 수 없습니다.</span>
      </div>

      <!-- v-for로 Vue가 DOM 소유 — JS로 직접 appendChild하지 않음 -->
      <div
        v-for="n in totalPages"
        :key="n"
        class="pdf-page-wrapper"
        :data-page="n"
      >
        <canvas
          :ref="(el) => setCanvasRef(el, n - 1)"
          style="display:block;width:100%;height:auto;"
        />
      </div>
    </div>

    <div v-if="!loading && totalPages > 0" class="pdf-viewer-indicator" aria-live="polite">
      {{ currentPage }} / {{ totalPages }} 페이지
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as pdfjsLib from 'pdfjs-dist'
import pdfjsWorkerUrl from 'pdfjs-dist/build/pdf.worker.min.mjs?url'

// nginx이 .mjs를 application/octet-stream으로 서빙하면 모듈 스크립트 로드 실패.
// Blob URL로 감싸면 MIME 타입 검사를 우회한다.
const workerBlob = new Blob(
  [`import '${pdfjsWorkerUrl}'`],
  { type: 'application/javascript' },
)
pdfjsLib.GlobalWorkerOptions.workerSrc = URL.createObjectURL(workerBlob)

const props = defineProps<{ src: string; height?: string }>()
const emit = defineEmits<{ 'scrolled-to-end': [] }>()

const viewerHeight = computed(() => props.height ?? '540px')

const scrollRef = ref<HTMLDivElement>()
const currentPage = ref(1)
const totalPages = ref(0)
const loading = ref(false)
const loadError = ref(false)

const canvasRefs: (HTMLCanvasElement | null)[] = []
function setCanvasRef(el: unknown, index: number) {
  canvasRefs[index] = el as HTMLCanvasElement | null
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
let pdfDoc: any = null
let pageObserver: IntersectionObserver | null = null
let lastPageObserver: IntersectionObserver | null = null

onMounted(() => {
  if (props.src) renderPdf(props.src)
})

watch(() => props.src, (url) => {
  if (!url) return
  renderPdf(url)
})

async function renderPdf(url: string) {
  pageObserver?.disconnect()
  lastPageObserver?.disconnect()
  loading.value = true
  loadError.value = false
  totalPages.value = 0
  currentPage.value = 1
  canvasRefs.length = 0

  try {
    pdfDoc = await pdfjsLib.getDocument({
      url,
      standardFontDataUrl: '/standard_fonts/',
      cMapUrl: '/cmaps/',
      cMapPacked: true,
      wasmUrl: '/wasm/',
    }).promise
    totalPages.value = pdfDoc.numPages

    // Vue가 v-for 캔버스 엘리먼트를 DOM에 추가할 때까지 대기
    await nextTick()

    const containerWidth = scrollRef.value?.clientWidth || 560
    const scale = Math.max(0.85, Math.min(containerWidth / 595, 1.6))

    for (let i = 0; i < pdfDoc.numPages; i++) {
      const canvas = canvasRefs[i]
      if (!canvas) continue
      const page = await pdfDoc.getPage(i + 1)
      const viewport = page.getViewport({ scale })
      canvas.width = viewport.width
      canvas.height = viewport.height
      const ctx = canvas.getContext('2d')
      if (ctx) await page.render({ canvasContext: ctx, viewport }).promise
    }

    loading.value = false
    await nextTick()
    setupObservers()
  } catch (err) {
    console.error('[PdfViewer] 렌더링 실패:', err)
    loading.value = false
    loadError.value = true
  }
}

function setupObservers() {
  if (!scrollRef.value) return
  const root = scrollRef.value

  pageObserver = new IntersectionObserver(
    (entries) => {
      let max = currentPage.value
      entries.forEach((e) => {
        if (e.isIntersecting) {
          const p = Number((e.target as HTMLElement).dataset.page)
          if (p > max) max = p
        }
      })
      currentPage.value = max
    },
    { root, threshold: 0.3 },
  )

  lastPageObserver = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) {
        emit('scrolled-to-end')
        lastPageObserver!.disconnect()
      }
    },
    { root, threshold: 0.4 },
  )

  const wrappers = root.querySelectorAll('.pdf-page-wrapper')
  wrappers.forEach((w) => pageObserver!.observe(w))
  if (wrappers.length > 0) {
    lastPageObserver.observe(wrappers[wrappers.length - 1])
  }
}

onBeforeUnmount(() => {
  pageObserver?.disconnect()
  lastPageObserver?.disconnect()
})
</script>

<style scoped>
.pdf-viewer {
  position: relative;
}

.pdf-viewer-scroll {
  height: v-bind(viewerHeight);
  overflow-y: auto;
  background: var(--surface-muted);
  border: 1px solid var(--border-base);
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pdf-viewer-loading,
.pdf-viewer-error {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 13px;
  min-height: 200px;
}

.pdf-viewer-loading {
  color: var(--text-subtle);
}

.pdf-viewer-error {
  color: var(--color-error);
}

.pdf-viewer-spinner {
  animation: pdf-spin 1s linear infinite;
  flex-shrink: 0;
}

@keyframes pdf-spin {
  to { transform: rotate(360deg); }
}

.pdf-page-wrapper {
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.14);
  background: #fff;
  flex-shrink: 0;
}

.pdf-viewer-indicator {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 11px;
  padding: 3px 9px;
  border-radius: 20px;
  pointer-events: none;
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.02em;
}
</style>
