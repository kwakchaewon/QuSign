import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import SignupView from '../views/SignupView.vue'
import DashboardView from '../views/DashboardView.vue'
import RequestView from '../views/RequestView.vue'
import SignerView from '../views/SignerView.vue'
import VerifyView from '../views/VerifyView.vue'

const AUTH_ROUTES = ['/documents', '/request']

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/signup', name: 'signup', component: SignupView },
    { path: '/documents', name: 'documents', component: DashboardView },
    { path: '/request', name: 'request', component: RequestView },
    { path: '/sign/:token?', name: 'sign', component: SignerView },
    { path: '/verify', name: 'verify', component: VerifyView },
  ],
})

router.beforeEach((to) => {
  const needsAuth = AUTH_ROUTES.some((p) => to.path.startsWith(p))
  if (needsAuth && !localStorage.getItem('qusign:token')) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
})

export default router
