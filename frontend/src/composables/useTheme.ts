import { ref, watch } from 'vue'

const STORAGE_KEY = 'qs-theme'

const theme = ref<'light' | 'dark'>(
  (localStorage.getItem(STORAGE_KEY) as 'light' | 'dark') ?? 'light'
)

watch(theme, (t) => {
  document.documentElement.setAttribute('data-theme', t)
  localStorage.setItem(STORAGE_KEY, t)
}, { immediate: true })

export function useTheme() {
  return { theme }
}
