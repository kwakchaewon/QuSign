<template>
  <header class="qs-topbar pub-topbar">
    <RouterLink class="qs-topbar-brand" :to="logoTo">
      <QuSignMark variant="badge" :size="28" />
      <span class="qs-topbar-name">{{ title }}</span>
    </RouterLink>
    <div v-if="subtitle" class="pub-topbar-subtitle">{{ subtitle }}</div>
    <div class="qs-topbar-right">
      <div v-if="showPqcBadge" class="pub-pqc-pill">
        <span class="pub-pqc-dot" />
        <span>ML-DSA-65</span>
      </div>
      <a v-if="showSupportLink" class="qs-link-quiet qs-topbar-help" href="#" @click.prevent>고객지원</a>
      <ThemeToggle :theme="theme" @change="setTheme" />
    </div>
  </header>
</template>

<script setup lang="ts">
import QuSignMark from '@/components/ui/QuSignMark.vue'
import ThemeToggle from '@/components/ui/ThemeToggle.vue'
import { useTheme } from '@/composables/useTheme'

withDefaults(defineProps<{
  title?: string
  subtitle?: string
  logoTo?: string
  showPqcBadge?: boolean
  showSupportLink?: boolean
}>(), {
  title: 'QuSign',
  logoTo: '/',
  showPqcBadge: false,
  showSupportLink: false,
})

const { theme } = useTheme()
function setTheme(t: 'light' | 'dark') { theme.value = t }
</script>

<style scoped>
.pub-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  position: relative;
}
.pub-topbar-subtitle {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  pointer-events: none;
}
.pub-pqc-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 9999px;
  border: 0.5px solid var(--border-default);
  background: var(--surface-muted);
  font-size: 11px;
  font-weight: 600;
  color: var(--text-secondary);
  letter-spacing: 0.03em;
  text-transform: uppercase;
}
.pub-pqc-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-success);
  flex-shrink: 0;
  animation: pub-pqc-pulse 2s ease-in-out infinite;
}
@keyframes pub-pqc-pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(0.85); }
}
</style>
