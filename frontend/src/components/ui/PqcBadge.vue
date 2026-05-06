<template>
  <span v-if="variant === 'mono'" class="qs-pqc qs-pqc-mono">
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 3l8 3v6c0 4.5-3.4 8.5-8 9-4.6-.5-8-4.5-8-9V6l8-3z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round" />
      <path d="M9 12.5l2 2 4-4.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" />
    </svg>
    <span class="qs-pqc-label">양자내성암호 (PQC)</span>
    <span class="qs-pqc-dot" aria-hidden="true" />
    <span class="qs-pqc-tag">ML-DSA-65</span>
  </span>
  <span v-else-if="variant === 'pill'" class="qs-pqc qs-pqc-pill">
    <span class="qs-pqc-dot-live" aria-hidden="true" />
    PQC 보안 적용
  </span>
  <span v-else class="qs-pqc qs-pqc-soft">
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 3l8 3v6c0 4.5-3.4 8.5-8 9-4.6-.5-8-4.5-8-9V6l8-3z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round" />
      <path d="M9 12.5l2 2 4-4.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" />
    </svg>
    <span>PQC 보안 — 양자컴퓨터에도 안전한 암호 방식</span>
  </span>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ variant?: 'soft' | 'pill' | 'mono' }>(), { variant: 'soft' })
</script>
