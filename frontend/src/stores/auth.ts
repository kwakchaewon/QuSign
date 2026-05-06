import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/lib/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('qusign:token'))
  const email = ref<string | null>(localStorage.getItem('qusign:email'))

  const isLoggedIn = computed(() => !!token.value)

  async function login(emailVal: string, password: string) {
    const res = await api.post<{ data: { accessToken: string } }>('/api/auth/login', {
      email: emailVal,
      password,
    })
    const accessToken = res.data.data.accessToken
    token.value = accessToken
    email.value = emailVal
    localStorage.setItem('qusign:token', accessToken)
    localStorage.setItem('qusign:email', emailVal)
  }

  async function register(emailVal: string, password: string) {
    await api.post('/api/auth/register', { email: emailVal, password })
  }

  function logout() {
    token.value = null
    email.value = null
    localStorage.removeItem('qusign:token')
    localStorage.removeItem('qusign:email')
  }

  return { token, email, isLoggedIn, login, register, logout }
})
