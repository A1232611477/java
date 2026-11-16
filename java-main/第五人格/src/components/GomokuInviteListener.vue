<template>
  <Transition name="drop">
    <div v-if="invite" class="invite-card" @click.stop>
      <div class="ic-head">
        <span class="ic-emblem">⚫</span>
        <div class="ic-meta">
          <p class="ic-title">{{ invite.fromNickname || '好友' }} 邀请你下五子棋</p>
          <p class="ic-sub">接受后将进入联机对局，你执白棋</p>
        </div>
      </div>
      <div class="ic-actions">
        <button class="ic-btn ok" :disabled="busy" @click="onAccept">{{ busy ? '…' : '接受' }}</button>
        <button class="ic-btn no" :disabled="busy" @click="onDecline">拒绝</button>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useAuth } from '../stores/auth'
import { useToast } from '../composables/useToast'
import { API_BASE, getToken, ApiError } from '../api/http'
import { acceptInvite, declineInvite, fetchPending } from '../api/gomoku'

const router = useRouter()
const { show } = useToast()
const { isLogged } = useAuth()

const invite = ref(null)
const busy = ref(false)
let client = null
let pollTimer = null

const JOIN_KEY = 'idv_gomoku_join'

function connect() {
  disconnect()
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
      client.subscribe('/user/queue/gomoku', (msg) => {
        try {
          const d = JSON.parse(msg.body)
          if (d && d.type === 'INVITE') setInvite(d)
          else if (d && d.type === 'CANCELLED') {
            // 对方取消了邀请
            if (invite.value && invite.value.roomId === d.roomId) invite.value = null
          }
        } catch (e) { /* 忽略 */ }
      })
    }
    client.activate()
  } catch (e) { /* 连接失败不阻塞 */ }
}

function disconnect() {
  if (client) {
    try { client.deactivate() } catch (e) { /* 忽略 */ }
    client = null
  }
}

function setInvite(d) {
  if (!d || !d.roomId) return
  if (invite.value && invite.value.roomId === d.roomId) return
  invite.value = { roomId: d.roomId, fromUserId: d.fromUserId, fromNickname: d.fromNickname }
}

async function poll() {
  try {
    const d = await fetchPending()
    if (d && d.roomId) setInvite({ roomId: d.roomId, fromUserId: d.fromUserId, fromNickname: d.fromNickname })
  } catch (e) { /* 静默 */ }
}

async function onAccept() {
  if (busy.value || !invite.value) return
  busy.value = true
  try {
    await acceptInvite(invite.value.roomId)
    try { localStorage.setItem(JOIN_KEY, invite.value.roomId) } catch (e) { /* 忽略 */ }
    invite.value = null
    router.push({ name: 'community' })
  } catch (e) {
    show(e instanceof ApiError ? e.message : '接受邀请失败')
    invite.value = null
  } finally {
    busy.value = false
  }
}

async function onDecline() {
  if (busy.value || !invite.value) return
  busy.value = true
  try {
    await declineInvite(invite.value.roomId)
  } catch (e) { /* 静默 */ }
  invite.value = null
  busy.value = false
}

watch(isLogged, (logged) => {
  if (logged) {
    connect()
    poll()
    if (pollTimer) clearInterval(pollTimer)
    pollTimer = setInterval(poll, 6000)
  } else {
    disconnect()
    if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
    invite.value = null
  }
}, { immediate: true })

onBeforeUnmount(() => {
  disconnect()
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
})
</script>

<style scoped>
.invite-card {
  position: fixed; top: 76px; right: 20px; z-index: 80;
  width: 320px; padding: 16px 18px;
  background: linear-gradient(165deg, rgba(9,20,46,0.98), rgba(6,14,36,0.98));
  border: 1px solid var(--border); border-radius: 16px;
  box-shadow: 0 26px 70px rgba(0,0,0,0.62), 0 0 34px rgba(216,177,90,0.14);
  backdrop-filter: blur(20px);
}
.invite-card::before {
  content: ""; position: absolute; top: 0; left: 0; right: 0; height: 2px;
  background: linear-gradient(90deg, transparent, var(--gold), transparent);
}
.ic-head { display: flex; align-items: center; gap: 12px; }
.ic-emblem {
  flex: none; width: 42px; height: 42px; border-radius: 50%;
  display: grid; place-items: center; font-size: 20px; color: var(--ink-1);
  background: radial-gradient(circle at 32% 28%, var(--gold-light), var(--gold) 70%);
  box-shadow: 0 0 0 2px rgba(216,177,90,0.5), 0 0 16px rgba(216,177,90,0.4);
}
.ic-meta { flex: 1; min-width: 0; }
.ic-title { font-size: 14px; font-weight: 700; color: var(--text); }
.ic-sub { font-size: 12px; color: var(--muted); margin-top: 3px; }
.ic-actions { display: flex; gap: 10px; margin-top: 14px; }
.ic-btn { flex: 1; padding: 9px 0; border-radius: 10px; font-size: 14px; cursor: pointer; font-family: var(--font); letter-spacing: 0.08em; transition: filter .15s, transform .15s; }
.ic-btn.ok { border: none; background: linear-gradient(120deg, var(--gold-light), var(--gold) 60%, #b8902f); color: #07101f; font-weight: 700; }
.ic-btn.ok:hover { filter: brightness(1.06); transform: translateY(-1px); }
.ic-btn.no { background: transparent; border: 1px solid rgba(159,178,216,0.32); color: #cdd9f2; }
.ic-btn.no:hover { border-color: var(--gold); color: var(--gold-light); }
.ic-btn:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }

.drop-enter-active, .drop-leave-active { transition: opacity 0.2s, transform 0.2s; }
.drop-enter-from, .drop-leave-to { opacity: 0; transform: translateY(-10px); }
</style>
