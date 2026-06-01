<template>
  <div class="qs-page qs-signer-page">
    <PublicTopbar title="QuSign" subtitle="전자서명 요청" logo-to="/" show-pqc-badge />

    <main class="qs-main">
      <!-- 스텝퍼 (취소 상태가 아닐 때만) -->
      <div v-if="!cancelled" class="qs-stepper" aria-label="서명 진행 단계">
        <div :class="['qs-step-item', { 'is-active': step === 1, 'is-done': step > 1 }]">
          <span class="qs-step-num">
            <svg v-if="step > 1" width="12" height="12" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.4"
                stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <template v-else>1</template>
          </span>
          <span class="qs-step-label">본인 확인</span>
        </div>
        <span :class="['qs-step-bar', { 'is-done': step > 1 }]" />
        <div :class="['qs-step-item', { 'is-active': step === 2, 'is-done': step > 2 }]">
          <span class="qs-step-num">
            <svg v-if="step > 2" width="12" height="12" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.4"
                stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <template v-else>2</template>
          </span>
          <span class="qs-step-label">문서 검토 · 서명</span>
        </div>
        <span :class="['qs-step-bar', { 'is-done': step > 2 }]" />
        <div :class="['qs-step-item', { 'is-active': step === 3 }]">
          <span class="qs-step-num">3</span>
          <span class="qs-step-label">완료</span>
        </div>
      </div>

      <!-- ── 취소 상태 ── -->
      <article v-if="cancelled" class="qs-card qs-cancelled-card" aria-labelledby="cancelled-title">
        <div class="qs-cancelled-icon" aria-hidden="true">
          <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
            <circle cx="32" cy="32" r="28" stroke="currentColor" stroke-width="2"
              fill="var(--surface-muted)"/>
            <path d="M22 22l20 20M42 22L22 42" stroke="currentColor" stroke-width="2.4"
              stroke-linecap="round"/>
          </svg>
        </div>
        <h1 id="cancelled-title" class="qs-cancelled-title">취소된 서명 요청입니다</h1>
        <p class="qs-cancelled-desc">
          요청자가 이 서명 요청을 취소했습니다.<br />
          서명이 필요하다면 요청자에게 문의해 주세요.
        </p>
        <div v-if="cancelledInfo.requester || cancelledInfo.cancelledAt" class="qs-cancelled-meta">
          <div v-if="cancelledInfo.requester" class="qs-cancelled-meta-row">
            <span class="qs-cancelled-meta-k">요청자</span>
            <span class="qs-cancelled-meta-v">{{ cancelledInfo.requester }}</span>
          </div>
          <div v-if="cancelledInfo.cancelledAt" class="qs-cancelled-meta-row">
            <span class="qs-cancelled-meta-k">취소 일시</span>
            <span class="qs-cancelled-meta-v">{{ cancelledInfo.cancelledAt }}</span>
          </div>
        </div>
      </article>

      <!-- ── Step 1: 이메일 OTP 인증 ── -->
      <article v-else-if="step === 1" class="qs-card" aria-labelledby="step1-title">
        <header class="qs-card-head">
          <h1 id="step1-title" class="qs-card-title">본인 확인</h1>
          <p class="qs-card-desc">
            서명 요청을 받은 이메일 주소를 입력해 주세요.<br />
            입력하신 주소로 6자리 인증 코드를 보내 드려요.
          </p>
        </header>

        <div class="qs-field">
          <label class="qs-label" for="signer-email">이메일</label>
          <div :class="['qs-input', { 'is-disabled': otpSent, 'is-error': emailTouched && !emailValid }]">
            <span class="qs-input-icon" aria-hidden="true">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="5" width="18" height="14" rx="3" stroke="currentColor" stroke-width="1.6"/>
                <path d="M4 7l8 6 8-6" stroke="currentColor" stroke-width="1.6"
                  stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
            <input
              id="signer-email"
              v-model="email"
              type="email"
              inputmode="email"
              autocomplete="email"
              placeholder="name@company.com"
              :disabled="otpSent"
              @blur="emailTouched = true"
              @keydown.enter="!otpSent && sendOtp()"
            />
            <span v-if="emailValid" class="qs-input-tick" aria-hidden="true">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.4"
                  stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
          </div>
          <span v-if="emailTouched && !emailValid && email.length > 0"
            class="qs-help is-error">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M12 4l9 16H3l9-16z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/>
              <path d="M12 10v4M12 17v.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
            </svg>
            이메일 형식이 올바르지 않아요
          </span>
          <span v-if="sendErr" class="qs-help is-error">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M12 4l9 16H3l9-16z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/>
              <path d="M12 10v4M12 17v.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
            </svg>
            {{ sendErr }}
          </span>
        </div>

        <button v-if="!otpSent"
          type="button"
          class="qs-btn qs-btn-primary qs-btn-lg qs-btn-block"
          style="margin-top: 8px"
          :disabled="!emailValid || isSending"
          @click="sendOtp"
        >
          <svg v-if="isSending" width="20" height="20" viewBox="0 0 24 24" fill="none"
            style="animation: qs-signer-spin 1s linear infinite" aria-hidden="true">
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-opacity="0.25" stroke-width="2.4"/>
            <path d="M21 12a9 9 0 0 0-9-9" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"/>
          </svg>
          <span>{{ isSending ? '전송 중…' : '인증 코드 받기' }}</span>
          <svg v-if="!isSending" width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2"
              stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>

        <template v-if="otpSent">
          <div class="qs-sent-banner" role="status">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.4"
                stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>
              <span class="qs-sent-banner-strong">{{ email }}</span>으로 인증 코드를 보냈어요.
            </span>
          </div>

          <div class="qs-field" style="margin-top: 14px">
            <div class="qs-label-row">
              <label class="qs-label">인증 코드 (6자리)</label>
              <span :class="['qs-otp-timer', { 'is-expiring': timerSeconds <= 30 }]">
                <svg class="qs-otp-clock" width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.7"/>
                  <path d="M12 7v5l3 2" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
                </svg>
                <span class="qs-otp-timer-mono">{{ fmtTimer(timerSeconds) }}</span>
              </span>
            </div>
            <div class="qs-otp-row" role="group" aria-label="인증 코드 6자리 입력">
              <input
                v-for="(_, i) in otpCode"
                :key="i"
                :ref="(el) => setOtpRef(i, el)"
                type="text"
                inputmode="numeric"
                :autocomplete="i === 0 ? 'one-time-code' : 'off'"
                maxlength="6"
                :value="otpCode[i]"
                placeholder="·"
                :aria-label="`인증 코드 ${i + 1}자리`"
                :class="['qs-otp-input', { 'has-value': otpCode[i], 'is-error': !!otpErr }]"
                :disabled="isVerifying || timerSeconds === 0"
                @input="handleOtpInput(i, $event)"
                @keydown="handleOtpKeydown(i, $event)"
                @focus="handleOtpFocus"
              />
            </div>
            <span v-if="otpErr" class="qs-help is-error" style="margin-top: 8px">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M12 4l9 16H3l9-16z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/>
                <path d="M12 10v4M12 17v.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
              </svg>
              {{ otpErr }}
            </span>
            <span v-if="timerSeconds === 0 && !otpErr" class="qs-help is-error" style="margin-top: 8px">
              인증 코드가 만료되었어요. 재전송해 주세요.
            </span>
          </div>

          <div class="qs-otp-meta">
            <span class="qs-help">코드를 받지 못하셨나요?</span>
            <button type="button" class="qs-link-quiet"
              :disabled="timerSeconds > 150"
              @click="sendOtp">
              재전송{{ timerSeconds > 150 ? ` (${timerSeconds - 150}s)` : '' }}
            </button>
          </div>

          <button
            type="button"
            class="qs-btn qs-btn-primary qs-btn-lg qs-btn-block"
            style="margin-top: 10px"
            :disabled="otpCode.join('').length < 6 || isVerifying || timerSeconds === 0"
            @click="verifyOtp"
          >
            <svg v-if="isVerifying" width="20" height="20" viewBox="0 0 24 24" fill="none"
              style="animation: qs-signer-spin 1s linear infinite" aria-hidden="true">
              <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-opacity="0.25" stroke-width="2.4"/>
              <path d="M21 12a9 9 0 0 0-9-9" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"/>
            </svg>
            <span>{{ isVerifying ? '확인 중…' : '인증하고 문서 보기' }}</span>
            <svg v-if="!isVerifying" width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2"
                stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </template>
      </article>

      <!-- ── Step 2: 문서 검토 + 서명 ── -->
      <article v-else-if="step === 2" class="qs-card" aria-labelledby="step2-title">
        <header class="qs-card-head">
          <h1 id="step2-title" class="qs-card-title">문서 검토 · 서명</h1>
          <p class="qs-card-desc">
            아래 문서 내용을 확인하고, 동의 후 서명을 진행해 주세요.
            서명은 즉시 ML-DSA-65 알고리즘으로 처리됩니다.
          </p>
        </header>

        <!-- 문서 메타 -->
        <div class="qs-doc-meta">
          <div class="qs-doc-meta-head">
            <span class="qs-doc-icon-lg" aria-hidden="true">
              <svg width="40" height="48" viewBox="0 0 40 48" fill="none">
                <path d="M6 2h20l8 8v34a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V6a4 4 0 0 1 4-4z"
                  fill="var(--surface-elevated)" stroke="var(--border-strong)" stroke-width="1.2"/>
                <path d="M26 2v8h8" fill="var(--surface-muted)" stroke="var(--border-strong)" stroke-width="1.2"/>
                <rect x="6" y="26" width="22" height="3" rx="1" fill="var(--color-error)" opacity="0.85"/>
                <text x="9" y="40" font-family="monospace" font-size="8"
                  font-weight="700" fill="var(--color-error)">PDF</text>
              </svg>
            </span>
            <div class="qs-doc-meta-headtext">
              <div class="qs-doc-meta-name" :title="docInfo?.filename">
                {{ docInfo?.filename ?? '서명 요청 문서' }}
              </div>
              <div class="qs-doc-meta-size">
                <span>{{ docInfo?.pages ? `${docInfo.pages}페이지` : 'PDF' }}</span>
              </div>
            </div>
          </div>
          <div class="qs-meta-list">
            <div class="qs-meta-row">
              <span class="qs-meta-k">요청자</span>
              <span class="qs-meta-v qs-mono">{{ docInfo?.requesterEmail ?? '—' }}</span>
            </div>
            <div class="qs-meta-row">
              <span class="qs-meta-k">요청 일시</span>
              <span class="qs-meta-v">{{ docInfo?.requestedAt ?? '—' }}</span>
            </div>
            <div class="qs-meta-row">
              <span class="qs-meta-k">서명 만료</span>
              <span class="qs-meta-v">
                <span>{{ docInfo?.expiresAt ?? '—' }}</span>
                <div v-if="docInfo?.expiresAt" class="qs-meta-warn">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.7"/>
                    <path d="M12 7v5l3 2" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
                  </svg>
                  만료 전까지 서명해 주세요
                </div>
              </span>
            </div>
            <div v-if="docInfo?.hashSha3256" class="qs-meta-row">
              <span class="qs-meta-k">SHA3-256</span>
              <span class="qs-meta-v">
                <button
                  type="button"
                  :class="['qs-hash-toggle', { 'is-open': hashOpen }]"
                  :aria-expanded="hashOpen"
                  @click="hashOpen = !hashOpen"
                >
                  <span>{{ hashOpen ? hashFull : hashShort }}</span>
                  <span class="qs-hash-chevron" aria-hidden="true">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                      <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="1.8"
                        stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </span>
                </button>
                <div v-if="hashOpen" class="qs-hash-full">{{ docInfo.hashSha3256 }}</div>
              </span>
            </div>
          </div>
        </div>

        <!-- PDF 미리보기 -->
        <div class="qs-pdf">
          <div class="qs-pdf-head">
            <span class="qs-pdf-pages">미리보기</span>
            <button type="button" class="qs-btn qs-btn-ghost qs-btn-sm" @click="scrolled = true">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M4 9V4h5M20 9V4h-5M4 15v5h5M20 15v5h-5"
                  stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              끝까지 확인
            </button>
          </div>
          <!-- 실제 PDF 로드 시 iframe, 아닐 경우 플레이스홀더 -->
          <iframe v-if="pdfBlobUrl" :src="pdfBlobUrl" class="qs-pdf-frame" title="서명 요청 문서" />
          <div v-else class="qs-pdf-canvas">
            <div class="qs-pdf-page">
              <div class="qs-pdf-page-content">
                <div class="qs-pdf-line is-title" />
                <div class="qs-pdf-line is-mid" />
                <div class="qs-pdf-line" />
                <div class="qs-pdf-line is-short" />
                <div class="qs-pdf-line is-mid" />
                <div class="qs-pdf-line" />
                <div class="qs-pdf-line is-short" />
                <div class="qs-pdf-sig-area">서명란</div>
              </div>
            </div>
            <div class="qs-pdf-fade" aria-hidden="true" />
          </div>
          <div class="qs-pdf-foot">
            <span :class="['qs-pdf-scrolled', { 'is-done': scrolled }]">
              <span class="qs-dot-live" aria-hidden="true" />
              {{ scrolled ? '문서를 끝까지 확인했어요' : '스크롤해 끝까지 확인해 주세요' }}
            </span>
          </div>
        </div>

        <!-- 동의 체크박스 -->
        <div class="qs-consents" role="group" aria-label="서명 동의">
          <label class="qs-consent">
            <input type="checkbox" v-model="consent1" />
            <span class="qs-consent-box" aria-hidden="true">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.4"
                  stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
            <span class="qs-consent-text">
              문서 내용을 확인했으며 서명에 동의합니다
              <span class="qs-consent-sub">전체 페이지를 검토했습니다</span>
            </span>
          </label>
          <label class="qs-consent">
            <input type="checkbox" v-model="consent2" />
            <span class="qs-consent-box" aria-hidden="true">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.4"
                  stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
            <span class="qs-consent-text">
              ML-DSA 전자서명의 법적 효력에 동의합니다
              <span class="qs-consent-sub">전자서명법 및 전자거래 기본법 제4조에 따른 효력</span>
            </span>
          </label>
        </div>

        <div class="qs-sign-actions">
          <button
            type="button"
            class="qs-btn qs-btn-primary qs-btn-lg qs-btn-block"
            :disabled="!canSign"
            @click="handleSign"
          >
            {{ canSign ? '서명하기' : '동의 후 서명할 수 있어요' }}
            <svg v-if="canSign" width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2"
                stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>

        <p class="qs-sign-foot">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" aria-hidden="true"
            style="display:inline; vertical-align:-1px">
            <rect x="4" y="10" width="16" height="11" rx="3" stroke="currentColor" stroke-width="1.6"/>
            <path d="M8 10V7a4 4 0 0 1 8 0v3" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
          </svg>
          서명 데이터는 TLS 1.3 채널로 전송되며 위·변조가 불가능합니다.
        </p>
      </article>

      <!-- ── Step 3: 서명 완료 ── -->
      <article v-else-if="step === 3" class="qs-card qs-success-card" aria-labelledby="step3-title">
        <div class="qs-success-icon" aria-hidden="true">
          <svg width="44" height="44" viewBox="0 0 44 44" fill="none">
            <circle cx="22" cy="22" r="20" stroke="currentColor" stroke-width="2.4"
              class="qs-success-icon-circle"/>
            <path d="M13 22.5l6 6L31 16" stroke="currentColor" stroke-width="3"
              stroke-linecap="round" stroke-linejoin="round"
              class="qs-success-icon-tick"/>
          </svg>
        </div>
        <h1 id="step3-title" class="qs-success-title">서명이 완료되었습니다</h1>
        <p class="qs-success-desc">
          문서가 양자내성암호로 안전하게 서명되었어요.<br />
          요청자에게는 자동으로 알림이 전송됩니다.
        </p>

        <div class="qs-success-receipt">
          <div class="qs-receipt-row">
            <span class="qs-receipt-k">문서</span>
            <span class="qs-receipt-v">{{ docInfo?.filename ?? '서명 완료 문서' }}</span>
          </div>
          <div class="qs-receipt-row">
            <span class="qs-receipt-k">서명자</span>
            <span class="qs-receipt-v qs-mono">{{ email }}</span>
          </div>
          <div class="qs-receipt-row">
            <span class="qs-receipt-k">서명 일시</span>
            <span class="qs-receipt-v">{{ signedAt }}</span>
          </div>
          <div class="qs-receipt-row">
            <span class="qs-receipt-k">알고리즘</span>
            <span class="qs-receipt-v qs-mono">ML-DSA-65 + SHA3-256</span>
          </div>
        </div>

        <div class="qs-success-actions">
          <button
            type="button"
            class="qs-btn qs-btn-primary qs-btn-lg qs-btn-block"
            :disabled="isDownloadingSigned"
            @click="downloadSignedPdf"
          >
            <svg v-if="!isDownloadingSigned" width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M12 4v12m0 0l-4-4m4 4l4-4" stroke="currentColor" stroke-width="1.8"
                stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M5 19h14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            </svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none"
              style="animation: qs-signer-spin 1s linear infinite" aria-hidden="true">
              <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-opacity="0.25" stroke-width="2.4"/>
              <path d="M21 12a9 9 0 0 0-9-9" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"/>
            </svg>
            {{ isDownloadingSigned ? '다운로드 중…' : '서명된 PDF 다운로드' }}
          </button>
        </div>

        <div class="qs-success-foot">
          QuSign 계정이 있으신가요?
          <RouterLink to="/login">로그인하면</RouterLink>
          서명 이력을 한곳에서 관리할 수 있어요.
        </div>
      </article>

      <!-- 푸터 trust strip -->
      <div class="qs-footer" aria-hidden="true">
        <span class="qs-footer-item">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none">
            <rect x="4" y="10" width="16" height="11" rx="3" stroke="currentColor" stroke-width="1.6"/>
            <path d="M8 10V7a4 4 0 0 1 8 0v3" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
          </svg>
          TLS 1.3
        </span>
        <span class="qs-footer-sep" />
        <span class="qs-footer-item">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none">
            <path d="M12 3l8 3v6c0 4.5-3.4 8.5-8 9-4.6-.5-8-4.5-8-9V6l8-3z"
              stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/>
            <path d="M9 12.5l2 2 4-4.5" stroke="currentColor" stroke-width="1.7"
              stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          ML-DSA-65
        </span>
        <span class="qs-footer-sep" />
        <span>© 2026 QuSign Inc.</span>
      </div>
    </main>

    <!-- 서명 생성 오버레이 -->
    <Teleport to="body">
      <div v-if="signing" class="qs-overlay" role="dialog" aria-modal="true" aria-labelledby="signing-title">
        <div class="qs-signing-card">
          <div class="qs-signing-spinner" aria-hidden="true">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-opacity="0.25" stroke-width="2.4"/>
              <path d="M21 12a9 9 0 0 0-9-9" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"/>
            </svg>
          </div>
          <h2 id="signing-title" class="qs-signing-title">ML-DSA-65로 서명 생성 중…</h2>
          <p class="qs-signing-desc">잠시만 기다려 주세요. 보통 2초 이내에 완료돼요.</p>
          <div class="qs-signing-steps" aria-live="polite">
            <div v-for="(s, i) in SIGNING_STEPS" :key="i"
              :class="['qs-signing-step',
                { 'is-done': i < signingStep, 'is-active': i === signingStep }]">
              <span class="qs-signing-step-dot" aria-hidden="true">
                <svg v-if="i < signingStep" width="9" height="9" viewBox="0 0 24 24" fill="none">
                  <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="3"
                    stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
              <span>{{ s }}</span>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/lib/api'
