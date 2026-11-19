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
      <!-- 横幅 -->
      <section class="friends-hero">
        <div class="hero-orn">✉</div>
        <h1 class="hero-title">好 友 大 厅</h1>
        <p class="hero-sub">加好友 · 私聊 · 联机组队</p>
        <div class="hero-stats">
          <span class="stat-chip"><b>{{ friends.length }}</b> 位好友</span>
          <span class="stat-chip"><b>{{ onlineCount }}</b> 在线</span>
          <span class="stat-chip" v-if="requests.length">待处理 <b>{{ requests.length }}</b> 条</span>
        </div>
      </section>

      <div class="layout">
        <!-- 左栏：好友 + 请求 -->
        <aside class="side">
          <!-- 添加好友（支持昵称模糊搜索 + UID） -->
          <div class="add-friend-wrap">
            <div class="add-friend">
              <span class="add-ico">⌕</span>
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
            <p class="side-title"><span class="sec-mark">⚑</span> 好友请求 <span class="badge-count">{{ requests.length }}</span></p>
            <div v-for="r in requests" :key="r.id" class="req-item">
              <span class="req-avatar">{{ (r.nickname || '友').charAt(0) }}</span>
              <div class="req-mid">
                <span class="req-name">{{ r.nickname }}</span>
                <span class="req-sub">想和你成为好友</span>
              </div>
              <button class="mini-btn ok" @click="onAccept(r)">接受</button>
              <button class="mini-btn no" @click="onReject(r)">拒绝</button>
            </div>
          </div>

          <!-- 好友列表 -->
          <p class="side-title"><span class="sec-mark">✦</span> 我的好友 <span class="badge-count">{{ friends.length }}</span></p>
          <div class="friend-list">
            <div
              v-for="f in friends"
              :key="f.id"
              class="friend-item"
              :class="{ active: activePeer?.id === f.id }"
              @click="openChat(f)"
            >
              <span class="avatar-wrap">
                <span class="req-avatar">{{ (f.nickname || '友').charAt(0) }}</span>
                <span class="online-dot" :class="{ off: f.status === 1 }"></span>
              </span>
              <div class="f-mid">
                <span class="req-name">{{ f.nickname }}</span>
                <span class="f-sub">{{ f.status === 1 ? '离线' : '在线' }}</span>
              </div>
              <button class="invite-friend" @click.stop="openInvite(f)" title="邀请联机">🎮</button>
              <button class="del-friend" @click.stop="onRemove(f)" title="删除好友">×</button>
            </div>
            <p v-if="!friends.length" class="empty">暂无好友，添加好友后即可聊天。</p>
          </div>
        </aside>

        <!-- 右栏：聊天窗口 -->
        <section class="chat-panel panel">
          <template v-if="activePeer">
            <header class="chat-head">
              <div class="chat-peer">
                <span class="avatar-wrap">
                  <span class="chat-avatar">{{ (activePeer.nickname || '友').charAt(0) }}</span>
                  <span class="online-dot" :class="{ off: activePeer.status === 1 }"></span>
                </span>
                <div class="cp-mid">
                  <span class="chat-name">{{ activePeer.nickname }}</span>
                  <span class="cp-sub">{{ activePeer.status === 1 ? '离线' : '在线' }}</span>
                </div>
              </div>
              <div class="chat-actions">
                <button class="link-btn teamup" @click="openInvite(activePeer)">🎮 邀请联机</button>
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
                  <span v-if="m.senderId !== myId" class="msg-avatar">{{ (activePeer.nickname || '友').charAt(0) }}</span>
                  <div class="msg-bubble">
                    <p class="msg-text">{{ m.content }}</p>
                    <span class="msg-time">{{ fmt(m.createdAt) }}</span>
                  </div>
                </div>
              </div>
              <div v-else class="chat-empty">
                <p class="chat-empty-icon">✉</p>
                <p>还没有消息，打个招呼吧～</p>
              </div>
            </div>

            <footer class="chat-input">
              <input v-model="draft" class="inp full" placeholder="输入消息，回车发送…" @keyup.enter="onSend" />
              <button class="btn-primary sm" :disabled="sending" @click="onSend">{{ sending ? '…' : '发送' }}</button>
            </footer>
          </template>

          <div v-else class="chat-empty idle">
            <div class="idle-orn">✉</div>
            <p>选择左侧好友开始聊天</p>
            <p class="idle-sub">实时私信 · 组队邀约 · 分享心得</p>
          </div>
        </section>
      </div>
    </main>

    <!-- 邀请联机弹窗 -->
    <Transition name="pop">
      <div v-if="inviteTarget" class="invite-mask" @click.self="inviteTarget = null">
        <div class="invite-modal">
          <h3 class="im-title">邀请「{{ inviteTarget.nickname }}」一起玩</h3>
          <div class="im-games">
            <button class="im-game" :disabled="inviting" @click="doInvite('gomoku')">
              <span class="im-ico">⚫</span>
              <span class="im-name">五子棋</span>
              <span class="im-desc">双人对弈 · 实时落子</span>
            </button>
            <button class="im-game" :disabled="inviting" @click="doInvite('escape')">
              <span class="im-ico">🕹</span>
              <span class="im-name">逃脱对决 1v4</span>
              <span class="im-desc">协作逃脱 · 对抗监管者</span>
            </button>
          </div>
          <button class="im-close" @click="inviteTarget = null">取消</button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
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
import { inviteFriend as inviteGomoku } from '../api/gomoku'
import { inviteFriend as inviteEscape } from '../api/game'

