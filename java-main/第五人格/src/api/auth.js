import { request } from './http'

/** 注册：POST /api/auth/register { nickname, password } */
export function register({ nickname, password }) {
  return request('/api/auth/register', {
    method: 'POST',
    body: { nickname, password },
    auth: false
  })
}

/**
 * 登录：POST /api/auth/login { nickname, password, rememberMe }
 * 返回 { token, tokenType, expiresIn, user }
 *
 * @param {boolean} [rememberMe] 勾选「记住我」时传 true，后端会签发长效令牌
 */
export function login({ nickname, password, rememberMe }) {
  return request('/api/auth/login', {
    method: 'POST',
    body: { nickname, password, rememberMe: !!rememberMe },
    auth: false
  })
}

/** 当前登录用户：GET /api/user/me（需要令牌） */
export function fetchMe() {
  return request('/api/user/me')
}
