<template>
  <div class="qs-admin-page">
    <h1 class="qs-admin-page-title">사용자 관리</h1>

    <div class="qs-admin-toolbar">
      <input
        v-model="searchEmail"
        type="search"
        class="qs-admin-search"
        placeholder="이메일로 검색"
        @keydown.enter="fetchUsers(0)"
      />
      <button class="qs-admin-btn" @click="fetchUsers(0)">검색</button>
    </div>

    <div v-if="loading" class="qs-admin-table-wrap">
      <div v-for="i in 8" :key="i" class="qs-skel" style="height:44px;border-radius:6px;margin-bottom:6px" />
    </div>

    <div v-else-if="users.length === 0" class="qs-admin-empty">검색 결과가 없습니다.</div>

    <div v-else class="qs-admin-table-wrap">
      <table class="qs-admin-table">
        <thead>
          <tr>
            <th>이메일</th>
            <th>역할</th>
            <th>가입일</th>
            <th>상태</th>
            <th>작업</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.email">
            <td class="qs-admin-td-email">{{ user.email }}</td>
            <td>
              <span :class="['qs-admin-role-badge', user.role === 'ADMIN' ? 'is-admin' : '']">
                {{ user.role === 'ADMIN' ? '관리자' : '일반' }}
              </span>
            </td>
            <td class="qs-admin-td-time">{{ formatDate(user.createdAt) }}</td>
            <td>
              <span :class="['qs-admin-status-badge', user.disabledAt ? 'is-disabled' : 'is-active']">
                {{ user.disabledAt ? '비활성' : '활성' }}
              </span>
            </td>
            <td>
              <button
                v-if="!user.disabledAt && user.role !== 'ADMIN'"
                class="qs-admin-btn-danger"
                :disabled="disabling === user.email"
                @click="confirmDisable(user)"
              >
                {{ disabling === user.email ? '처리 중…' : '비활성화' }}
              </button>
              <span v-else-if="user.disabledAt" class="qs-admin-td-muted">{{ formatDate(user.disabledAt) }} 비활성화</span>
              <span v-else class="qs-admin-td-muted">-</span>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="qs-admin-pagination">
        <button class="qs-admin-btn-ghost" :disabled="page === 0" @click="fetchUsers(page - 1)">이전</button>
        <span class="qs-admin-page-info">{{ page + 1 }} / {{ totalPages }}</span>
        <button class="qs-admin-btn-ghost" :disabled="page >= totalPages - 1" @click="fetchUsers(page + 1)">다음</button>
      </div>
    </div>

    <div v-if="confirmTarget" class="qs-admin-modal-overlay" @click.self="confirmTarget = null">
      <div class="qs-admin-modal">
        <h3 class="qs-admin-modal-title">사용자 비활성화</h3>
        <p class="qs-admin-modal-body">
          <strong>{{ confirmTarget.email }}</strong> 계정을 비활성화하면 해당 사용자는 로그인할 수 없습니다. 계속하시겠습니까?
        </p>
        <div class="qs-admin-modal-actions">
          <button class="qs-admin-btn-ghost" @click="confirmTarget = null">취소</button>
          <button class="qs-admin-btn-danger" @click="doDisable">비활성화</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/lib/api'

interface AdminUser {
  email: string
  role: string
  createdAt: string | null
  disabledAt: string | null
}

const users = ref<AdminUser[]>([])
const loading = ref(true)
const searchEmail = ref('')
const page = ref(0)
const totalPages = ref(1)
const disabling = ref<string | null>(null)
const confirmTarget = ref<AdminUser | null>(null)

async function fetchUsers(p: number) {
  loading.value = true
  try {
    const params = new URLSearchParams({ page: String(p), size: '20' })
    if (searchEmail.value) params.set('email', searchEmail.value)
    const res = await api.get<{ data: { content: AdminUser[]; totalPages: number } }>(`/api/admin/users?${params}`)
    users.value = res.data.data.content
    totalPages.value = res.data.data.totalPages || 1
    page.value = p
  } finally {
    loading.value = false
  }
}

function confirmDisable(user: AdminUser) {
  confirmTarget.value = user
}

async function doDisable() {
  if (!confirmTarget.value) return
  const email = confirmTarget.value.email
  confirmTarget.value = null
  disabling.value = email
  try {
    await api.put(`/api/admin/users/${encodeURIComponent(email)}/disable`)
    const target = users.value.find((u) => u.email === email)
    if (target) target.disabledAt = new Date().toISOString()
  } finally {
    disabling.value = null
  }
}

function formatDate(d: string | null) {
  if (!d) return '-'
  const dt = new Date(d)
  if (isNaN(dt.getTime())) return d
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}.${pad(dt.getMonth()+1)}.${pad(dt.getDate())}`
}

onMounted(() => fetchUsers(0))
</script>