const { show } = useToast()
const { user } = useAuth()
const router = useRouter()

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
const onlineCount = computed(() => friends.value.filter(f => f.status !== 1).length)

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

// ---------- 邀请好友联机 ----------
const inviteTarget = ref(null)   // 要邀请的好友对象
const inviting = ref(false)      // 邀请请求中

function openInvite(f) {
  inviteTarget.value = f
}

async function doInvite(game) {
  if (!inviteTarget.value || inviting.value) return
  inviting.value = true
  const f = inviteTarget.value
  try {
    if (game === 'gomoku') {
      const data = await inviteGomoku(f.id)
      try { localStorage.setItem('idv_gomoku_host', data.roomId) } catch (e) { /* 忽略 */ }
      show(`已邀请「${f.nickname}」下五子棋，等待接受…`)
    } else {
      const data = await inviteEscape(f.id)
      try { localStorage.setItem('idv_escape_host', data.roomId) } catch (e) { /* 忽略 */ }
      show(`已邀请「${f.nickname}」玩逃脱对决，等待接受…`)
    }
    inviteTarget.value = null
    router.push({ name: 'community' })
  } catch (e) {
    show(e instanceof ApiError ? e.message : '邀请失败')
  } finally {
    inviting.value = false
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

/* ---------- 顶栏 ---------- */
.com-topbar { position: sticky; top: 0; z-index: 40; background: rgba(6, 12, 28, 0.92); backdrop-filter: blur(8px); border-bottom: 1px solid var(--border); }
.com-topbar::before { content: ""; position: absolute; top: 0; left: 0; right: 0; height: 1px; background: linear-gradient(90deg, transparent, var(--gold), transparent); }
.tb-inner { max-width: 1080px; margin: 0 auto; padding: 12px 20px; display: flex; align-items: center; justify-content: space-between; }
.tb-brand { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.tb-emblem { width: 34px; height: 34px; border-radius: 50%; background: radial-gradient(circle at 32% 28%, var(--gold-light), var(--gold) 70%); color: #1a1208; display: grid; place-items: center; font-weight: 800; font-size: 16px; box-shadow: 0 0 0 2px rgba(216,177,90,0.5), 0 0 16px rgba(216,177,90,0.4); }
.tb-word { font-family: var(--font); color: var(--gold-light); font-size: 16px; letter-spacing: 1px; }
.tb-back { height: 36px; padding: 0 16px; border: 1px solid var(--border); border-radius: 18px; background: transparent; color: var(--text); font-size: 13px; cursor: pointer; transition: border-color .2s, color .2s; }
.tb-back:hover { border-color: var(--gold); color: var(--gold-light); }

/* ---------- 横幅 ---------- */
.friends-main { max-width: 1080px; margin: 24px auto 60px; padding: 0 20px; }
.friends-hero {
  position: relative; text-align: center; padding: 2.2rem 1.5rem 1.9rem; margin-bottom: 1.5rem;
  border-radius: 18px; overflow: hidden;
  background: linear-gradient(160deg, rgba(43,91,181,0.14), rgba(9,20,46,0.55) 55%, rgba(216,177,90,0.08));
  border: 1px solid var(--border);
  box-shadow: 0 16px 44px rgba(2,6,18,0.5), inset 0 1px 0 rgba(255,255,255,0.05);
}
.friends-hero::before, .friends-hero::after {
  content: ""; position: absolute; width: 26px; height: 26px; pointer-events: none;
  border: 1.5px solid var(--gold); opacity: 0.85;
}
.friends-hero::before { top: 12px; left: 12px; border-right: none; border-bottom: none; border-top-left-radius: 10px; }
.friends-hero::after { bottom: 12px; right: 12px; border-left: none; border-top: none; border-bottom-right-radius: 10px; }
.hero-orn { font-size: 1.5rem; color: var(--gold); text-shadow: 0 0 14px rgba(216,177,90,0.7); animation: floaty 4s ease-in-out infinite; }
.hero-title {
  font-family: var(--font); font-weight: 700; font-size: 2.3rem; letter-spacing: 0.14em; margin: 0.35rem 0 0.45rem;
  background: linear-gradient(110deg, #fff 10%, var(--blue-light) 45%, var(--gold-light) 80%);
  background-size: 220% auto; -webkit-background-clip: text; background-clip: text; color: transparent;
  filter: drop-shadow(0 0 22px rgba(127,168,232,0.3)); animation: shimmer 6s linear infinite;
}
@keyframes shimmer { to { background-position: 220% center; } }
@keyframes floaty { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-6px); } }
.hero-sub { color: var(--muted); font-size: 0.95rem; letter-spacing: 0.06em; }
.hero-stats { display: flex; flex-wrap: wrap; justify-content: center; gap: 0.7rem; margin-top: 1.2rem; }
.stat-chip {
  padding: 0.34rem 0.95rem; border-radius: 999px; font-size: 0.82rem; color: #cdd9f2;
  background: rgba(255,255,255,0.04); border: 1px solid rgba(159,178,216,0.25);
}
.stat-chip b { color: var(--gold-light); font-family: var(--font); font-weight: 700; }

/* ---------- 布局 ---------- */
.layout { display: grid; grid-template-columns: 310px 1fr; gap: 20px; align-items: start; }
@media (max-width: 760px) { .layout { grid-template-columns: 1fr; } }

.side { display: flex; flex-direction: column; gap: 16px; }

/* ---------- 添加好友 ---------- */
.add-friend-wrap { position: relative; }
.add-friend { display: flex; align-items: center; gap: 8px; padding: 8px; background: var(--card); border: 1px solid var(--border); border-radius: 12px; }
.add-friend:focus-within { border-color: var(--gold); box-shadow: 0 0 0 3px rgba(216,177,90,0.14); }
.add-ico { color: var(--gold); font-size: 1.1rem; padding-left: 4px; }
.add-friend .inp { flex: 1; background: transparent; border: none; box-shadow: none; padding: 0.45rem 0.2rem; }
.add-friend .inp:focus { box-shadow: none; border-color: transparent; }
.search-results { position: absolute; top: calc(100% + 6px); left: 0; right: 0; z-index: 30; background: rgba(9,20,46,0.98); border: 1px solid var(--border); border-radius: 12px; box-shadow: 0 12px 32px rgba(0,0,0,0.5); max-height: 240px; overflow: auto; padding: 6px; }
.search-item { display: flex; align-items: center; gap: 8px; padding: 8px 10px; border-radius: 8px; cursor: pointer; transition: background .15s; }
.search-item:hover { background: rgba(216,177,90,0.1); }
.mini-uid { font-size: 11px; color: var(--muted); margin-left: auto; }
.mini-off { font-size: 11px; color: var(--muted); }
.search-empty { font-size: 12px; color: var(--muted); padding: 6px 2px; }

/* ---------- 侧栏标题 ---------- */
.side-title { display: flex; align-items: center; gap: 0.4rem; font-family: var(--font); font-size: 14px; color: var(--gold-light); letter-spacing: 1px; font-weight: 700; margin-bottom: 8px; }
.sec-mark { color: var(--gold); text-shadow: 0 0 10px rgba(216,177,90,0.6); font-size: 13px; }
.badge-count { font-size: 12px; color: var(--gold); margin-left: 2px; }

/* ---------- 好友请求 ---------- */
.req-block { background: var(--card); border: 1px solid var(--border); border-radius: 14px; padding: 14px; }
.req-item { display: flex; align-items: center; gap: 9px; padding: 7px 0; }
.req-item + .req-item { border-top: 1px dashed rgba(159,178,216,0.14); }
.req-avatar { width: 34px; height: 34px; border-radius: 50%; background: linear-gradient(135deg, var(--blue), var(--ink-3)); color: var(--gold-light); display: grid; place-items: center; font-weight: 700; font-size: 14px; flex-shrink: 0; box-shadow: 0 0 0 2px rgba(127,168,232,0.25); }
.req-mid { flex: 1; min-width: 0; }
.req-name { display: block; font-size: 14px; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.req-sub { display: block; font-size: 11px; color: var(--muted); margin-top: 2px; }
.mini-btn { flex: none; border-radius: 8px; font-size: 12px; padding: 4px 12px; cursor: pointer; transition: filter .2s, background .2s; }
.mini-btn.ok { border: none; background: linear-gradient(120deg, var(--gold-light), var(--gold) 60%, #b8902f); color: #07101f; font-weight: 700; }
.mini-btn.ok:hover { filter: brightness(1.06); }
.mini-btn.no { background: transparent; border: 1px solid rgba(159,178,216,0.32); color: #cdd9f2; }
.mini-btn.no:hover { border-color: #ff9a9a; color: #ffc2c2; }

/* ---------- 好友列表 ---------- */
.friend-list { background: var(--card); border: 1px solid var(--border); border-radius: 14px; padding: 10px; max-height: 480px; overflow: auto; }
.friend-item { display: flex; align-items: center; gap: 10px; padding: 9px 10px; border-radius: 10px; cursor: pointer; transition: background .15s; position: relative; }
.friend-item:hover { background: rgba(216,177,90,0.08); }
.friend-item.active { background: rgba(216,177,90,0.16); }
.friend-item.active::before { content: ""; position: absolute; left: 0; top: 50%; transform: translateY(-50%); width: 3px; height: 60%; border-radius: 3px; background: var(--gold); }
.avatar-wrap { position: relative; flex: none; }
.online-dot { position: absolute; right: -1px; bottom: -1px; width: 10px; height: 10px; border-radius: 50%; background: #58d68d; border: 2px solid var(--ink-2); box-shadow: 0 0 8px rgba(88,214,141,0.7); }
.online-dot.off { background: #5a6b8c; box-shadow: none; }
.f-mid { flex: 1; min-width: 0; }
.f-sub { display: block; font-size: 11px; color: var(--muted); margin-top: 2px; }
.del-friend { flex: none; width: 22px; height: 22px; border-radius: 50%; border: 1px solid rgba(255,154,154,0.4); background: transparent; color: #ff9a9a; font-size: 14px; line-height: 1; cursor: pointer; opacity: 0; transition: opacity .15s, background .15s; }
.friend-item:hover .del-friend { opacity: 1; }
.del-friend:hover { background: rgba(255,154,154,0.15); }

/* ---------- 聊天面板 ---------- */
.chat-panel { display: flex; flex-direction: column; height: 600px; padding: 0; overflow: hidden; }
.chat-head { display: flex; align-items: center; justify-content: space-between; padding: 13px 18px; border-bottom: 1px solid var(--border); background: linear-gradient(180deg, rgba(43,91,181,0.08), transparent); }
.chat-peer { display: flex; align-items: center; gap: 11px; }
.chat-avatar { width: 38px; height: 38px; border-radius: 50%; background: linear-gradient(135deg, var(--blue), var(--ink-3)); color: var(--gold-light); display: grid; place-items: center; font-weight: 700; font-size: 16px; box-shadow: 0 0 0 2px rgba(127,168,232,0.25); }
.cp-mid { display: flex; flex-direction: column; }
.chat-name { font-size: 15px; font-weight: 700; }
.cp-sub { font-size: 11px; color: var(--muted); }
.cp-sub.on { color: #7fe0ac; }
.teamup { margin-right: 0; text-decoration: none; }
.chat-body { flex: 1; overflow-y: auto; padding: 18px; }
.msg-list { display: flex; flex-direction: column; gap: 12px; }
.msg-row { display: flex; align-items: flex-end; gap: 9px; }
.msg-row.mine { justify-content: flex-end; }
.msg-avatar { flex: none; width: 30px; height: 30px; border-radius: 50%; background: linear-gradient(135deg, var(--blue), var(--ink-3)); color: var(--gold-light); display: grid; place-items: center; font-weight: 700; font-size: 13px; }
.msg-bubble { position: relative; max-width: 68%; background: rgba(43,91,181,0.22); border: 1px solid rgba(127,168,232,0.28); padding: 9px 13px; border-radius: 16px; }
.msg-row:not(.mine) .msg-bubble { border-bottom-left-radius: 5px; }
.msg-row.mine .msg-bubble { background: linear-gradient(135deg, rgba(216,177,90,0.24), rgba(216,177,90,0.12)); border-color: rgba(216,177,90,0.4); border-bottom-right-radius: 5px; }
.msg-text { font-size: 14px; color: var(--text); word-break: break-word; white-space: pre-wrap; }
.msg-time { font-size: 10px; color: var(--muted); margin-top: 4px; display: block; opacity: 0.8; }

.chat-input { display: flex; gap: 8px; padding: 12px 14px; border-top: 1px solid var(--border); background: rgba(4,8,26,0.3); }
.chat-input .inp { flex: 1; }

.chat-empty { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 0.5rem; color: var(--muted); text-align: center; padding: 1rem; }
.chat-empty-icon { font-size: 44px; color: var(--gold); text-shadow: 0 0 18px rgba(216,177,90,0.5); }
.idle-orn { font-size: 52px; color: var(--gold); text-shadow: 0 0 24px rgba(216,177,90,0.6); animation: floaty 4s ease-in-out infinite; }
.idle-sub { font-size: 0.8rem; opacity: 0.7; }

.empty { text-align: center; color: var(--muted); padding: 20px 0; font-size: 13px; }

/* ---------- 邀请联机 ---------- */
.invite-friend { flex: none; width: 26px; height: 26px; border-radius: 8px; border: 1px solid rgba(159,178,216,0.32); background: transparent; color: var(--gold-light); font-size: 13px; cursor: pointer; opacity: 0; transition: opacity .15s, background .15s, border-color .15s; display: grid; place-items: center; }
.friend-item:hover .invite-friend { opacity: 1; }
.invite-friend:hover { background: rgba(216,177,90,0.15); border-color: var(--gold); }

.invite-mask { position: fixed; inset: 0; z-index: 70; background: rgba(2,6,18,0.7); backdrop-filter: blur(4px); display: grid; place-items: center; padding: 20px; }
.invite-modal { width: 100%; max-width: 380px; background: rgba(9,20,46,0.98); border: 1px solid var(--border); border-radius: 16px; padding: 22px; box-shadow: 0 30px 80px rgba(0,0,0,0.6); }
.invite-modal::before { content: ""; display: block; position: absolute; top: 0; left: 0; right: 0; height: 2px; background: linear-gradient(90deg, transparent, var(--gold), transparent); }
.im-title { font-family: var(--font); font-size: 16px; color: var(--gold-light); letter-spacing: 1px; text-align: center; margin-bottom: 16px; }
.im-games { display: flex; flex-direction: column; gap: 10px; }
.im-game { display: flex; align-items: center; gap: 12px; padding: 13px 14px; border: 1px solid rgba(159,178,216,0.22); border-radius: 12px; background: rgba(255,255,255,0.03); color: var(--text); cursor: pointer; transition: .18s; text-align: left; }
.im-game:hover { border-color: var(--gold); background: rgba(216,177,90,0.1); transform: translateY(-2px); }
.im-game:disabled { opacity: 0.6; cursor: not-allowed; }
.im-ico { flex: none; width: 40px; height: 40px; border-radius: 50%; display: grid; place-items: center; font-size: 20px; color: #1a1208; background: radial-gradient(circle at 32% 28%, var(--gold-light), var(--gold) 70%); }
.im-name { flex: 1; font-size: 15px; font-weight: 700; }
.im-desc { font-size: 11px; color: var(--muted); }
.im-close { display: block; margin: 16px auto 0; padding: 7px 24px; border: 1px solid var(--border); border-radius: 999px; background: transparent; color: var(--muted); font-size: 13px; cursor: pointer; transition: .18s; }
.im-close:hover { color: var(--gold-light); border-color: var(--gold); }

.pop-enter-active, .pop-leave-active { transition: opacity .2s; }
.pop-enter-from, .pop-leave-to { opacity: 0; }
</style>
