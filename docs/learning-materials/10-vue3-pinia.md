# Vue 3 + Pinia + Vue Router (3단계)

> PLAN.md §3 대응 — 프론트엔드 핵심 개념 정리

---

## 이론

### Vue 3 Composition API

Options API(Vue 2)와 달리 Composition API는 **로직을 기능 단위로 모을 수 있습니다.**

```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

// 반응형 상태
const count = ref(0)
const doubled = computed(() => count.value * 2)

// 생명주기
onMounted(() => {
  console.log('mounted')
})

// 메서드
function increment() {
  count.value++   // ref는 .value로 접근
}
</script>

<template>
  <button @click="increment">{{ count }}</button>
  <p>doubled: {{ doubled }}</p>
</template>
```

`<script setup>`은 Composition API의 문법 설탕입니다.
컴포넌트에서 `return`을 생략할 수 있고, 가져온 컴포넌트를 자동으로 등록합니다.

### ref vs reactive

| | `ref` | `reactive` |
|---|---|---|
| 대상 | 원시값·객체 모두 | 객체만 |
| 접근 | `.value` 필요 (JS에서) | 직접 접근 |
| 주의 | 비구조화 시 반응성 유지 | 비구조화 시 반응성 잃음 |

```typescript
const name = ref('Alice')        // ref — .value로 접근
name.value = 'Bob'

const user = reactive({ name: 'Alice' })  // reactive — 직접 접근
user.name = 'Bob'
```

### Pinia — 상태 관리

Vuex의 후계자입니다. 보일러플레이트 없이 TypeScript 친화적 스토어를 만들 수 있습니다.

```typescript
// stores/authStore.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<User | null>(null)

  const isLoggedIn = computed(() => token.value !== null)

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
  }

  return { token, user, isLoggedIn, setToken, logout }
})
```

```vue
<!-- 컴포넌트에서 사용 -->
<script setup lang="ts">
import { useAuthStore } from '@/stores/authStore'
const authStore = useAuthStore()
</script>

<template>
  <span v-if="authStore.isLoggedIn">{{ authStore.user?.email }}</span>
</template>
```

### Vue Router — 네비게이션 가드

```typescript
// router/index.ts
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView },
    { path: '/home', component: HomeView, meta: { requiresAuth: true } },
    { path: '/sign/:token', component: SignerView },  // :token → 동적 파라미터
  ]
})

// 전역 가드 — 인증 체크
router.beforeEach((to, from) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return { path: '/login' }   // 리다이렉트
  }
})
```

### Axios 인터셉터

```typescript
// lib/api.ts
const api = axios.create({ baseURL: '' })

// 요청 인터셉터 — JWT 자동 첨부
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 응답 인터셉터 — 401 자동 로그아웃
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore().logout()
      router.push('/login')
    }
    return Promise.reject(error)
  }
)
```

---

## QuSign 화면 구조

```
LoginView       /login          ← 로그인 폼
RegisterView    /register       ← 회원가입
HomeView        /home           ← 대시보드 (통계 + 최근 요청)
RequestView     /request        ← 3단계 서명 요청 생성 (멀티 업로드)
SignerView      /sign/:token    ← 서명자 화면 (본인 확인 → PDF 미리보기 → 서명)
VerifyView      /verify         ← 무결성 검증 (토큰 / 파일 탭)
DocumentDetailView  /documents/:id  ← 요청 상세 (감사 타임라인 포함)
AccountSettingsView /settings   ← 비밀번호 변경 / 계정 탈퇴
```

### PDF.js 연동

```vue
<!-- PdfViewer.vue -->
<script setup lang="ts">
import * as pdfjsLib from 'pdfjs-dist'

pdfjsLib.GlobalWorkerOptions.workerSrc = '/pdf.worker.min.js'

async function renderPdf(url: string) {
  const pdf = await pdfjsLib.getDocument(url).promise
  const page = await pdf.getPage(1)
  const canvas = canvasRef.value!
  const context = canvas.getContext('2d')!
  const viewport = page.getViewport({ scale: 1.5 })
  canvas.width = viewport.width
  canvas.height = viewport.height
  await page.render({ canvasContext: context, viewport }).promise
}
</script>
```

---

## 확인 질문 & 답변

**Q1. `ref(0)`과 `reactive({ count: 0 })`은 언제 각각 쓰는가?**

> 단일 원시값(숫자·문자열·불리언)이나 단순 변수는 `ref`를 씁니다. 여러 관련 필드를 묶은 객체는 `reactive`를 씁니다. 다만 비구조화(`const { count } = reactive(...)`) 시 반응성이 사라지므로 `toRefs()`를 사용해야 합니다. 익숙하지 않다면 항상 `ref`를 쓰는 것이 안전합니다.

**Q2. Pinia 스토어의 state가 컴포넌트 간 공유되는 원리는?**

> Pinia는 앱 전역에서 스토어 인스턴스를 싱글톤으로 관리합니다. `useAuthStore()`를 여러 컴포넌트에서 호출해도 같은 인스턴스를 반환합니다. Vue의 반응형 시스템이 상태 변화를 구독하는 모든 컴포넌트에 자동 전파합니다.

**Q3. `router.beforeEach`가 없으면 어떤 보안 문제가 생기나?**

> 인증되지 않은 사용자가 URL을 직접 입력해서 `/home`, `/settings` 같은 보호된 페이지에 접근할 수 있습니다. 하지만 API 요청은 서버에서 JWT를 검증하므로 데이터는 보호됩니다. 가드는 사용자 경험(UX) 보호이고, 실제 데이터 보호는 서버 사이드 인증입니다.

**Q4. Axios 인터셉터에서 401을 가로채서 자동 로그아웃하는 이유는?**

> JWT가 만료되면 서버가 401을 반환합니다. 인터셉터 없이는 각 API 호출 코드마다 401 처리를 작성해야 합니다. 인터셉터에서 한 번만 처리하면 토큰 만료 시 자동으로 로그인 페이지로 이동하고 스토어 상태도 초기화됩니다.
