import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import SignupView from '../views/SignupView.vue'
import DashboardView from '../views/DashboardView.vue'
import RequestView from '../views/RequestView.vue'
import SignerView from '../views/SignerView.vue'
import VerifyView from '../views/VerifyView.vue'

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

export default router
