<template>
  <div class="pdf-viewer">
    <div ref="scrollRef" class="pdf-viewer-scroll">
      <!-- 로딩 중이고 아직 페이지 정보 없을 때만 스피너 표시 -->
      <div v-if="loading && totalPages === 0" class="pdf-viewer-loading">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true" class="pdf-viewer-spinner">
          <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-opacity="0.25" stroke-width="2.4"/>
          <path d="M21 12a9 9 0 0 0-9-9" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"/>
        </svg>
        <span>PDF 불러오는 중…</span>
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

// @ts-ignore — import.meta.url is valid in Vite (module: ESNext), IDE false-positive
pdfjsLib.GlobalWorkerOptions.workerSrc = new URL(
  'pdfjs-dist/build/pdf.worker.min.mjs',
  import.meta.url,
).href

const props = defineProps<{ src: string; height?: string }>()
const emit = defineEmits<{ 'scrolled-to-end': [] }>()

const viewerHeight = computed(() => props.height ?? '540px')

const scrollRef = ref<HTMLDivElement>()
const currentPage = ref(1)
const totalPages = ref(0)
const loading = ref(false)

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
  totalPages.value = 0
  currentPage.value = 1
  canvasRefs.length = 0

  try {
    pdfDoc = await pdfjsLib.getDocument({ url }).promise
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
  } catch {
    loading.value = false
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

.pdf-viewer-loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--text-subtle);
  font-size: 13px;
  min-height: 200px;
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
