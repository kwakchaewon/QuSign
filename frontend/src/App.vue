<template>
  <div>
    <RouterView />

    <div :class="['qs-toast-wrap', { 'is-show': message !== null }]" aria-live="assertive">
      <div class="qs-toast">
        <div class="qs-toast-ic is-error" aria-hidden="true">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
            <path d="M12 8v5M12 16h.01" stroke="currentColor" stroke-width="2.2"
              stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div>
          <p class="qs-toast-t">오류</p>
          <p class="qs-toast-d">{{ message }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useToast } from '@/composables/useToast'

const { message } = useToast()
</script>
