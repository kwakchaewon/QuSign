import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import SignupView from '../views/SignupView.vue'
import HomeView from '../views/HomeView.vue'
import UnifiedDashboardView from '../views/UnifiedDashboardView.vue'
import DashboardView from '../views/DashboardView.vue'
import DocumentDetailView from '../views/DocumentDetailView.vue'
import BundleDetailView from '../views/BundleDetailView.vue'
import RequestView from '../views/RequestView.vue'
import SignerView from '../views/SignerView.vue'
import VerifyView from '../views/VerifyView.vue'
import AccountSettingsView from '../views/AccountSettingsView.vue'
import NotificationsView from '../views/NotificationsView.vue'
import ReceivedDocumentsView from '../views/ReceivedDocumentsView.vue'
import ReceivedDetailView from '../views/ReceivedDetailView.vue'

const AUTH_ROUTES = ['/home', '/documents', '/bundles', '/request', '/settings', '/notifications', '/sign', '/received']

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: () => (localStorage.getItem('qusign:token') ? '/home' : '/login'),
    },
    { path: '/home',            name: 'home',             component: HomeView,                meta: { title: '홈' } },
    { path: '/login',           name: 'login',            component: LoginView,               meta: { title: '로그인' } },
    { path: '/signup',          name: 'signup',           component: SignupView,              meta: { title: '회원가입' } },
    { path: '/documents',       name: 'documents',        component: UnifiedDashboardView,    meta: { title: '문서 현황' } },
    { path: '/documents/sent',  name: 'documents-sent',   component: DashboardView,           meta: { title: '보낸 서명 요청' } },
    { path: '/documents/:id',   name: 'document-detail',  component: DocumentDetailView,      meta: { title: '문서 상세' } },
    { path: '/bundles/:id',     name: 'bundle-detail',    component: BundleDetailView,        meta: { title: '번들 상세' } },
    { path: '/request',         name: 'request',          component: RequestView,             meta: { title: '서명 요청' } },
    { path: '/received',        name: 'received',         component: ReceivedDocumentsView,   meta: { title: '받은 서명 요청' } },
    { path: '/received/:token', name: 'received-detail',  component: ReceivedDetailView,      meta: { title: '서명 요청 상세' } },
    { path: '/sign/:token?',    name: 'sign',             component: SignerView,              meta: { title: '서명하기' } },
    { path: '/verify',          name: 'verify',           component: VerifyView,              meta: { title: '검증' } },
    { path: '/settings',        name: 'settings',         component: AccountSettingsView,     meta: { title: '계정 설정' } },
    { path: '/notifications',   name: 'notifications',    component: NotificationsView,       meta: { title: '알림' } },
  ],
})

const BASE_TITLE = 'QuSign'

router.beforeEach((to) => {
  const needsAuth = AUTH_ROUTES.some((p) => to.path.startsWith(p))
  if (needsAuth && !localStorage.getItem('qusign:token')) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
})

router.afterEach((to) => {
  const pageTitle = to.meta.title as string | undefined
  document.title = pageTitle ? `${pageTitle} :: ${BASE_TITLE}` : BASE_TITLE
})

export default router
