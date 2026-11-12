<template>
  <div class="friends-page">
    <NightSky />
    <header class="com-topbar">
      <div class="tb-inner">
        <div class="tb-brand" @click="$router.replace('/home')">
          <span class="tb-emblem">祭</span>
          <span class="tb-word">第五人格 · 好友与聊天</span>
        </div>
        <button class="tb-back" @click="$router.replace('/home')">← 返回首页</button>
      </div>
    </header>

    <main class="friends-main">
      <div class="layout">
        <!-- 左栏：好友 + 请求 -->
        <aside class="side">
          <!-- 添加好友（支持昵称模糊搜索 + UID） -->
          <div class="add-friend-wrap">
            <div class="add-friend">
              <input
                v-model="addName"
                class="inp"
                placeholder="输入昵称或 UID 搜索好友…"
                @input="onSearch"
                @keyup.enter="onAdd"
              />
              <button class="btn-primary sm" :disabled="adding" @click="onAdd">{{ adding ? '…' : '添加' }}</button>
            </div>
            <!-- 搜索结果下拉 -->
            <div v-if="results.length" class="search-results">
              <div
                v-for="r in results"
                :key="r.id"
                class="search-item"
                @click="pickResult(r)"
              >
                <span class="req-avatar">{{ (r.nickname || '友').charAt(0) }}</span>
                <span class="req-name">{{ r.nickname }}</span>
                <span class="mini-uid">UID {{ r.id }}</span>
                <span v-if="r.status === 1" class="mini-off">已禁用</span>
              </div>
            </div>
            <p v-else-if="searched && !searching" class="search-empty">未找到匹配用户</p>
          </div>

          <!-- 待处理请求 -->
          <div v-if="requests.length" class="req-block">
            <p class="side-title">好友请求 <span class="badge-count">{{ requests.length }}</span></p>
            <div v-for="r in requests" :key="r.id" class="req-item">
              <span class="req-avatar">{{ (r.nickname || '友').charAt(0) }}</span>
              <span class="req-name">{{ r.nickname }}</span>
              <button class="link-btn" @click="onAccept(r)">接受</button>
              <button class="link-btn danger" @click="onReject(r)">拒绝</button>
            </div>
          </div>

          <!-- 好友列表 -->
          <p class="side-title">我的好友 <span class="badge-count">{{ friends.length }}</span></p>
          <div class="friend-list">
            <div
              v-for="f in friends"
              :key="f.id"
              class="friend-item"
              :class="{ active: activePeer?.id === f.id }"
              @click="openChat(f)"
            >
              <span class="req-avatar">{{ (f.nickname || '友').charAt(0) }}</span>
              <span class="req-name">{{ f.nickname }}</span>
              <span v-if="f.status === 1" class="mini-off">离线</span>
            </div>
            <p v-if="!friends.length" class="empty">暂无好友，添加好友后即可聊天。</p>
          </div>
        </aside>

        <!-- 右栏：聊天窗口 -->
        <section class="chat-panel panel">
          <template v-if="activePeer">
            <header class="chat-head">
              <div class="chat-peer">
                <span class="chat-avatar">{{ (activePeer.nickname || '友').charAt(0) }}</span>
                <span class="chat-name">{{ activePeer.nickname }}</span>
              </div>
              <div class="chat-actions">
                <RouterLink to="/community" class="link-btn teamup" title="去联机组队">🎮 联机组队</RouterLink>
              </div>
            </header>

            <div class="chat-body" ref="chatBody">
              <div v-if="messages.length" class="msg-list">
                <div
                  v-for="m in messages"
                  :key="m.id"
                  class="msg-row"
                  :class="{ mine: m.senderId === myId }"
                >
                  <div class="msg-bubble">
                    <p class="msg-text">{{ m.content }}</p>
                    <span class="msg-time">{{ fmt(m.createdAt) }}</span>
                  </div>
                </div>
              </div>
              <p v-else class="empty">还没有消息，打个招呼吧～</p>
            </div>

            <footer class="chat-input">
              <input v-model="draft" class="inp full" placeholder="输入消息…" @keyup.enter="onSend" />
              <button class="btn-primary sm" :disabled="sending" @click="onSend">发送</button>
            </footer>
          </template>

          <div v-else class="chat-empty">
            <p class="chat-empty-icon">✉</p>
            <p>选择左侧好友开始聊天</p>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import NightSky from '../components/NightSky.vue'
import { useToast } from '../composables/useToast'
import { useAuth } from '../stores/auth'
import { ApiError, getToken, API_BASE } from '../api/http'
import {
  fetchFriends, fetchFriendRequests, addFriend, searchUsers,
  acceptFriend, rejectFriend, removeFriend, fetchHistory, sendMessage
} from '../api/social'

const { show } = useToast()
const { user } = useAuth()

