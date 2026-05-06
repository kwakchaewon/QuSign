<template>
  <div :class="['qs-toast-wrap', show && 'is-show']" aria-live="polite">
    <div class="qs-toast">
      <div class="qs-toast-ic">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M5 12.5l4.5 4.5L19 7" stroke="currentColor" stroke-width="2.2"
            stroke-linecap="round" stroke-linejoin="round" />
        </svg>
      </div>
      <div>
        <p class="qs-toast-t">로그인 성공</p>
        <p class="qs-toast-d">{{ email }}으로 안전하게 연결되었어요</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ show: boolean; email: string }>()
</script>
