<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="qs-terms-backdrop"
      role="dialog"
      aria-labelledby="terms-modal-title"
      aria-modal="true"
      @click.self="handleClose"
      @keydown.escape="handleClose"
    >
      <div class="qs-terms-modal">
        <!-- 헤더 -->
        <div class="qs-terms-head">
          <h2 id="terms-modal-title" class="qs-terms-title">이용약관 및 개인정보처리방침</h2>
          <button class="qs-terms-close" aria-label="닫기" @click="handleClose">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M18 6 6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </button>
        </div>

        <!-- 본문 (스크롤 영역) -->
        <div class="qs-terms-body" ref="bodyRef" @scroll="onScroll">
          <!-- 이용약관 -->
          <section class="qs-terms-section">
            <h3 class="qs-terms-section-title">이용약관</h3>
            <p class="qs-terms-updated">최종 수정일: 2026년 6월 9일</p>

            <h4>제1조 (목적)</h4>
            <p>본 약관은 QuSign(이하 "서비스")이 제공하는 양자내성암호 기반 전자서명 서비스의 이용 조건 및 절차, 회사와 회원 간의 권리·의무를 규정함을 목적으로 합니다.</p>

            <h4>제2조 (서비스 제공)</h4>
            <p>서비스는 NIST 표준 ML-DSA-65 알고리즘을 이용한 전자서명 생성·검증, PDF 문서 서명 관리, 서명 요청 및 알림 기능을 제공합니다.</p>

            <h4>제3조 (개인키 보안)</h4>
            <p>회원의 개인키는 회원이 설정한 비밀번호로 AES-256 암호화되어 저장됩니다.</p>
            <p>비밀번호를 분실할 경우 개인키를 복구할 수 없으며, 이로 인한 손해에 대해 서비스는 책임을 지지 않습니다.</p>

            <h4>제4조 (금지 행위)</h4>
            <ul>
              <li>타인의 동의 없이 서명을 위조하거나 대리 서명하는 행위</li>
              <li>서비스를 이용한 불법 계약 체결</li>
              <li>시스템 무결성을 해치는 일체의 행위</li>
            </ul>

            <h4>제5조 (서비스 변경 및 중단)</h4>
            <p>서비스는 운영상 필요에 따라 서비스 내용을 변경하거나 중단할 수 있으며, 이 경우 사전에 공지합니다.</p>

            <h4>제6조 (준거법)</h4>
            <p>본 약관은 대한민국 법률에 따라 해석되며, 분쟁 발생 시 서울중앙지방법원을 제1심 관할 법원으로 합니다.</p>
          </section>

          <div class="qs-terms-divider" />

          <!-- 개인정보처리방침 -->
          <section class="qs-terms-section">
            <h3 class="qs-terms-section-title">개인정보처리방침</h3>
            <p class="qs-terms-updated">최종 수정일: 2026년 6월 9일</p>

            <h4>1. 수집하는 개인정보</h4>
            <ul>
              <li><strong>필수:</strong> 이메일 주소, 암호화된 비밀번호</li>
              <li><strong>자동 생성:</strong> ML-DSA-65 공개키/개인키(암호화 저장), 서명 이벤트 기록</li>
              <li><strong>서명 과정:</strong> 업로드된 PDF, SHA3-256 해시, 서명 일시</li>
              <li><strong>감사 로그:</strong> 서비스 이용 시 접속 IP 주소, 브라우저 정보(User-Agent), 이벤트 유형 및 발생 일시</li>
            </ul>

            <h4>2. 개인정보 이용 목적</h4>
            <ul>
              <li>전자서명 생성 및 검증 서비스 제공</li>
              <li>서명 요청·완료 알림 발송</li>
              <li>계정 인증 및 보안 유지</li>
              <li>감사 로그 기록을 통한 서비스 보안 강화 및 부정 이용 방지</li>
            </ul>

            <h4>3. 보유 및 이용 기간</h4>
            <p>회원 탈퇴 시 이메일·비밀번호는 즉시 삭제(소프트 딜리트)됩니다.</p>
            <p>다만, 서명 이력은 전자서명 검증의 법적 효력 유지를 위해 탈퇴 후에도 보존될 수 있습니다.</p>
            <p>감사 로그(서비스 이용 중 발생하는 서명 이벤트 및 접속 IP·User-Agent)는 인증 보안 감시·서명 무결성 검증·분쟁 해결 목적으로 수집일로부터 <strong>10년간</strong> 보관됩니다(전자서명법 제31조 기준).</p>

            <h4>4. 개인정보 보호 조치</h4>
            <ul>
              <li>개인키: 비밀번호 기반 AES-256 암호화 저장</li>
              <li>전송: TLS 1.3 암호화 통신</li>
              <li>비밀번호: bcrypt 단방향 해시</li>
            </ul>

            <h4>5. 제3자 제공</h4>
            <p>수집된 개인정보는 원칙적으로 제3자에게 제공하지 않습니다.</p>
            <p>다만, 법령에 따른 요청이 있는 경우 예외로 합니다.</p>

            <h4>6. 개인정보 국외 이전 (개인정보보호법 제28조의8)</h4>
            <p>서비스는 Amazon Web Services 싱가포르(ap-southeast-1) 리전에서 운영됩니다. 이에 따라 수집된 개인정보가 국외로 이전될 수 있습니다.</p>
            <ul>
              <li><strong>이전받는 자:</strong> Amazon Web Services, Inc.</li>
              <li><strong>이전되는 국가:</strong> 싱가포르</li>
              <li><strong>이전 일시 및 방법:</strong> 서비스 이용 시 네트워크를 통해 전송</li>
              <li><strong>이전되는 개인정보 항목:</strong> 이메일, 이름, 서명 데이터, 업로드 문서</li>
              <li><strong>이전받는 자의 이용 목적:</strong> 서비스 인프라 운영 (저장, 처리)</li>
              <li><strong>보유·이용 기간:</strong> 회원 탈퇴 후 즉시 파기 또는 법령에 따른 보존 기간</li>
            </ul>

            <h4>7. 문의</h4>
            <p>개인정보 관련 문의: <a href="mailto:privacy@qusign.kr">privacy@qusign.kr</a></p>
          </section>

          <!-- 스크롤 유도 패딩 -->
          <div style="height: 8px;" />
        </div>

        <!-- 하단 동의 버튼 (sticky) — 읽기 전용일 때 숨김 -->
        <div v-if="!viewOnly" class="qs-terms-foot">
          <p v-if="!scrolledToBottom" class="qs-terms-scroll-hint">
            ↓ 아래로 스크롤하여 내용을 확인하세요
          </p>
          <div class="qs-terms-actions">
            <button class="qs-terms-btn-disagree" @click="handleDisagree">
              동의하지 않아요
            </button>
            <button class="qs-terms-btn-agree" @click="handleAgree">
              이용약관 및 개인정보처리방침에 동의해요
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

// 모달 열릴 때마다 스크롤 상태 초기화
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

/* 헤더 */
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

/* 본문 */
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

.qs-terms-divider {
  border: none;
  border-top: 1px solid var(--border-default);
  margin: 20px 0;
}

/* 하단 푸터 */
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
