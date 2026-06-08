import axios from 'axios'
import { useToast } from '@/composables/useToast'

const { showError } = useToast()

const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 15000,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('qusign:token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      const isAuthEndpoint = err.config?.url?.includes('/api/auth/')
      if (!isAuthEndpoint) {
        localStorage.removeItem('qusign:token')
        localStorage.removeItem('qusign:email')
        window.location.href = '/login'
      }
    } else if (!err.response) {
      showError('네트워크 연결을 확인해주세요.')
    } else if (err.response.status >= 500) {
      showError('서버 오류가 발생했어요. 잠시 후 다시 시도해주세요.')
    }
    return Promise.reject(err)
  },
)

export default api
