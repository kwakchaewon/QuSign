import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import SignupView from '../views/SignupView.vue'
import HomeView from '../views/HomeView.vue'
import DashboardView from '../views/DashboardView.vue'
import DocumentDetailView from '../views/DocumentDetailView.vue'
import RequestView from '../views/RequestView.vue'
import SignerView from '../views/SignerView.vue'
import VerifyView from '../views/VerifyView.vue'
import AccountSettingsView from '../views/AccountSettingsView.vue'
import NotificationsView from '../views/NotificationsView.vue'

const AUTH_ROUTES = ['/home', '/documents', '/request', '/settings', '/notifications', '/sign']

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: () => (localStorage.getItem('qusign:token') ? '/home' : '/login'),
    },
    { path: '/home', name: 'home', component: HomeView },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/signup', name: 'signup', component: SignupView },
    { path: '/documents', name: 'documents', component: DashboardView },
    { path: '/documents/:id', name: 'document-detail', component: DocumentDetailView },
    { path: '/request', name: 'request', component: RequestView },
    { path: '/sign/:token?', name: 'sign', component: SignerView },
    { path: '/verify', name: 'verify', component: VerifyView },
    { path: '/settings', name: 'settings', component: AccountSettingsView },
    { path: '/notifications', name: 'notifications', component: NotificationsView },
  ],
})

router.beforeEach((to) => {
  const needsAuth = AUTH_ROUTES.some((p) => to.path.startsWith(p))
  if (needsAuth && !localStorage.getItem('qusign:token')) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
})

export default router
