import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/lib/api'
import { useNotificationStore } from '@/stores/notification'

function parseJwtRole(token: string): string {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.role ?? 'USER'
  } catch {
    return 'USER'
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('qusign:token'))
  const email = ref<string | null>(localStorage.getItem('qusign:email'))
  const role = ref<string>(token.value ? parseJwtRole(token.value) : 'USER')
  const sessionLockEnabled = ref(localStorage.getItem('qusign:sessionLock') !== 'false')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'ADMIN')

  async function login(emailVal: string, password: string) {
    const res = await api.post<{ data: { accessToken: string } }>('/api/auth/login', {
      email: emailVal,
      password,
    })
    const accessToken = res.data.data.accessToken
    token.value = accessToken
    email.value = emailVal
    role.value = parseJwtRole(accessToken)
    localStorage.setItem('qusign:token', accessToken)
    localStorage.setItem('qusign:email', emailVal)

    const notificationStore = useNotificationStore()
    await notificationStore.fetchNotifications()
    notificationStore.connectSSE()
  }

  async function register(emailVal: string, password: string) {
    await api.post('/api/auth/register', { email: emailVal, password })
  }

  function logout() {
    const notificationStore = useNotificationStore()
    notificationStore.disconnectSSE()

    token.value = null
    email.value = null
    role.value = 'USER'
    localStorage.removeItem('qusign:token')
    localStorage.removeItem('qusign:email')
  }

  function setSessionLock(enabled: boolean) {
    sessionLockEnabled.value = enabled
    localStorage.setItem('qusign:sessionLock', String(enabled))
  }

  return { token, email, role, isLoggedIn, isAdmin, sessionLockEnabled, login, register, logout, setSessionLock }
})
