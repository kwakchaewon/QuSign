<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="qs-terms-backdrop"
      role="dialog"
      :aria-labelledby="'terms-title-' + type"
      aria-modal="true"
      @click.self="$emit('update:modelValue', false)"
      @keydown.escape="$emit('update:modelValue', false)"
    >
      <div class="qs-terms-modal">
        <div class="qs-terms-head">
          <h2 :id="'terms-title-' + type" class="qs-terms-title">{{ title }}</h2>
          <button
            class="qs-terms-close"
            aria-label="닫기"
            @click="$emit('update:modelValue', false)"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M18 6 6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </button>
        </div>

        <div class="qs-terms-body">
          <!-- 이용약관 -->
          <template v-if="type === 'terms'">
            <p class="qs-terms-updated">최종 수정일: 2026년 6월 1일</p>

            <h3>제1조 (목적)</h3>
            <p>본 약관은 QuSign(이하 "서비스")이 제공하는 양자내성암호 기반 전자서명 서비스의 이용 조건 및 절차, 회사와 회원 간의 권리·의무를 규정함을 목적으로 합니다.</p>

            <h3>제2조 (서비스 제공)</h3>
            <p>서비스는 NIST 표준 ML-DSA-65 알고리즘을 이용한 전자서명 생성·검증, PDF 문서 서명 관리, 서명 요청 및 알림 기능을 제공합니다.</p>

            <h3>제3조 (개인키 보안)</h3>
            <p>회원의 개인키는 회원이 설정한 비밀번호로 AES-256 암호화되어 저장됩니다. 비밀번호를 분실할 경우 개인키를 복구할 수 없으며, 이로 인한 손해에 대해 서비스는 책임을 지지 않습니다.</p>

            <h3>제4조 (금지 행위)</h3>
            <ul>
              <li>타인의 동의 없이 서명을 위조하거나 대리 서명하는 행위</li>
              <li>서비스를 이용한 불법 계약 체결</li>
              <li>시스템 무결성을 해치는 일체의 행위</li>
            </ul>

            <h3>제5조 (서비스 변경 및 중단)</h3>
            <p>서비스는 운영상 필요에 따라 서비스 내용을 변경하거나 중단할 수 있으며, 이 경우 사전에 공지합니다.</p>

            <h3>제6조 (준거법)</h3>
            <p>본 약관은 대한민국 법률에 따라 해석되며, 분쟁 발생 시 서울중앙지방법원을 제1심 관할 법원으로 합니다.</p>
          </template>

          <!-- 개인정보처리방침 -->
          <template v-else>
            <p class="qs-terms-updated">최종 수정일: 2026년 6월 1일</p>

            <h3>1. 수집하는 개인정보</h3>
            <ul>
              <li><strong>필수:</strong> 이메일 주소, 암호화된 비밀번호</li>
              <li><strong>자동 생성:</strong> ML-DSA-65 공개키/개인키(암호화 저장), 서명 이벤트 기록</li>
              <li><strong>서명 과정:</strong> 업로드된 PDF, SHA3-256 해시, 서명 일시</li>
            </ul>

            <h3>2. 개인정보 이용 목적</h3>
            <ul>
              <li>전자서명 생성 및 검증 서비스 제공</li>
              <li>서명 요청·완료 알림 발송</li>
              <li>계정 인증 및 보안 유지</li>
            </ul>

            <h3>3. 보유 및 이용 기간</h3>
            <p>회원 탈퇴 시 이메일·비밀번호는 즉시 삭제(소프트 딜리트)됩니다. 다만, 서명 이력은 전자서명 검증의 법적 효력 유지를 위해 탈퇴 후에도 보존될 수 있습니다.</p>

            <h3>4. 개인정보 보호 조치</h3>
            <ul>
              <li>개인키: 비밀번호 기반 AES-256 암호화 저장</li>
              <li>전송: TLS 1.3 암호화 통신</li>
              <li>비밀번호: bcrypt 단방향 해시</li>
            </ul>

            <h3>5. 제3자 제공</h3>
            <p>수집된 개인정보는 원칙적으로 제3자에게 제공하지 않습니다. 다만, 법령에 따른 요청이 있는 경우 예외로 합니다.</p>

            <h3>6. 문의</h3>
            <p>개인정보 관련 문의: <a href="mailto:privacy@qusign.kr">privacy@qusign.kr</a></p>
          </template>
        </div>

        <div class="qs-terms-foot">
          <button class="qs-terms-confirm" @click="$emit('update:modelValue', false)">
            확인
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  modelValue: boolean
  type: 'terms' | 'privacy'
}>()

defineEmits<{ (e: 'update:modelValue', v: boolean): void }>()

const title = computed(() =>
  props.type === 'terms' ? '이용약관' : '개인정보처리방침'
)
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
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-lg);
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
  font-size: 16px;
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
  padding: 20px 24px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.qs-terms-updated {
  font-size: 12px;
  color: var(--text-tertiary);
  margin: 0 0 16px;
}

.qs-terms-body h3 {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 18px 0 6px;
}

.qs-terms-body p {
  margin: 0 0 8px;
}

.qs-terms-body ul {
  margin: 0 0 8px;
  padding-left: 18px;
}

.qs-terms-body li {
  margin-bottom: 4px;
}

.qs-terms-body a {
  color: var(--color-primary-500);
  text-decoration: none;
}
.qs-terms-body a:hover {
  text-decoration: underline;
}

.qs-terms-foot {
  padding: 16px 24px;
  border-top: 1px solid var(--border-default);
  flex-shrink: 0;
  display: flex;
  justify-content: flex-end;
}

.qs-terms-confirm {
  padding: 8px 20px;
  border-radius: var(--radius-md, 10px);
  border: none;
  background: var(--color-primary-500);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity var(--duration-fast, 0.15s);
}
.qs-terms-confirm:hover {
  opacity: 0.88;
}
</style>
