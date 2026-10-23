import { ref, computed } from 'vue'
import * as adminApi from '../api/admin'
import { getAdminToken, readAdminUser, saveAdminAuth, clearAdminAuth } from '../api/http'

// 模块级共享状态：整个应用只有一份管理员登录态（与用户登录态隔离）
const token = ref(getAdminToken())
const user = ref(readAdminUser())
const loading = ref(false)

const isAdmin = computed(() => !!token.value)

/**
 * 后台管理员登录态。
 * 存 localStorage（长期）：管理员登录后稳定保留，避免频繁重登。
 */
export function useAdminAuth() {
  async function signIn(payload) {
    loading.value = true
    try {
      const data = await adminApi.adminLogin(payload)
      token.value = data.token
      user.value = data.user
      saveAdminAuth(data.token, data.user)
      return data
    } finally {
      loading.value = false
    }
  }

  function signOut() {
    token.value = ''
    user.value = null
    clearAdminAuth()
  }

  return { token, user, loading, isAdmin, signIn, signOut }
}
