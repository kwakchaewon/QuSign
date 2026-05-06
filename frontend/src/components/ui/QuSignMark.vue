<template>
  <svg v-if="variant === 'glyph'" :width="size" :height="size" viewBox="0 0 44 44" fill="none" aria-hidden="true">
    <circle cx="22" cy="22" r="14" stroke="var(--color-primary-500)" stroke-width="2.4" />
    <path d="M30 30 L37 37" stroke="var(--color-primary-500)" stroke-width="2.4" stroke-linecap="round" />
    <path d="M16 22 L20 26 L29 17" stroke="var(--color-primary-500)" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round" />
  </svg>
  <svg v-else-if="variant === 'lock'" :width="size" :height="size" viewBox="0 0 44 44" fill="none" aria-hidden="true">
    <rect x="6" y="6" width="32" height="32" rx="10" fill="var(--color-primary-500)" />
    <path d="M16 21v-3a6 6 0 0 1 12 0v3" stroke="#fff" stroke-width="2.2" stroke-linecap="round" />
    <rect x="14" y="20" width="16" height="13" rx="3" fill="#fff" />
    <circle cx="22" cy="26" r="1.8" fill="var(--color-primary-500)" />
    <rect x="21.2" y="26" width="1.6" height="4" rx="0.8" fill="var(--color-primary-500)" />
  </svg>
  <svg v-else :width="size" :height="size" viewBox="0 0 44 44" fill="none" aria-hidden="true">
    <rect x="2" y="2" width="40" height="40" rx="12" fill="var(--color-primary-500)" />
    <circle cx="22" cy="21" r="9" stroke="#fff" stroke-width="2.4" />
    <path d="M27 26 L33 32" stroke="#fff" stroke-width="2.6" stroke-linecap="round" />
  </svg>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  variant?: 'badge' | 'glyph' | 'lock'
  size?: number
}>(), { variant: 'badge', size: 44 })
</script>