const friends = ref([])
const requests = ref([])
const addName = ref('')
const adding = ref(false)
const results = ref([])
const searched = ref(false)
const searching = ref(false)
let searchTimer = null

const activePeer = ref(null)
const messages = ref([])
const draft = ref('')
const sending = ref(false)
const chatBody = ref(null)

const myId = computed(() => user.value?.id)

// ---------- 好友 ----------
async function loadFriends() {
  try {
    friends.value = await fetchFriends()
  } catch (e) {
    show(e instanceof ApiError ? e.message : '好友列表加载失败')
  }
}

async function loadRequests() {
  try {
    requests.value = await fetchFriendRequests()
  } catch (e) { /* 静默 */ }
}

async function onAdd() {
  const name = addName.value.trim()
  if (!name) { show('请输入要添加的昵称或 UID'); return }
  adding.value = true
  try {
    await addFriend(name)
    show('已发送好友请求')
    addName.value = ''
    results.value = []
    searched.value = false
    await loadFriends()
    await loadRequests()
  } catch (e) {
    show(e instanceof ApiError ? e.message : '添加失败')
  } finally {
    adding.value = false
  }
}

// 输入时实时搜索（防抖 300ms）
function onSearch() {
  const kw = addName.value.trim()
  clearTimeout(searchTimer)
  if (!kw) {
    results.value = []
    searched.value = false
    return
  }
  searching.value = true
  searched.value = false
  searchTimer = setTimeout(async () => {
    try {
      results.value = await searchUsers(kw)
      searched.value = true
    } catch (e) {
      results.value = []
      searched.value = true
    } finally {
      searching.value = false
    }
  }, 300)
}

// 点击搜索结果直接添加
async function pickResult(r) {
  addName.value = r.nickname
  results.value = []
  searched.value = false
  await onAdd()
}

async function onAccept(r) {
  try {
    await acceptFriend(r.id)
    show(`已与「${r.nickname}」成为好友`)
    await loadFriends()
    await loadRequests()
  } catch (e) {
    show(e instanceof ApiError ? e.message : '操作失败')
  }
}

async function onReject(r) {
  try {
    await rejectFriend(r.id)
    await loadRequests()
  } catch (e) {
    show(e instanceof ApiError ? e.message : '操作失败')
  }
}

async function onRemove(f) {
  if (!confirm(`确定删除好友「${f.nickname}」？`)) return
  try {
    await removeFriend(f.id)
    if (activePeer.value?.id === f.id) activePeer.value = null
    await loadFriends()
  } catch (e) {
    show(e instanceof ApiError ? e.message : '操作失败')
  }
}

// ---------- 聊天 ----------
async function openChat(f) {
  activePeer.value = f
  messages.value = []
  try {
    messages.value = await fetchHistory(f.id, 50)
    scrollToBottom()
  } catch (e) {
    show(e instanceof ApiError ? e.message : '聊天记录加载失败')
  }
}

async function onSend() {
  const text = draft.value.trim()
  if (!text) return
  if (!activePeer.value) return
  sending.value = true
  try {
    await sendMessage(activePeer.value.id, text)
    draft.value = ''
    // 重新拉取历史，保证顺序一致
    messages.value = await fetchHistory(activePeer.value.id, 50)
    scrollToBottom()
  } catch (e) {
    show(e instanceof ApiError ? e.message : '发送失败')
  } finally {
    sending.value = false
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
  })
}

function fmt(s) {
  if (!s) return ''
  return String(s).replace('T', ' ').slice(5, 16)
}

// ---------- WebSocket 实时收私信 ----------
let client = null

function connectSocket() {
  const token = getToken()
  if (!token) return
  try {
    client = new Client({
      webSocketFactory: () => new SockJS(`${API_BASE}/ws`),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      debug: () => {}
    })
    client.onConnect = () => {
      client.subscribe('/user/queue/chat', (msg) => {
        try {
          const data = JSON.parse(msg.body)
          // 若当前正与该发送者聊天，追加消息；否则仅提示
          if (activePeer.value && data.senderId === activePeer.value.id) {
            messages.value.push(data)
            scrollToBottom()
          } else if (data.senderId !== myId.value) {
            show(`收到「${data.senderNickname || '好友'}」的新消息`)
          }
        } catch (e) { /* 忽略 */ }
      })
    }
    client.activate()
  } catch (e) { /* WebSocket 连接失败不阻塞页面 */ }
}

onMounted(async () => {
  await loadFriends()
  await loadRequests()
  connectSocket()
})

onBeforeUnmount(() => {
  if (client) {
    try { client.deactivate() } catch (e) { /* 忽略 */ }
    client = null
  }
})
</script>

