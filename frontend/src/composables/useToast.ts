import { ref } from 'vue'

const message = ref<string | null>(null)
let timer: ReturnType<typeof setTimeout> | null = null

export function useToast() {
  function showError(text: string) {
    message.value = text
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => { message.value = null }, 4000)
  }
  function hide() { message.value = null }
  return { message, showError, hide }
}
