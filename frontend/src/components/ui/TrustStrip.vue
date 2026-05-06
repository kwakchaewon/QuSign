<template>
  <div class="qs-trust">
    <div v-for="item in items" :key="item.k" class="qs-trust-item">
      <span class="qs-trust-k">{{ item.k }}</span>
      <span class="qs-trust-v">{{ item.v }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
const items = [
  { k: '인증', v: 'ISMS-P' },
  { k: '서명', v: 'ML-DSA-65' },
  { k: '전송', v: 'TLS 1.3' },
]
</script>
