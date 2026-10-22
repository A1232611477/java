import { computed, ref } from 'vue'
import * as authApi from '../api/auth'
import { clearAuth, getToken, readUser, saveAuth, saveUser } from '../api/http'

// 模块级共享状态：整个应用只有一份登录态
const token = ref(getToken())
const user = ref(readUser())
const loading = ref(false)

const isLogged = computed(() => !!token.value)

/**
 * 登录态：令牌与用户信息同时存内存（响应式）和 Web Storage。
 * 存哪里由「记住我」决定：勾选 → localStorage（长期）；不勾 → sessionStorage（关浏览器即失效）。
 */
export function useAuth() {
  async function signIn(payload) {
    loading.value = true
    try {
      const data = await authApi.login(payload)
      token.value = data.token
      user.value = data.user
      // expiresIn 由后端按是否记住我返回，用于在用户中心展示「有效期至」
      saveAuth(data.token, data.user, !!payload?.rememberMe, data.expiresIn)
      return data
    } finally {
      loading.value = false
    }
  }

  /** 拿服务端最新用户信息（顺便验证令牌还有效） */
  async function refresh() {
    const me = await authApi.fetchMe()
    user.value = me
    saveUser(me)
    return me
  }

  function signOut() {
    token.value = ''
    user.value = null
    clearAuth()
  }

  return { token, user, loading, isLogged, signIn, refresh, signOut }
}
