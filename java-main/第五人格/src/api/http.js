// 统一请求封装：拼后端地址、自动带 JWT、统一处理 { code, message, data } 结构
//
// 后端地址默认本机 8081 的 springBootTest；
// 如需修改，在项目根目录建 .env 写 VITE_API_BASE=http://xxx:8081

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8081'

const TOKEN_KEY = 'idv_token'
const USER_KEY = 'idv_user'
const EXPIRE_KEY = 'idv_expires_at'

// ---------- 令牌读写 ----------
//
// 「记住我」决定登录态存在哪儿：
//   勾选   → localStorage   长期保留，关浏览器也在
//   不勾选 → sessionStorage 关掉浏览器（标签页）就没了
// 读取时先看 sessionStorage 再看 localStorage，两种模式互不覆盖、可以共存。

const boxes = () => [window.sessionStorage, window.localStorage]

/** 当前登录态存在哪种存储里 */
function boxOf(key) {
  return window.sessionStorage.getItem(key) !== null ? window.sessionStorage : window.localStorage
}

export function getToken() {
  return (
    window.sessionStorage.getItem(TOKEN_KEY) ||
    window.localStorage.getItem(TOKEN_KEY) ||
    ''
  )
}

export function hasToken() {
  return !!getToken()
}

/** 是否勾选过「记住我」（= 令牌在 localStorage 里） */
export function isRemembered() {
  return window.localStorage.getItem(TOKEN_KEY) !== null
}

/** 令牌失效时间戳（毫秒），没记录过返回 null */
export function readExpiresAt() {
  const raw = boxOf(EXPIRE_KEY).getItem(EXPIRE_KEY)
  const n = Number(raw)
  return Number.isFinite(n) && n > 0 ? n : null
}

/**
 * 保存登录态
 * @param {string}  token     JWT
 * @param {object}  user      用户信息
 * @param {boolean} remember  是否勾选「记住我」
 * @param {number}  expiresIn 令牌有效期（秒），后端返回，用来算「有效期至」
 */
export function saveAuth(token, user, remember, expiresIn) {
  clearAuth()
  const box = remember ? window.localStorage : window.sessionStorage
  box.setItem(TOKEN_KEY, token)
  box.setItem(USER_KEY, JSON.stringify(user || null))
  if (Number.isFinite(expiresIn) && expiresIn > 0) {
    box.setItem(EXPIRE_KEY, String(Date.now() + expiresIn * 1000))
  }
}

export function readUser() {
  try {
    return JSON.parse(boxOf(USER_KEY).getItem(USER_KEY) || 'null')
  } catch {
    return null
  }
}

/** 登录后刷新用户信息时用：写到令牌所在的同一存储，避免两边不一致 */
export function saveUser(user) {
  boxOf(TOKEN_KEY).setItem(USER_KEY, JSON.stringify(user || null))
}

/** 登出 / 令牌失效时调用：两种存储一起清，避免残留半套状态 */
export function clearAuth() {
  for (const box of boxes()) {
    box.removeItem(TOKEN_KEY)
    box.removeItem(USER_KEY)
    box.removeItem(EXPIRE_KEY)
  }
}

// ---------- 错误类型 ----------

/**
 * 统一的接口错误：code 为后端 Result.code（-1 表示压根连不上后端）
 */
export class ApiError extends Error {
  constructor(code, message) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

// ---------- 请求 ----------

/**
 * @param {string} path   以 / 开头的接口路径，如 /api/auth/login
 * @param {object} options method / body / auth(是否携带令牌，默认 true)
 */
export async function request(path, options = {}) {
  const { method = 'GET', body, auth = true } = options

  const headers = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  const token = getToken()
  if (auth && token) headers['Authorization'] = `Bearer ${token}`

  let res
  try {
    res = await fetch(`${API_BASE}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body)
    })
  } catch {
    throw new ApiError(-1, `连接不上后端（${API_BASE}），请先启动 springboottest 后端的 8081 端口`)
  }

  // 注意：后端 GlobalExceptionHandler 把业务异常也包成 HTTP 200，
  // 所以不能只看 res.ok，必须判断 body.code
  const payload = await res.json().catch(() => null)

  if (!res.ok) {
    throw new ApiError(res.status, `请求失败：HTTP ${res.status}`)
  }
  if (!payload) {
    throw new ApiError(500, '后端返回的内容不是合法 JSON，请检查接口地址')
  }
  if (payload.code === 401) {
    // 令牌过期或无效，清掉本地登录态，避免一直带着废令牌重试
    clearAuth()
    throw new ApiError(401, payload.message || '登录状态已失效，请重新登录')
  }
  if (payload.code !== 200) {
    throw new ApiError(payload.code, payload.message || '请求失败')
  }
  return payload.data
}

export { API_BASE }
