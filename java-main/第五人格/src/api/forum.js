import { request } from './http'

// 论坛接口

/** 帖子列表（分页） */
export function fetchPosts(page = 1, size = 20) {
  return request(`/api/forum?page=${page}&size=${size}`)
}

/** 发帖 */
export function createPost(title, content) {
  return request('/api/forum', { method: 'POST', body: { title, content } })
}

/** 帖子详情 */
export function fetchPostDetail(id) {
  return request(`/api/forum/${id}`)
}

/** 删除帖子（仅作者） */
export function deletePost(id) {
  return request(`/api/forum/${id}`, { method: 'DELETE' })
}
