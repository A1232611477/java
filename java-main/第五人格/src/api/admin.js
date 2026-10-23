import { requestWithToken, getAdminToken } from './http'

// 所有后台接口统一从「管理员令牌」取 Authorization，与用户登录态隔离
function adminRequest(path, options = {}) {
  const token = getAdminToken()
  const authHeader = token ? `Bearer ${token}` : ''
  return requestWithToken(path, options, authHeader)
}

/** 管理员登录：POST /api/admin/login */
export function adminLogin({ nickname, password, rememberMe }) {
  return adminRequest('/api/admin/login', {
    method: 'POST',
    body: { nickname, password, rememberMe: !!rememberMe }
  })
}

/** 内容列表（分页 + 关键词 + 分类 + 状态过滤） */
export function fetchContent(params = {}) {
  const qs = new URLSearchParams()
  if (params.keyword) qs.set('keyword', params.keyword)
  if (params.categoryId) qs.set('categoryId', params.categoryId)
  if (params.status !== undefined && params.status !== null && params.status !== '') {
    qs.set('status', params.status)
  }
  if (params.page) qs.set('page', params.page)
  if (params.size) qs.set('size', params.size)
  const query = qs.toString()
  return adminRequest(`/api/admin/content${query ? '?' + query : ''}`)
}

/** 内容详情 */
export function getContent(id) {
  return adminRequest(`/api/admin/content/${id}`)
}

/** 新增内容 */
export function createContent(payload) {
  return adminRequest('/api/admin/content', { method: 'POST', body: payload })
}

/** 编辑内容 */
export function updateContent(id, payload) {
  return adminRequest(`/api/admin/content/${id}`, { method: 'PUT', body: payload })
}

/** 删除内容 */
export function deleteContent(id) {
  return adminRequest(`/api/admin/content/${id}`, { method: 'DELETE' })
}

/** 上架 / 下架 */
export function setContentStatus(id, status) {
  return adminRequest(`/api/admin/content/${id}/status?status=${status}`, { method: 'PATCH' })
}

/** 概览统计 */
export function fetchStats() {
  return adminRequest('/api/admin/content/stats/overview')
}

/** 内容表单用分类选项 */
export function fetchCategoryOptions() {
  return adminRequest('/api/admin/content/categories/options')
}