import PublicTopbar from '@/components/layout/PublicTopbar.vue'

const route = useRoute()
const signToken = computed(() => (route.params.token as string) ?? '')

// ── 취소 상태 ──
const cancelled = ref(false)
const cancelledInfo = ref({ requester: '', cancelledAt: '' })

// ── Step 1: OTP ──
const email = ref('')
const emailTouched = ref(false)
const emailValid = computed(() => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value.trim()))
const otpSent = ref(false)
const isSending = ref(false)
const sendErr = ref<string | null>(null)
const otpCode = ref<string[]>(['', '', '', '', '', ''])
const otpRefs: (HTMLInputElement | null)[] = []

function setOtpRef(i: number, el: unknown) {
  otpRefs[i] = el instanceof HTMLInputElement ? el : null
}

function handleOtpFocus(event: Event) {
  (event.target as HTMLInputElement)?.select()
}

const hashFull = computed(() => docInfo.value?.hashSha3256 ?? '')
const hashShort = computed(() => {
  const h = docInfo.value?.hashSha3256 ?? ''
  return h ? `${h.slice(0, 16)}…${h.slice(-8)}` : ''
})
const timerSeconds = ref(180)
const isVerifying = ref(false)
const otpErr = ref<string | null>(null)
let timerInterval: ReturnType<typeof setInterval> | null = null

