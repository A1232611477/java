import { request } from './http'

// 社交（好友 + 私信）接口，全部走用户令牌（request 自动携带）

/** 好友列表 */
export function fetchFriends() {
  return request('/api/social/friends')
}

/** 待处理的好友请求 */
export function fetchFriendRequests() {
  return request('/api/social/friend/requests')
}

/** 搜索用户（昵称模糊匹配，纯数字按 UID） */
export function searchUsers(keyword) {
  return request(`/api/social/user/search?keyword=${encodeURIComponent(keyword)}`)
}

/** 添加好友（按昵称；若对方已发过请求则等同接受） */
export function addFriend(nickname) {
  return request('/api/social/friend', { method: 'POST', body: { nickname } })
}

/** 接受好友请求 */
export function acceptFriend(friendId) {
  return request(`/api/social/friend/${friendId}/accept`, { method: 'POST' })
}

/** 拒绝好友请求 */
export function rejectFriend(friendId) {
  return request(`/api/social/friend/${friendId}/reject`, { method: 'POST' })
}

/** 删除好友 */
export function removeFriend(friendId) {
  return request(`/api/social/friend/${friendId}`, { method: 'DELETE' })
}

/** 与某好友的聊天记录 */
export function fetchHistory(peerId, limit = 50) {
  return request(`/api/social/message/${peerId}?limit=${limit}`)
}

/** 发送私信（REST 兜底） */
export function sendMessage(receiverId, content) {
  return request('/api/social/message', { method: 'POST', body: { receiverId, content } })
}

/** 会话列表 */
export function fetchConversations() {
  return request('/api/social/conversations')
}

/** 未读私信总数 */
export function fetchUnread() {
  return request('/api/social/unread')
}
