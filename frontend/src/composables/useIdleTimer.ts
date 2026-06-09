import { watch, onUnmounted } from 'vue'
import type { Ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const TIMEOUT_MS = 15 * 60 * 1000
const ACTIVITY_EVENTS = ['mousemove', 'mousedown', 'keydown', 'scroll', 'touchstart'] as const

export function useIdleTimer(enabled: Ref<boolean>) {
  const auth = useAuthStore()
  const router = useRouter()
  let timerId: ReturnType<typeof setTimeout> | null = null

  function handleTimeout() {
    auth.logout()
    router.push('/login')
  }

  function reset() {
    if (timerId) clearTimeout(timerId)
    timerId = setTimeout(handleTimeout, TIMEOUT_MS)
  }

  function start() {
    ACTIVITY_EVENTS.forEach(e => window.addEventListener(e, reset, { passive: true }))
    reset()
  }

  function stop() {
    if (timerId) { clearTimeout(timerId); timerId = null }
    ACTIVITY_EVENTS.forEach(e => window.removeEventListener(e, reset))
  }

  watch(
    [enabled, () => auth.isLoggedIn],
    ([isEnabled, isLoggedIn]) => {
      stop()
      if (isEnabled && isLoggedIn) start()
    },
    { immediate: true },
  )

  onUnmounted(stop)
}
