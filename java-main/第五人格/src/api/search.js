import { request } from './http'

/**
 * 首页展示数据（分级分类树 + 精选内容）
 * 对应后端 GET /api/home，需要登录令牌（由 request 自动携带）。
 * 返回结构：{ categories: [{id,name,code,children:[...]}], featured: [{...}] }
 */
export function fetchHome() {
  return request('/api/home')
}

/**
 * 搜索（支持分级分类）
 * 对应后端 GET /api/search，需要登录令牌。
 *
 * @param {object} params
 * @param {string} [params.q]            关键词（匹配标题/摘要/标签）
 * @param {number} [params.categoryId]   一级分类 id（按大类过滤）
 * @param {number} [params.subCategoryId] 二级分类 id（按小类过滤）
 * @param {number} [params.page]         页码（默认 1）
 * @param {number} [params.size]         每页条数（默认 10）
 * @returns 后端 SearchResultVO：{ total, page, size, list }
 */
export function searchSite(params = {}) {
  const { q = '', categoryId = null, subCategoryId = null, page = 1, size = 10 } = params
  const sp = new URLSearchParams()
  if (q) sp.set('q', q)
  if (categoryId != null) sp.set('categoryId', String(categoryId))
  if (subCategoryId != null) sp.set('subCategoryId', String(subCategoryId))
  sp.set('page', String(page))
  sp.set('size', String(size))
  return request(`/api/search?${sp.toString()}`)
}

/**
 * 内容详情（含正文 content 与视频地址），供详情弹窗展示
 * 对应后端 GET /api/content/{id}，需要登录令牌。
 * @returns ContentDetailVO：{ id, title, category, subCategory, summary, coverUrl, videoUrl, content, author, tags, viewCount, createdAt }
 */
export function fetchContentDetail(id) {
  return request(`/api/content/${id}`)
}
