<template>
  <div class="qs-page">
    <!-- ===== Topbar ===== -->
    <header class="qs-topbar">
      <div class="qs-topbar-inner">
        <RouterLink class="qs-topbar-brand" to="/documents">
          <QuSignMark variant="badge" :size="28" />
          <span class="qs-topbar-name">QuSign</span>
        </RouterLink>
        <nav class="qs-topbar-nav">
          <RouterLink class="qs-nav-link" to="/documents">내 문서</RouterLink>
          <RouterLink class="qs-nav-link" to="/request">서명 요청</RouterLink>
          <RouterLink class="qs-nav-link is-active" to="/settings">설정</RouterLink>
        </nav>
        <div class="qs-topbar-right">
          <ThemeToggle :theme="theme" @change="handleThemeToggle" />
          <div class="qs-user">
            <div class="qs-user-avatar">{{ userInitial }}</div>
            <span class="qs-user-email">{{ userEmail }}</span>
          </div>
          <button class="qs-icon-btn" title="로그아웃" @click="handleLogout">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>
    </header>

    <!-- ===== Main ===== -->
    <main class="qs-settings-main">
      <div class="qs-settings-head">
        <h1 class="qs-settings-title">설정</h1>
        <p class="qs-settings-sub">계정과 보안, 알림을 한 곳에서 관리하세요.</p>
      </div>

      <div class="qs-settings-layout">
        <!-- Sidebar anchor nav -->
        <aside class="qs-settings-aside" aria-label="설정 섹션">
          <button
            v-for="s in SECTIONS" :key="s.id"
            type="button"
            :class="['qs-anchor', { 'is-active': activeSection === s.id, 'is-danger': s.danger }]"
            @click="jumpTo(s.id)">
            <span class="qs-anchor-num">{{ s.num }}</span>
            <span>{{ s.label }}</span>
          </button>
        </aside>

        <!-- Content -->
        <div class="qs-settings-content">

          <!-- ===== 01 프로필 ===== -->
          <div id="sec-profile">
            <section class="qs-card">
              <div class="qs-card-head">
                <h2 class="qs-card-title">프로필</h2>
                <p class="qs-card-desc">QuSign에 등록된 계정 정보입니다.</p>
              </div>
              <div class="qs-profile-row">
                <div class="qs-profile-avatar" aria-hidden="true">{{ userInitial }}</div>
                <div class="qs-profile-info">
                  <div class="qs-profile-email">{{ userEmail }}</div>
                  <div class="qs-profile-meta">
                    <span class="qs-meta-pill">
                      <svg width="11" height="11" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <rect x="5" y="11" width="14" height="10" rx="2" stroke="currentColor" stroke-width="1.8"/>
                        <path d="M8 11V8a4 4 0 0 1 8 0v3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                      </svg>
                      이메일 계정
                    </span>
                    <span class="qs-meta-pill">ML-DSA-65</span>
                  </div>
                </div>
              </div>
            </section>
          </div>

          <!-- ===== 02 보안 ===== -->
          <div id="sec-security">
            <!-- 비밀번호 변경 -->
            <section class="qs-card">
              <div class="qs-card-head">
                <h2 class="qs-card-title">비밀번호 변경</h2>
                <p class="qs-card-desc">8자 이상, 대소문자 · 숫자 · 특수문자를 조합하면 더 안전해요.</p>
              </div>
              <form class="qs-form" @submit.prevent="handlePasswordChange">
                <!-- 현재 비밀번호 -->
                <div class="qs-field">
                  <label class="qs-field-label" for="pw-current">현재 비밀번호</label>
                  <div class="qs-input-wrap">
                    <input
                      id="pw-current"
                      :type="showPwCurrent ? 'text' : 'password'"
                      class="qs-input qs-input-with-trail"
                      v-model="pwCurrent"
                      placeholder="현재 비밀번호 입력"
                      autocomplete="current-password" />
                    <span class="qs-input-trail">
                      <button type="button" class="qs-input-eye"
                        :aria-label="showPwCurrent ? '숨기기' : '보기'"
                        @click="showPwCurrent = !showPwCurrent">
                        <svg v-if="!showPwCurrent" width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                          <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
                          <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.6"/>
                        </svg>
                        <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                          <path d="M3 3l18 18" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
                          <path d="M10.6 10.6a2 2 0 0 0 2.8 2.8" stroke="currentColor" stroke-width="1.6"/>
                          <path d="M9.5 5.4A10 10 0 0 1 12 5c6.5 0 10 7 10 7a17 17 0 0 1-3.1 3.9M6.2 6.7C3.4 8.4 2 12 2 12s3.5 7 10 7c1.6 0 3-.3 4.3-.8" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
                        </svg>
                      </button>
                    </span>
                  </div>
                </div>

                <!-- 새 비밀번호 -->
                <div class="qs-field">
                  <label class="qs-field-label" for="pw-new">새 비밀번호</label>
                  <div class="qs-input-wrap">
                    <input
                      id="pw-new"
                      :type="showPwNew ? 'text' : 'password'"
                      :class="['qs-input', 'qs-input-with-trail', { 'is-error': pwTooShort }]"
                      v-model="pwNew"
                      placeholder="8자 이상"
                      autocomplete="new-password" />
                    <span class="qs-input-trail">
                      <button type="button" class="qs-input-eye"
                        :aria-label="showPwNew ? '숨기기' : '보기'"
                        @click="showPwNew = !showPwNew">
                        <svg v-if="!showPwNew" width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                          <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
                          <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.6"/>
                        </svg>
                        <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                          <path d="M3 3l18 18" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
                          <path d="M10.6 10.6a2 2 0 0 0 2.8 2.8" stroke="currentColor" stroke-width="1.6"/>
                          <path d="M9.5 5.4A10 10 0 0 1 12 5c6.5 0 10 7 10 7a17 17 0 0 1-3.1 3.9M6.2 6.7C3.4 8.4 2 12 2 12s3.5 7 10 7c1.6 0 3-.3 4.3-.8" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
                        </svg>
                      </button>
                    </span>
                  </div>
                  <div v-if="pwNew.length > 0" class="qs-strength">
                    <div class="qs-strength-bars">
                      <span :class="['qs-strength-bar', pwStrength.score >= 1 ? pwStrengthClass : '']"></span>
                      <span :class="['qs-strength-bar', pwStrength.score >= 2 ? pwStrengthClass : '']"></span>
                      <span :class="['qs-strength-bar', pwStrength.score >= 3 ? pwStrengthClass : '']"></span>
                    </div>
                    <span :class="['qs-strength-label', pwStrengthClass]">{{ pwStrength.label }}</span>
                  </div>
                  <div v-if="pwTooShort" class="qs-error-msg">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.7"/>
                      <path d="M12 7v6M12 16.5v.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    최소 8자 이상이어야 해요
                  </div>
                </div>

                <!-- 새 비밀번호 확인 -->
                <div class="qs-field">
                  <label class="qs-field-label" for="pw-confirm">새 비밀번호 확인</label>
                  <div class="qs-input-wrap">
                    <input
                      id="pw-confirm"
                      :type="showPwNew ? 'text' : 'password'"
                      :class="['qs-input', { 'is-error': pwMismatch }]"
                      v-model="pwConfirm"
                      placeholder="새 비밀번호 다시 입력"
                      autocomplete="new-password" />
                  </div>
                  <div v-if="pwMismatch" class="qs-error-msg">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.7"/>
                      <path d="M12 7v6M12 16.5v.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    비밀번호가 일치하지 않아요
                  </div>
                </div>

                <div class="qs-form-foot">
                  <button type="submit" class="qs-btn qs-btn-primary qs-btn-md"
                    :disabled="!canSubmitPw">
                    <span v-if="pwSubmitting" class="qs-btn-spinner" aria-hidden="true"></span>
                    <span>{{ pwSubmitting ? '변경 중…' : '비밀번호 변경' }}</span>
                  </button>
                  <div v-if="pwSuccess" class="qs-success-msg">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    비밀번호가 변경되었어요
                  </div>
                </div>
              </form>
            </section>

            <div style="height: 16px;"></div>

            <!-- 보안 토글 -->
            <section class="qs-card">
              <div class="qs-card-head">
                <h2 class="qs-card-title">보안</h2>
                <p class="qs-card-desc">서명과 로그인의 안전성을 향상시키는 옵션들입니다.</p>
              </div>

              <!-- PQC (locked) -->
              <div class="qs-toggle-row">
                <div class="qs-toggle-info">
                  <div class="qs-toggle-title">
                    양자내성 서명 (ML-DSA-65)
                    <span class="qs-toggle-locked">
                      <svg width="11" height="11" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <rect x="5" y="11" width="14" height="10" rx="2" stroke="currentColor" stroke-width="1.8"/>
                        <path d="M8 11V8a4 4 0 0 1 8 0v3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                      </svg>
                      기본
                    </span>
                  </div>
                  <div class="qs-toggle-desc">모든 서명을 ML-DSA-65로 생성해 양자컴퓨터에 대비합니다.</div>
                </div>
                <div class="qs-toggle-side">
                  <button type="button" class="qs-switch is-on" disabled aria-pressed="true" aria-label="양자내성 서명"></button>
                </div>
              </div>

              <!-- 2FA -->
              <div class="qs-toggle-row">
                <div class="qs-toggle-info">
                  <div class="qs-toggle-title">2단계 인증 (TOTP)</div>
                  <div class="qs-toggle-desc">로그인 시 인증 앱의 6자리 코드를 입력합니다.</div>
                </div>
                <div class="qs-toggle-side">
                  <span v-if="toggleBusy['twoFA']" class="qs-mini-spinner" aria-hidden="true"></span>
                  <span :class="['qs-saved', { 'is-show': toggleSaved['twoFA'] }]">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    저장됨
                  </span>
                  <button type="button"
                    :class="['qs-switch', { 'is-on': secTwoFA }]"
                    :aria-pressed="secTwoFA"
                    aria-label="2단계 인증"
                    @click="updateToggle('twoFA', () => secTwoFA = !secTwoFA)"></button>
                </div>
              </div>

              <!-- Session lock -->
              <div class="qs-toggle-row">
                <div class="qs-toggle-info">
                  <div class="qs-toggle-title">자리비움 세션 자동 잠금</div>
                  <div class="qs-toggle-desc">15분간 활동이 없으면 자동으로 로그아웃됩니다.</div>
                </div>
                <div class="qs-toggle-side">
                  <span v-if="toggleBusy['sessionLock']" class="qs-mini-spinner" aria-hidden="true"></span>
                  <span :class="['qs-saved', { 'is-show': toggleSaved['sessionLock'] }]">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    저장됨
                  </span>
                  <button type="button"
                    :class="['qs-switch', { 'is-on': secSession }]"
                    :aria-pressed="secSession"
                    aria-label="세션 자동 잠금"
                    @click="updateToggle('sessionLock', () => secSession = !secSession)"></button>
                </div>
              </div>
            </section>
          </div>

          <!-- ===== 03 알림 ===== -->
          <div id="sec-notify">
            <section class="qs-card">
              <div class="qs-card-head">
                <h2 class="qs-card-title">알림</h2>
                <p class="qs-card-desc">중요한 항목만 골라 받아볼 수 있어요.</p>
              </div>

              <div v-for="t in notifyToggles" :key="t.key" class="qs-toggle-row">
                <div class="qs-toggle-info">
                  <div class="qs-toggle-title">{{ t.title }}</div>
                  <div class="qs-toggle-desc">{{ t.desc }}</div>
                </div>
                <div class="qs-toggle-side">
                  <span v-if="toggleBusy[t.key]" class="qs-mini-spinner" aria-hidden="true"></span>
                  <span :class="['qs-saved', { 'is-show': toggleSaved[t.key] }]">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    저장됨
                  </span>
                  <button type="button"
                    :class="['qs-switch', { 'is-on': t.value.value }]"
                    :aria-pressed="t.value.value"
                    :aria-label="t.title"
                    @click="updateToggle(t.key, () => t.value.value = !t.value.value)"></button>
                </div>
              </div>
            </section>
          </div>

          <!-- ===== 04 계정 삭제 ===== -->
          <div id="sec-danger">
            <section class="qs-card qs-card-danger">
              <div class="qs-card-head">
                <h2 class="qs-card-title">계정 삭제</h2>
                <p class="qs-card-desc">계정을 삭제하면 다음 데이터가 영구적으로 사라집니다.</p>
              </div>
              <ul class="qs-danger-list">
                <li>모든 PDF 문서와 양자내성 서명 기록</li>
                <li>서명 검증에 사용되는 공개키 · 검증 로그</li>
                <li>발송한 서명 요청 및 받은 요청 내역</li>
              </ul>
              <button type="button" class="qs-btn qs-btn-md qs-btn-danger-outline"
                @click="openDeleteModal">
                계정 삭제 요청
              </button>
            </section>
          </div>

        </div>
      </div>
    </main>

    <!-- ===== Toast ===== -->
    <div :class="['qs-toast-wrap', { 'is-show': !!toast }]" aria-live="polite">
      <div v-if="toast" class="qs-toast">
        <span class="qs-toast-dot" aria-hidden="true"></span>
        <span>{{ toast }}</span>
      </div>
    </div>

    <!-- ===== Delete Modal ===== -->
    <div v-if="showDeleteModal" class="qs-modal-backdrop"
      role="dialog" aria-modal="true" aria-labelledby="del-modal-title"
      @click.self="closeDeleteModal">
      <div class="qs-modal">
        <div class="qs-modal-icon" aria-hidden="true">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.7"/>
            <path d="M12 7v6M12 16.5v.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <h3 id="del-modal-title" class="qs-modal-title">정말 계정을 삭제할까요?</h3>
        <p class="qs-modal-desc">
          이 작업은 되돌릴 수 없어요. 진행하려면 아래에 본인 이메일을 정확히 입력해 주세요.
        </p>
        <span class="qs-confirm-email">{{ userEmail }}</span>
        <div class="qs-field">
          <input
            ref="deleteInputEl"
            type="email"
            class="qs-input"
            v-model="deleteEmailInput"
            placeholder="이메일 입력"
            autocomplete="off"
            @keydown.escape="closeDeleteModal" />
        </div>
        <div class="qs-modal-actions">
          <button type="button" class="qs-btn qs-btn-secondary qs-btn-md"
            :disabled="deleteSubmitting"
            @click="closeDeleteModal">
            취소
          </button>
          <button type="button" class="qs-btn qs-btn-md qs-btn-danger"
            :disabled="!deleteEmailMatch || deleteSubmitting"
            @click="confirmDelete">
            <span v-if="deleteSubmitting" class="qs-btn-spinner" aria-hidden="true"></span>
            <span>{{ deleteSubmitting ? '삭제 중…' : '영구 삭제' }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import QuSignMark from '@/components/ui/QuSignMark.vue'
import ThemeToggle from '@/components/ui/ThemeToggle.vue'
import { useAuthStore } from '@/stores/auth'

const SECTIONS = [
  { id: 'profile',  num: '01', label: '프로필' },
  { id: 'security', num: '02', label: '보안' },
  { id: 'notify',   num: '03', label: '알림' },
  { id: 'danger',   num: '04', label: '계정 삭제', danger: true },
]

const router = useRouter()
const auth = useAuthStore()
const userEmail = computed(() => auth.email ?? '')
const userInitial = computed(() => auth.email?.charAt(0).toUpperCase() ?? '?')

// Theme
const theme = ref<'light' | 'dark'>('light')
watch(theme, (t) => document.documentElement.setAttribute('data-theme', t), { immediate: true })
function handleThemeToggle(t: 'light' | 'dark') { theme.value = t }

function handleLogout() {
  auth.logout()
  router.push('/login')
}

// Scroll-spy
const activeSection = ref('profile')
function jumpTo(id: string) {
  const el = document.getElementById('sec-' + id)
  if (!el) return
  window.scrollTo({ top: el.getBoundingClientRect().top + window.scrollY - 88, behavior: 'smooth' })
}

let scrollHandler: () => void
onMounted(() => {
  const ids = SECTIONS.map(s => 'sec-' + s.id)
  scrollHandler = () => {
    const fromTop = window.scrollY + 140
    let current = ids[0]
    for (const id of ids) {
      const el = document.getElementById(id)
      if (el && el.offsetTop <= fromTop) current = id
    }
    activeSection.value = current.replace('sec-', '')
  }
  scrollHandler()
  window.addEventListener('scroll', scrollHandler, { passive: true })
})
onUnmounted(() => {
  window.removeEventListener('scroll', scrollHandler)
})

// Toast
const toast = ref<string | null>(null)
function showToast(msg: string) {
  toast.value = msg
  setTimeout(() => { toast.value = null }, 2400)
}

// Toggle helpers
const toggleBusy = ref<Record<string, boolean>>({})
const toggleSaved = ref<Record<string, boolean>>({})
function updateToggle(key: string, setter: () => void) {
  setter()
  toggleBusy.value[key] = true
  setTimeout(() => {
    toggleBusy.value[key] = false
    toggleSaved.value[key] = true
    setTimeout(() => { toggleSaved.value[key] = false }, 1600)
  }, 500)
}

// Security toggles
const secTwoFA = ref(false)
const secSession = ref(true)

// Notification toggles (ref-wrapped for v-for reactivity)
const notifyReq = ref(true)
const notifyDone = ref(true)
const notifyWeekly = ref(false)
const notifyMarketing = ref(false)

const notifyToggles = [
  { key: 'notifyReq',       value: notifyReq,       title: '서명 요청 알림',      desc: '다른 사람이 나에게 서명을 요청하면 이메일로 알려드려요.' },
  { key: 'notifyDone',      value: notifyDone,      title: '서명 완료 알림',      desc: '요청한 서명이 완료되면 이메일로 알려드려요.' },
  { key: 'notifyWeekly',    value: notifyWeekly,    title: '주간 활동 요약',      desc: '매주 월요일 아침, 한 주의 서명 활동을 정리해 보내드려요.' },
  { key: 'notifyMarketing', value: notifyMarketing, title: '신제품 소식 · 팁',    desc: '새 기능과 활용 팁을 보내드려요.' },
]

// Password form
const pwCurrent = ref('')
const pwNew = ref('')
const pwConfirm = ref('')
const showPwCurrent = ref(false)
const showPwNew = ref(false)
const pwSubmitting = ref(false)
const pwSuccess = ref(false)

function scorePassword(pw: string): { score: number; label: string } {
  if (!pw) return { score: 0, label: '' }
  let s = 0
  if (pw.length >= 8) s++
  if (pw.length >= 12) s++
  if (/[A-Z]/.test(pw) && /[a-z]/.test(pw)) s++
  if (/\d/.test(pw)) s++
  if (/[^A-Za-z0-9]/.test(pw)) s++
  const score = Math.min(3, Math.ceil(s / 2))
  return { score, label: ['', '약함', '보통', '강함'][score] }
}

const pwStrength = computed(() => scorePassword(pwNew.value))
const pwStrengthClass = computed(() => {
  const s = pwStrength.value.score
  return s === 1 ? 'is-weak' : s === 2 ? 'is-medium' : s === 3 ? 'is-strong' : ''
})
const pwMismatch = computed(() => pwConfirm.value.length > 0 && pwConfirm.value !== pwNew.value)
const pwTooShort = computed(() => pwNew.value.length > 0 && pwNew.value.length < 8)
const canSubmitPw = computed(() =>
  pwCurrent.value.length >= 1 &&
  pwNew.value.length >= 8 &&
  pwConfirm.value === pwNew.value &&
  !pwSubmitting.value
)

async function handlePasswordChange() {
  if (!canSubmitPw.value) return
  pwSubmitting.value = true
  pwSuccess.value = false
  // TODO: await api.put('/api/users/password', { current: pwCurrent.value, next: pwNew.value })
  await new Promise(r => setTimeout(r, 900))
  pwSubmitting.value = false
  pwSuccess.value = true
  pwCurrent.value = ''
  pwNew.value = ''
  pwConfirm.value = ''
  showToast('비밀번호가 변경되었어요')
  setTimeout(() => { pwSuccess.value = false }, 3500)
}

// Delete modal
const showDeleteModal = ref(false)
const deleteEmailInput = ref('')
const deleteSubmitting = ref(false)
const deleteInputEl = ref<HTMLInputElement | null>(null)
const deleteEmailMatch = computed(() => deleteEmailInput.value === userEmail.value)

function openDeleteModal() {
  showDeleteModal.value = true
  deleteEmailInput.value = ''
  nextTick(() => deleteInputEl.value?.focus())
}
function closeDeleteModal() { showDeleteModal.value = false }

async function confirmDelete() {
  if (!deleteEmailMatch.value) return
  deleteSubmitting.value = true
  // TODO: await api.delete('/api/users/me')
  await new Promise(r => setTimeout(r, 900))
  deleteSubmitting.value = false
  closeDeleteModal()
  showToast('계정 삭제 요청이 접수되었어요')
}
</script>