// ── Step 2 ──
const step = ref(1)
const signerToken = ref('')
const docInfo = ref<{
  filename: string
  requesterEmail: string
  requestedAt: string
  expiresAt: string
  hashSha3256: string
  pages?: number
} | null>(null)
const hashOpen = ref(false)
const pdfBlobUrl = ref('')
const scrolled = ref(false)
const consent1 = ref(false)
const consent2 = ref(false)
const canSign = computed(() => scrolled.value && consent1.value && consent2.value)

// ── 서명 ──
const signing = ref(false)
const signingStep = ref(0)
const SIGNING_STEPS = [
  '문서 해시 계산 (SHA3-256)',
  'ML-DSA-65 서명 생성',
  '타임스탬프 발급',
  '서명 검증 및 저장',
]
const signedAt = ref('')
const signErr = ref<string | null>(null)

// ── Step 3 ──
const isDownloadingSigned = ref(false)

// ─────────────────────────────────────
// 마운트 시 문서 정보 조회 (취소 여부 포함)
// ─────────────────────────────────────
onMounted(async () => {
  if (!signToken.value) return
  try {
    const res = await api.get<{
      data: {
        cancelled: boolean
        cancelledAt?: string
        requesterEmail: string
        documentName: string
        hashSha3256: string
        requestedAt: string
        expiresAt: string
        pages?: number
      }
    }>(`/api/public/signature-requests/${signToken.value}`)
    const d = res.data.data
    if (d.cancelled) {
      cancelled.value = true
      cancelledInfo.value = {
        requester: d.requesterEmail,
        cancelledAt: d.cancelledAt ?? '',
      }
    } else {
      docInfo.value = {
        filename: d.documentName,
        requesterEmail: d.requesterEmail,
        requestedAt: formatDate(d.requestedAt),
        expiresAt: formatDate(d.expiresAt),
        hashSha3256: d.hashSha3256,
        pages: d.pages,
      }
    }
  } catch {
    // 엔드포인트 미구현 시 무시 — OTP 플로우는 정상 진행
  }
})

