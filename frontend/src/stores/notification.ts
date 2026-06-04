import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/lib/api'

export interface Notification {
  id: number
  type: string
  title: string
  message: string
  referenceId: number | null
  referenceToken: string | null
  isRead: boolean
  createdAt: string
}

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<Notification[]>([])
  const unreadCount = ref(0)
  let eventSource: EventSource | null = null

  async function fetchNotifications() {
    const res = await api.get<{ data: Notification[] }>('/api/notifications')
    notifications.value = res.data.data ?? []
    unreadCount.value = notifications.value.filter((n) => !n.isRead).length
  }

  async function fetchPage(page: number): Promise<Notification[]> {
    const res = await api.get<{ data: Notification[] }>('/api/notifications', { params: { page } })
    return res.data.data ?? []
  }

  async function markAsRead(id: number) {
    await api.put(`/api/notifications/${id}/read`)
    const n = notifications.value.find((n) => n.id === id)
    if (n && !n.isRead) {
      n.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  }

  async function markAllAsRead() {
    await api.put('/api/notifications/read-all')
    notifications.value.forEach((n) => (n.isRead = true))
    unreadCount.value = 0
  }

  function connectSSE(token: string) {
    if (eventSource) return
    const url = `http://localhost:8080/api/notifications/stream?token=${encodeURIComponent(token)}`
    eventSource = new EventSource(url)

    eventSource.addEventListener('notification', (e) => {
      const incoming: Notification = JSON.parse(e.data)
      notifications.value.unshift(incoming)
      if (!incoming.isRead) unreadCount.value++
    })

    eventSource.onerror = () => {
      eventSource?.close()
      eventSource = null
      // 5초 후 재연결 시도
      setTimeout(() => connectSSE(token), 5000)
    }
  }

  function disconnectSSE() {
    eventSource?.close()
    eventSource = null
    notifications.value = []
    unreadCount.value = 0
  }

  return {
    notifications,
    unreadCount,
    fetchNotifications,
    fetchPage,
    markAsRead,
    markAllAsRead,
    connectSSE,
    disconnectSSE,
  }
})
