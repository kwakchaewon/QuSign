<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="qs-terms-backdrop"
      role="dialog"
      aria-labelledby="transfer-modal-title"
      aria-modal="true"
      @click.self="handleClose"
      @keydown.escape="handleClose"
    >
      <div class="qs-terms-modal">
        <!-- 헤더 -->
        <div class="qs-terms-head">
          <h2 id="transfer-modal-title" class="qs-terms-title">개인정보 국외 이전 동의</h2>
          <button class="qs-terms-close" aria-label="닫기" @click="handleClose">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M18 6 6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </button>
        </div>

        <!-- 본문 (스크롤 영역) -->
        <div class="qs-terms-body" ref="bodyRef" @scroll="onScroll">
          <section class="qs-terms-section">
            <h3 class="qs-terms-section-title">개인정보 국외 이전 안내</h3>
            <p class="qs-terms-updated">근거: 개인정보보호법 제28조의8</p>

            <p>서비스는 Amazon Web Services 싱가포르(ap-southeast-1) 리전에서 운영됩니다. 이에 따라 수집된 개인정보가 아래와 같이 국외로 이전될 수 있으며, 이에 대한 별도 동의가 필요합니다.</p>

            <h4>이전받는 자</h4>
            <p>Amazon Web Services, Inc.</p>

            <h4>이전되는 국가</h4>
            <p>싱가포르 (ap-southeast-1 리전)</p>

            <h4>이전 일시 및 방법</h4>
            <p>서비스 이용 시 네트워크를 통해 상시 전송</p>

            <h4>이전되는 개인정보 항목</h4>
            <ul>
              <li>이메일 주소</li>
              <li>암호화된 비밀번호 해시</li>
              <li>ML-DSA-65 공개키 (개인키는 암호화된 형태로 저장)</li>
              <li>서명 이력 및 업로드된 PDF 문서</li>
              <li>서비스 이용 기록 (접속 IP, User-Agent, 이벤트 일시)</li>
            </ul>

            <h4>이전받는 자의 이용 목적</h4>
            <p>서비스 인프라 운영 (저장, 처리, 백업)</p>

            <h4>보유·이용 기간</h4>
            <p>회원 탈퇴 후 즉시 파기. 단, 서명 이력은 전자서명법 제31조에 따라 탈퇴 후에도 보존될 수 있습니다.</p>
            <p>감사 로그는 수집일로부터 10년간 보관됩니다.</p>

            <h4>거부 시 불이익</h4>
            <p>위 국외 이전에 동의하지 않으실 경우 서비스 이용이 불가합니다. 서비스 인프라 전체가 AWS 싱가포르 리전에서 운영되기 때문입니다.</p>

            <h4>문의</h4>
            <p>개인정보 관련 문의: <a href="mailto:privacy@qusign.kr">privacy@qusign.kr</a></p>
          </section>

          <div style="height: 8px;" />
        </div>

        <!-- 하단 동의 버튼 -->
        <div v-if="!viewOnly" class="qs-terms-foot">
          <p v-if="!scrolledToBottom" class="qs-terms-scroll-hint">
            ↓ 아래로 스크롤하여 내용을 확인하세요
          </p>
          <div class="qs-terms-actions">
            <button class="qs-terms-btn-disagree" @click="handleDisagree">
              동의하지 않아요
            </button>
            <button class="qs-terms-btn-agree" @click="handleAgree">
              국외 이전에 동의해요
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{ modelValue: boolean; viewOnly?: boolean }>()
const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'agree'): void
  (e: 'disagree'): void
}>()

const bodyRef = ref<HTMLElement | null>(null)
const scrolledToBottom = ref(false)

function onScroll() {
  const el = bodyRef.value
  if (!el) return
  scrolledToBottom.value = el.scrollTop + el.clientHeight >= el.scrollHeight - 24
}

watch(() => props.modelValue, (open) => {
  if (open) {
    scrolledToBottom.value = false
    setTimeout(() => {
      const el = bodyRef.value
      if (el) el.scrollTop = 0
    }, 50)
  }
})

function handleClose() {
  emit('update:modelValue', false)
}

function handleAgree() {
  emit('agree')
  emit('update:modelValue', false)
}

function handleDisagree() {
  emit('disagree')
  emit('update:modelValue', false)
}
</script>

<style scoped>
.qs-terms-backdrop {
  position: fixed;
  inset: 0;
  background: var(--surface-overlay, rgba(0, 0, 0, 0.5));
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: var(--z-modal, 1000);
  padding: 16px;
}

.qs-terms-modal {
  background: var(--surface-elevated);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-xl, 20px);
  width: 100%;
  max-width: 560px;
  max-height: 82vh;
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.qs-terms-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 16px;
  border-bottom: 1px solid var(--border-default);
  flex-shrink: 0;
}

.qs-terms-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.qs-terms-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: none;
  color: var(--text-secondary);
  cursor: pointer;
  transition: background var(--duration-fast, 0.15s);
}
.qs-terms-close:hover {
  background: var(--surface-muted);
  color: var(--text-primary);
}

.qs-terms-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px 0;
}

.qs-terms-section {
  margin-bottom: 8px;
}

.qs-terms-section-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px;
}

.qs-terms-updated {
  font-size: 11px;
  color: var(--text-tertiary);
  margin: 0 0 14px;
}

.qs-terms-body h4 {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 16px 0 5px;
}

.qs-terms-body p {
  font-size: 12px;
  line-height: 1.7;
  color: var(--text-secondary);
  margin: 0 0 6px;
}

.qs-terms-body ul {
  font-size: 12px;
  line-height: 1.7;
  color: var(--text-secondary);
  margin: 0 0 6px;
  padding-left: 18px;
}

.qs-terms-body li { margin-bottom: 2px; }

.qs-terms-body a {
  color: var(--color-primary-500);
  text-decoration: none;
}
.qs-terms-body a:hover { text-decoration: underline; }

.qs-terms-foot {
  flex-shrink: 0;
  border-top: 1px solid var(--border-default);
  padding: 14px 20px 16px;
  background: var(--surface-elevated);
}

.qs-terms-scroll-hint {
  font-size: 11px;
  color: var(--text-tertiary);
  text-align: center;
  margin: 0 0 10px;
}

.qs-terms-actions {
  display: flex;
  gap: 8px;
}

.qs-terms-btn-disagree {
  flex: 1;
  padding: 10px 12px;
  border-radius: var(--radius-md, 10px);
  border: 1px solid var(--border-default);
  background: var(--surface-muted);
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background var(--duration-fast, 0.15s);
  white-space: nowrap;
}
.qs-terms-btn-disagree:hover {
  background: var(--border-default);
  color: var(--text-primary);
}

.qs-terms-btn-agree {
  flex: 2;
  padding: 10px 12px;
  border-radius: var(--radius-md, 10px);
  border: none;
  background: var(--color-primary-500);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity var(--duration-fast, 0.15s);
  white-space: nowrap;
}
.qs-terms-btn-agree:hover { opacity: 0.88; }
</style>
