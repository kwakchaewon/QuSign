<template>
  <div class="qs-page qs-signer-page">
    <PublicTopbar title="QuSign" subtitle="전자서명 요청" logo-to="/" show-pqc-badge />

    <main class="qs-main">
      <!-- 스텝퍼 (정상 진행 중일 때만) -->
      <div v-if="!accessError && !cancelled && !alreadySigned" class="qs-stepper" aria-label="서명 진행 단계">
        <div :class="['qs-step-item', { 'is-active': step === 1, 'is-done': step > 1 }]">
          <span class="qs-step-num">
            <svg v-if="step > 1" width="12" height="12" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.4"
                stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <template v-else>1</template>
          </span>
          <span class="qs-step-label">문서 검토 · 서명</span>
        </div>
        <span :class="['qs-step-bar', { 'is-done': step > 1 }]" />
        <div :class="['qs-step-item', { 'is-active': step === 2 }]">
          <span class="qs-step-num">2</span>
          <span class="qs-step-label">완료</span>
        </div>
      </div>

      <!-- ── 접근 오류 ── -->
      <article v-if="accessError" class="qs-card qs-cancelled-card" aria-labelledby="access-error-title">
        <div class="qs-cancelled-icon" aria-hidden="true">
          <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
            <circle cx="32" cy="32" r="28" stroke="currentColor" stroke-width="2"
              fill="var(--surface-muted)"/>
            <path d="M22 22l20 20M42 22L22 42" stroke="currentColor" stroke-width="2.4"
              stroke-linecap="round"/>
          </svg>
        </div>
        <h1 id="access-error-title" class="qs-cancelled-title">{{ accessError.title }}</h1>
        <p class="qs-cancelled-desc">{{ accessError.desc }}</p>
      </article>

      <!-- ── 취소 상태 ── -->
      <article v-else-if="cancelled" class="qs-card qs-cancelled-card" aria-labelledby="cancelled-title">
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
        <div v-if="cancelledInfo.requester" class="qs-cancelled-meta">
          <div class="qs-cancelled-meta-row">
            <span class="qs-cancelled-meta-k">요청자</span>
            <span class="qs-cancelled-meta-v">{{ cancelledInfo.requester }}</span>
          </div>
        </div>
      </article>

      <!-- ── 이미 서명 완료 상태 ── -->
      <article v-else-if="alreadySigned" class="qs-card qs-cancelled-card" aria-labelledby="already-signed-title">
        <div class="qs-cancelled-icon qs-already-signed-icon" aria-hidden="true">
          <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
            <circle cx="32" cy="32" r="28" stroke="currentColor" stroke-width="2"
              fill="var(--color-success-bg)"/>
            <path d="M20 33l8 8 16-16" stroke="currentColor" stroke-width="2.4"
              stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1 id="already-signed-title" class="qs-cancelled-title">이미 서명이 완료된 요청입니다</h1>
        <p class="qs-cancelled-desc">
          이 문서에 대한 서명이 이미 완료되었습니다.<br />
          서명 사본이 필요하다면 요청자에게 문의해 주세요.
        </p>
        <div v-if="alreadySignedInfo.requester" class="qs-cancelled-meta">
          <div class="qs-cancelled-meta-row">
            <span class="qs-cancelled-meta-k">요청자</span>
            <span class="qs-cancelled-meta-v">{{ alreadySignedInfo.requester }}</span>
          </div>
          <div v-if="alreadySignedInfo.documentName" class="qs-cancelled-meta-row">
            <span class="qs-cancelled-meta-k">문서</span>
            <span class="qs-cancelled-meta-v">{{ alreadySignedInfo.documentName }}</span>
          </div>
        </div>
      </article>

      <!-- ── Step 1: 문서 검토 + 서명 ── -->
      <article v-else-if="step === 1" class="qs-card" aria-labelledby="step1-title">
        <header class="qs-card-head">
          <h1 id="step1-title" class="qs-card-title">문서 검토 · 서명</h1>
          <p class="qs-card-desc">
            아래 문서 내용을 확인하고, 동의 후 서명을 진행해 주세요.<br />
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
              <div class="qs-doc-meta-name">
                <template v-if="docInfo?.isBundle && docInfo.documents.length > 1">
                  {{ docInfo.documents[0].filename }} 외 {{ docInfo.documents.length - 1 }}건
                </template>
                <template v-else>
                  {{ docInfo?.filename ?? '서명 요청 문서' }}
                </template>
              </div>
              <div class="qs-doc-meta-size">
                <span v-if="docInfo?.isBundle && docInfo.documents.length > 1">
                  {{ docInfo.documents.length }}개 PDF 묶음 서명
                </span>
                <span v-else>{{ docInfo?.pages ? `${docInfo.pages}페이지` : 'PDF' }}</span>
              </div>
              <!-- 번들 파일 목록 -->
              <div v-if="docInfo?.isBundle && docInfo.documents.length > 1" style="margin-top:8px">
                <div v-for="doc in docInfo.documents" :key="doc.index"
                  style="display:flex;align-items:center;gap:6px;font-size:12px;color:var(--text-secondary);margin-top:3px">
                  <svg width="10" height="13" viewBox="0 0 28 36" fill="none">
                    <rect width="28" height="36" rx="4" fill="var(--color-error-bg)"/>
                    <text x="4" y="22" font-size="7" font-weight="800" fill="var(--color-error)"
                      font-family="monospace">PDF</text>
                  </svg>
                  {{ doc.filename }}
                </div>
              </div>
            </div>
          </div>
          <div class="qs-meta-list">
            <div class="qs-meta-row">
              <span class="qs-meta-k">요청자</span>
              <span class="qs-meta-v qs-mono">{{ docInfo?.requesterEmail ?? '—' }}</span>
            </div>
            <div v-if="docInfo?.message" class="qs-meta-row qs-meta-row--message">
              <span class="qs-meta-k">요청 메시지</span>
              <span class="qs-meta-v qs-meta-message">{{ docInfo.message }}</span>
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
            <span class="qs-pdf-pages">문서 미리보기</span>
            <button
              v-if="pdfBlobUrl"
              type="button"
              class="qs-btn qs-btn-ghost qs-btn-sm"
              @click="largeViewOpen = true"
            >
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M4 9V4h5M20 9V4h-5M4 15v5h5M20 15v5h-5"
                  stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              크게 보기
            </button>
          </div>
          <div :class="['qs-pdf-notice', { 'is-done': scrolled }]" role="status">
            <svg v-if="scrolled" width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.4"
                stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.7"/>
              <path d="M12 8v4M12 15.5v.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
            </svg>
            <span>
              {{ scrolled
                ? '문서를 모두 확인했어요. 아래에서 서명을 진행해 주세요.'
                : '문서를 끝까지 스크롤해야 서명할 수 있어요.' }}
            </span>
          </div>
          <PdfViewer v-if="pdfBlobUrl" :src="pdfBlobUrl" @scrolled-to-end="scrolled = true" />
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

        <!-- 비밀번호 입력 -->
        <div class="qs-field" style="margin-top: 16px">
          <label class="qs-label" for="sign-password">서명 비밀번호</label>
          <div class="qs-input">
            <span class="qs-input-icon" aria-hidden="true">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <rect x="4" y="10" width="16" height="11" rx="3" stroke="currentColor" stroke-width="1.6"/>
                <path d="M8 10V7a4 4 0 0 1 8 0v3" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
              </svg>
            </span>
            <input
              id="sign-password"
              v-model="password"
              type="password"
              placeholder="계정 비밀번호를 입력해 주세요"
              autocomplete="current-password"
              @keydown.enter="handleSign"
            />
          </div>
          <span v-if="signErr" class="qs-help is-error" style="margin-top: 6px">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M12 4l9 16H3l9-16z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/>
              <path d="M12 10v4M12 17v.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
            </svg>
            {{ signErr }}
          </span>
        </div>

        <div class="qs-sign-actions">
          <button
            type="button"
            class="qs-btn qs-btn-primary qs-btn-lg qs-btn-block"
            :disabled="!canSign"
            @click="handleSign"
          >
            {{ canSign ? '서명하기' : '동의 및 비밀번호 입력 후 서명할 수 있어요' }}
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

      <!-- ── Step 2: 서명 완료 ── -->
      <article v-else-if="step === 2" class="qs-card qs-success-card" aria-labelledby="step2-title">
        <div class="qs-success-icon" aria-hidden="true">
          <svg width="44" height="44" viewBox="0 0 44 44" fill="none">
            <circle cx="22" cy="22" r="20" stroke="currentColor" stroke-width="2.4"
              class="qs-success-icon-circle"/>
            <path d="M13 22.5l6 6L31 16" stroke="currentColor" stroke-width="3"
              stroke-linecap="round" stroke-linejoin="round"
              class="qs-success-icon-tick"/>
          </svg>
        </div>
        <h1 id="step2-title" class="qs-success-title">서명이 완료되었습니다</h1>
        <p class="qs-success-desc">
          문서가 양자내성암호로 안전하게 서명되었어요.<br />
          요청자에게는 자동으로 알림이 전송됩니다.
        </p>

        <div class="qs-success-receipt">
          <div class="qs-receipt-row">
            <span class="qs-receipt-k">문서</span>
            <span class="qs-receipt-v">
              <template v-if="docInfo?.isBundle && docInfo.documents.length > 1">
                {{ docInfo.documents[0].filename }} 외 {{ docInfo.documents.length - 1 }}건
              </template>
              <template v-else>{{ docInfo?.filename ?? '서명 완료 문서' }}</template>
            </span>
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
          <!-- 번들: 각 문서 개별 다운로드 -->
          <template v-if="docInfo?.isBundle && docInfo.documents.length > 1">
            <button
              v-for="doc in docInfo.documents"
              :key="doc.index"
              type="button"
              class="qs-btn qs-btn-secondary qs-btn-lg qs-btn-block"
              :disabled="downloadingIndex === doc.index"
              @click="downloadSignedBundleDoc(doc.index, doc.filename)"
              style="margin-bottom:6px"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M12 4v12m0 0l-4-4m4 4l4-4" stroke="currentColor" stroke-width="1.8"
                  stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M5 19h14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
              </svg>
              {{ downloadingIndex === doc.index ? '다운로드 중…' : doc.filename }}
            </button>
          </template>
          <!-- 단건 다운로드 -->
          <template v-else>
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
          </template>
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

    <!-- 크게 보기 모달 -->
    <Teleport to="body">
      <div
        v-if="largeViewOpen"
        class="qs-large-viewer-backdrop"
        role="dialog"
        aria-modal="true"
        aria-label="문서 크게 보기"
        @click.self="largeViewOpen = false"
      >
        <div class="qs-large-viewer-card">
          <div class="qs-large-viewer-head">
            <span class="qs-large-viewer-title">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M6 2h9l5 5v13a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z"
                  stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
                <path d="M14 2v5h5" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
              </svg>
              {{ docInfo?.filename ?? '서명 요청 문서' }}
            </span>
            <div class="qs-large-viewer-head-right">
              <span v-if="scrolled" class="qs-large-viewer-done">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.4"
                    stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                확인 완료
              </span>
              <button
                type="button"
                class="qs-large-viewer-close"
                aria-label="닫기"
                @click="largeViewOpen = false"
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2"
                    stroke-linecap="round"/>
                </svg>
              </button>
            </div>
          </div>
          <PdfViewer
            :src="pdfBlobUrl"
            height="72vh"
            @scrolled-to-end="scrolled = true"
          />
          <div class="qs-large-viewer-foot">
            <span v-if="!scrolled" class="qs-large-viewer-hint">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.7"/>
                <path d="M12 8v4M12 15.5v.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
              </svg>
              끝까지 스크롤하면 서명 동의가 가능해요
            </span>
            <button
              type="button"
              class="qs-btn qs-btn-primary"
              :class="{ 'qs-btn-ghost': !scrolled }"
              @click="largeViewOpen = false"
            >
              {{ scrolled ? '확인 완료 · 닫기' : '닫기' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

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
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/lib/api'
import PublicTopbar from '@/components/layout/PublicTopbar.vue'
import PdfViewer from '@/components/PdfViewer.vue'

const route = useRoute()
const signToken = computed(() => (route.params.token as string) ?? '')

const email = localStorage.getItem('qusign:email') ?? ''

// ── 접근 오류 ──
const accessError = ref<{ title: string; desc: string } | null>(null)

// ── 취소 상태 ──
const cancelled = ref(false)
const cancelledInfo = ref({ requester: '' })

// ── 이미 서명 완료 상태 ──
const alreadySigned = ref(false)
const alreadySignedInfo = ref({ requester: '', documentName: '' })

// ── Step 1 ──
const step = ref(1)

interface BundleDoc { index: number; filename: string; hashSha3256: string }

const docInfo = ref<{
  filename: string
  requesterEmail: string
  message: string | null
  requestedAt: string
  expiresAt: string
  hashSha3256: string
  pages?: number
  isBundle: boolean
  documents: BundleDoc[]
} | null>(null)
const hashOpen = ref(false)
const hashFull = computed(() => docInfo.value?.hashSha3256 ?? '')
const hashShort = computed(() => {
  const h = docInfo.value?.hashSha3256 ?? ''
  return h ? `${h.slice(0, 16)}…${h.slice(-8)}` : ''
})
const pdfBlobUrl = ref('')
const scrolled = ref(false)
const largeViewOpen = ref(false)
const consent1 = ref(false)
const consent2 = ref(false)
const password = ref('')
const canSign = computed(() =>
  scrolled.value && consent1.value && consent2.value && password.value.trim().length > 0
)

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

// ── Step 2 ──
const isDownloadingSigned = ref(false)
const downloadingIndex = ref<number | null>(null)

// ─────────────────────────────────────
// 마운트 시 서명 요청 정보 + PDF 조회
// ─────────────────────────────────────
onMounted(async () => {
  if (!signToken.value) return
  try {
    const res = await api.get<{
      data: {
        cancelled: boolean
        signed: boolean
        requesterEmail: string
        documentName: string
        message: string | null
        hashSha3256: string
        requestedAt: string
        expiresAt: string
        isBundle: boolean
        documents: BundleDoc[]
      }
    }>(`/api/signature-requests/${signToken.value}/info`)
    const d = res.data.data
    if (d.cancelled) {
      cancelled.value = true
      cancelledInfo.value = { requester: d.requesterEmail }
    } else if (d.signed) {
      alreadySigned.value = true
      alreadySignedInfo.value = { requester: d.requesterEmail, documentName: d.documentName }
    } else {
      docInfo.value = {
        filename: d.documentName,
        requesterEmail: d.requesterEmail,
        message: d.message ?? null,
        requestedAt: formatDate(d.requestedAt),
        expiresAt: formatDate(d.expiresAt),
        hashSha3256: d.hashSha3256,
        isBundle: d.isBundle ?? false,
        documents: d.documents ?? [],
      }
      await fetchPdf()
    }
  } catch (err: any) {
    const status = err?.response?.status
    if (status === 403) {
      accessError.value = {
        title: '접근 권한이 없습니다',
        desc: '이 서명 요청의 서명자가 아닙니다.\n서명 요청자에게 문의해 주세요.',
      }
    } else if (status === 404) {
      accessError.value = {
        title: '요청을 찾을 수 없습니다',
        desc: '삭제되었거나 존재하지 않는 서명 요청이에요.\nURL을 다시 확인해 주세요.',
      }
    }
    // 401은 api 인터셉터가 로그인 페이지로 리다이렉트
  }
})

onBeforeUnmount(() => {
  if (pdfBlobUrl.value) URL.revokeObjectURL(pdfBlobUrl.value)
})

async function fetchPdf() {
  try {
    const res = await api.get(`/api/signature-requests/${signToken.value}/document`, {
      responseType: 'blob',
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

  const visualDone = new Promise<void>(resolve => {
    let i = 0
    const iv = setInterval(() => {
      i++
      signingStep.value = i
      if (i >= SIGNING_STEPS.length) { clearInterval(iv); resolve() }
    }, 700)
  })

  const apiDone = (async () => {
    try {
      await api.post(`/api/signature-requests/${signToken.value}/sign`, {
        password: password.value,
      })
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
    step.value = 2
  }
}

// ─────────────────────────────────────
// 서명된 PDF 다운로드
// ─────────────────────────────────────
async function downloadSignedPdf() {
  isDownloadingSigned.value = true
  try {
    const res = await api.get(`/api/signature-requests/${signToken.value}/signed-document`, {
      responseType: 'blob',
    })
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = (docInfo.value?.filename ?? 'document.pdf').replace(/\.pdf$/i, '') + '_qusigned.pdf'
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    alert('서명된 PDF 다운로드에 실패했어요.')
  } finally {
    isDownloadingSigned.value = false
  }
}

async function downloadSignedBundleDoc(index: number, filename: string) {
  downloadingIndex.value = index
  try {
    const res = await api.get(
      `/api/signature-requests/${signToken.value}/signed-bundle-documents/${index}`,
      { responseType: 'blob' }
    )
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = filename.replace(/\.pdf$/i, '') + '_qusigned.pdf'
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    alert('다운로드에 실패했어요.')
  } finally {
    downloadingIndex.value = null
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