<style scoped>
.friends-page { position: relative; min-height: 100vh; color: var(--text); z-index: 1; }
.com-topbar { position: sticky; top: 0; z-index: 40; background: rgba(6, 12, 28, 0.92); backdrop-filter: blur(8px); border-bottom: 1px solid var(--border); }
.tb-inner { max-width: 1080px; margin: 0 auto; padding: 12px 20px; display: flex; align-items: center; justify-content: space-between; }
.tb-brand { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.tb-emblem { width: 34px; height: 34px; border-radius: 50%; background: var(--gold); color: #1a1208; display: grid; place-items: center; font-weight: 800; font-size: 16px; }
.tb-word { font-family: var(--font); color: var(--gold-light); font-size: 16px; letter-spacing: 1px; }
.tb-back { height: 36px; padding: 0 16px; border: 1px solid var(--border); border-radius: 18px; background: transparent; color: var(--text); font-size: 13px; cursor: pointer; }
.tb-back:hover { border-color: var(--gold); color: var(--gold-light); }

.friends-main { max-width: 1080px; margin: 22px auto 60px; padding: 0 20px; }
.layout { display: grid; grid-template-columns: 300px 1fr; gap: 20px; align-items: start; }
@media (max-width: 760px) { .layout { grid-template-columns: 1fr; } }

.side { display: flex; flex-direction: column; gap: 16px; }
.add-friend-wrap { position: relative; }
.add-friend { display: flex; gap: 8px; }
.add-friend .inp { flex: 1; }
.search-results { position: absolute; top: calc(100% + 4px); left: 0; right: 0; z-index: 30; background: var(--card); border: 1px solid var(--border); border-radius: 10px; box-shadow: 0 8px 24px rgba(0,0,0,0.4); max-height: 240px; overflow: auto; }
.search-item { display: flex; align-items: center; gap: 8px; padding: 8px 10px; cursor: pointer; transition: background .15s; }
.search-item:hover { background: rgba(216,177,90,0.1); }
.mini-uid { font-size: 11px; color: var(--muted); margin-left: auto; }
.search-empty { font-size: 12px; color: var(--muted); padding: 6px 2px; }
.side-title { font-size: 14px; color: var(--gold-light); font-weight: 700; margin-bottom: 8px; }
.badge-count { font-size: 12px; color: var(--gold); margin-left: 4px; }

.req-block { background: var(--card); border: 1px solid var(--border); border-radius: 12px; padding: 14px; }
.req-item { display: flex; align-items: center; gap: 8px; padding: 6px 0; }
.req-avatar { width: 30px; height: 30px; border-radius: 50%; background: linear-gradient(135deg, var(--blue), var(--ink-3)); color: var(--gold-light); display: grid; place-items: center; font-weight: 700; font-size: 13px; flex-shrink: 0; }
.req-name { flex: 1; font-size: 14px; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.friend-list { background: var(--card); border: 1px solid var(--border); border-radius: 12px; padding: 10px; max-height: 460px; overflow: auto; }
.friend-item { display: flex; align-items: center; gap: 8px; padding: 8px; border-radius: 8px; cursor: pointer; transition: background .15s; }
.friend-item:hover { background: rgba(216,177,90,0.08); }
.friend-item.active { background: rgba(216,177,90,0.16); }
.mini-off { font-size: 11px; color: var(--muted); }

.chat-panel { display: flex; flex-direction: column; height: 560px; padding: 0; overflow: hidden; }
.chat-head { display: flex; align-items: center; justify-content: space-between; padding: 14px 18px; border-bottom: 1px solid var(--border); }
.chat-peer { display: flex; align-items: center; gap: 10px; }
.chat-avatar { width: 34px; height: 34px; border-radius: 50%; background: linear-gradient(135deg, var(--blue), var(--ink-3)); color: var(--gold-light); display: grid; place-items: center; font-weight: 700; }
.chat-name { font-size: 15px; font-weight: 700; }
.teamup { margin-right: 0; }
.chat-body { flex: 1; overflow-y: auto; padding: 16px 18px; }
.msg-list { display: flex; flex-direction: column; gap: 10px; }
.msg-row { display: flex; }
.msg-row.mine { justify-content: flex-end; }
.msg-bubble { max-width: 70%; background: rgba(43,91,181,0.24); border: 1px solid rgba(127,168,232,0.28); padding: 8px 12px; border-radius: 12px; }
.msg-row.mine .msg-bubble { background: rgba(216,177,90,0.16); border-color: rgba(216,177,90,0.4); }
.msg-text { font-size: 14px; color: var(--text); word-break: break-word; }
.msg-time { font-size: 10px; color: var(--muted); margin-top: 4px; display: block; }
.chat-input { display: flex; gap: 8px; padding: 12px 14px; border-top: 1px solid var(--border); }
.chat-input .inp { flex: 1; }
.chat-empty { flex: 1; display: grid; place-items: center; color: var(--muted); }
.chat-empty-icon { font-size: 40px; margin-bottom: 10px; }
.empty { text-align: center; color: var(--muted); padding: 20px 0; font-size: 13px; }
</style>
