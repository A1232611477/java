import { request } from './http'

// 五子棋联机接口

/** 邀请好友联机，返回 { roomId, color:1 } */
export function inviteFriend(friendId) {
  return request('/api/gomoku/invite', { method: 'POST', body: { friendId } })
}

/** 接受邀请，返回 { roomId, color:2, hostNickname } */
export function acceptInvite(roomId) {
  return request('/api/gomoku/accept', { method: 'POST', body: { roomId } })
}

/** 拒绝邀请 */
export function declineInvite(roomId) {
  return request('/api/gomoku/decline', { method: 'POST', body: { roomId } })
}

/** 取消邀请 */
export function cancelInvite(roomId) {
  return request('/api/gomoku/cancel', { method: 'POST', body: { roomId } })
}

/** 查询当前用户待处理的邀请 */
export function fetchPending() {
  return request('/api/gomoku/pending')
}

/** 查询房间快照 */
export function fetchGomokuRoom(roomId) {
  return request(`/api/gomoku/room/${roomId}`)
}