onBeforeUnmount(() => {
  stopTimer()
  if (pdfBlobUrl.value) URL.revokeObjectURL(pdfBlobUrl.value)
})

// ─────────────────────────────────────
// 타이머
// ─────────────────────────────────────
function startTimer() {
  stopTimer()
  timerSeconds.value = 180
  timerInterval = setInterval(() => {
    timerSeconds.value--
    if (timerSeconds.value <= 0) stopTimer()
  }, 1000)
}

function stopTimer() {
  if (timerInterval) { clearInterval(timerInterval); timerInterval = null }
}

function fmtTimer(s: number) {
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${m}:${String(sec).padStart(2, '0')}`
}

// ─────────────────────────────────────
// OTP
// ─────────────────────────────────────
async function sendOtp() {
  if (!emailValid.value || isSending.value) return
  isSending.value = true
  sendErr.value = null
  try {
    await api.post(`/api/public/signature-requests/${signToken.value}/send-otp`, {
      email: email.value.trim(),
    })
    otpSent.value = true
    otpCode.value = ['', '', '', '', '', '']
    otpErr.value = null
    startTimer()
    await nextTick()
    otpRefs[0]?.focus()
  } catch (err: any) {
    sendErr.value = err?.response?.data?.message ?? '인증 코드 전송에 실패했어요. 잠시 후 다시 시도해 주세요.'
  } finally {
    isSending.value = false
  }
}

function handleOtpInput(idx: number, event: Event) {
  const raw = (event.target as HTMLInputElement).value
  const cleaned = raw.replace(/\D/g, '')
  if (cleaned.length > 1) {
    const next = ['', '', '', '', '', '']
    cleaned.slice(0, 6).split('').forEach((c, i) => { next[i] = c })
    otpCode.value = next
    const lastIdx = Math.min(cleaned.length, 5)
    nextTick(() => otpRefs[lastIdx]?.focus())
    otpErr.value = null
    return
  }
  const next = [...otpCode.value]
  next[idx] = cleaned.slice(0, 1)
  otpCode.value = next
  otpErr.value = null
  if (cleaned && idx < 5) nextTick(() => otpRefs[idx + 1]?.focus())
}

function handleOtpKeydown(idx: number, e: KeyboardEvent) {
  if (e.key === 'Backspace' && !otpCode.value[idx] && idx > 0) {
    nextTick(() => otpRefs[idx - 1]?.focus())
  } else if (e.key === 'ArrowLeft' && idx > 0) {
    nextTick(() => otpRefs[idx - 1]?.focus())
  } else if (e.key === 'ArrowRight' && idx < 5) {
    nextTick(() => otpRefs[idx + 1]?.focus())
  } else if (e.key === 'Enter' && otpCode.value.join('').length === 6) {
    verifyOtp()
  }
}

async function verifyOtp() {
  const code = otpCode.value.join('')
  if (code.length < 6 || isVerifying.value) return
  isVerifying.value = true
  otpErr.value = null
  try {
    const res = await api.post<{ data: { signerToken: string } }>(
      `/api/public/signature-requests/${signToken.value}/verify-otp`,
      { email: email.value.trim(), code }
    )
    signerToken.value = res.data.data.signerToken
    stopTimer()
    await fetchDocInfoWithToken()
    await fetchPdf()
    step.value = 2
  } catch (err: any) {
    otpErr.value = err?.response?.data?.message ?? '인증 코드가 일치하지 않아요.'
  } finally {
    isVerifying.value = false
  }
}

// ─────────────────────────────────────
// 문서 정보 (토큰 인증 후)
// ─────────────────────────────────────
async function fetchDocInfoWithToken() {
  if (docInfo.value) return
  try {
    const res = await api.get<{
      data: {
        documentName: string
        requesterEmail: string
        requestedAt: string
        expiresAt: string
        hashSha3256: string
        pages?: number
      }
    }>(`/api/public/signature-requests/${signToken.value}`, {
      headers: signerToken.value ? { Authorization: `Bearer ${signerToken.value}` } : {},
    })
    const d = res.data.data
    docInfo.value = {
      filename: d.documentName,
      requesterEmail: d.requesterEmail,
      requestedAt: formatDate(d.requestedAt),
      expiresAt: formatDate(d.expiresAt),
      hashSha3256: d.hashSha3256,
      pages: d.pages,
    }
  } catch {
    // 조회 실패 시 빈 정보로 진행
  }
}

async function fetchPdf() {
  try {
    const res = await api.get(`/api/public/signature-requests/${signToken.value}/document`, {
      responseType: 'blob',
      headers: signerToken.value ? { Authorization: `Bearer ${signerToken.value}` } : {},
    })
    if (pdfBlobUrl.value) URL.revokeObjectURL(pdfBlobUrl.value)
    pdfBlobUrl.value = URL.createObjectURL(res.data)
  } catch {
    // 미리보기 실패 시 플레이스홀더 유지
  }
}

// ─────────────────────────────────────
// 서명
// ─────────────────────────────────────
async function handleSign() {
  if (!canSign.value || signing.value) return
  signing.value = true
  signingStep.value = 0
  signErr.value = null

  // 시각적 스텝 진행
  const visualDone = new Promise<void>(resolve => {
    let i = 0
    const iv = setInterval(() => {
      i++
      signingStep.value = i
      if (i >= SIGNING_STEPS.length) { clearInterval(iv); resolve() }
    }, 700)
  })

  // API 호출
  const apiDone = (async () => {
    try {
      await api.post(
        `/api/public/signature-requests/${signToken.value}/sign`,
        { email: email.value.trim() },
        signerToken.value ? { headers: { Authorization: `Bearer ${signerToken.value}` } } : undefined
      )
      signedAt.value = new Date().toLocaleString('ko-KR', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit',
      })
    } catch (err: any) {
      signErr.value = err?.response?.data?.message ?? '서명에 실패했어요. 다시 시도해 주세요.'
    }
  })()

  await Promise.all([visualDone, apiDone])
  signing.value = false
  if (!signErr.value) {
    step.value = 3
  }
}

// ─────────────────────────────────────
// 서명된 PDF 다운로드
// ─────────────────────────────────────
async function downloadSignedPdf() {
  isDownloadingSigned.value = true
  try {
    const res = await api.get(`/api/public/signature-requests/${signToken.value}/signed-document`, {
      responseType: 'blob',
      headers: signerToken.value ? { Authorization: `Bearer ${signerToken.value}` } : {},
    })
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = `signed_${docInfo.value?.filename ?? 'document.pdf'}`
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    alert('서명된 PDF 다운로드에 실패했어요.')
  } finally {
    isDownloadingSigned.value = false
  }
}

// ─────────────────────────────────────
// 유틸
// ─────────────────────────────────────
function formatDate(d: string | null | undefined) {
  if (!d) return '—'
  const dt = new Date(d)
  if (isNaN(dt.getTime())) return d
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}.${pad(dt.getMonth() + 1)}.${pad(dt.getDate())} ${pad(dt.getHours())}:${pad(dt.getMinutes())}`
}
</script>
