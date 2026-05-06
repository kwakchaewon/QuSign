<template>
  <button
    type="button"
    class="qs-theme-toggle"
    :aria-label="isDark ? '라이트 모드로 전환' : '다크 모드로 전환'"
    @click="emit('change', isDark ? 'light' : 'dark')"
  >
    <svg v-if="isDark" width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="12" r="4" stroke="currentColor" stroke-width="1.6" />
      <path d="M12 3v2M12 19v2M3 12h2M19 12h2M5.6 5.6l1.4 1.4M17 17l1.4 1.4M5.6 18.4L7 17M17 7l1.4-1.4"
        stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
    </svg>
    <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M20 14.5A8 8 0 1 1 9.5 4a7 7 0 0 0 10.5 10.5z"
        stroke="currentColor" stroke-width="1.6" stroke-linejoin="round" />
    </svg>
    <span>{{ isDark ? '라이트' : '다크' }}</span>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ theme: 'light' | 'dark' }>()
const emit = defineEmits<{ change: [theme: 'light' | 'dark'] }>()

const isDark = computed(() => props.theme === 'dark')
</script>
